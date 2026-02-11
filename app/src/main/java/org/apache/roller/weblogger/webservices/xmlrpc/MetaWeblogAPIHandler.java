/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 */

package org.apache.roller.weblogger.webservices.xmlrpc;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.MediaFileManager;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.MediaFile;
import org.apache.roller.weblogger.pojos.MediaFileDirectory;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntry.PubStatus;
import org.apache.roller.weblogger.pojos.WeblogEntrySearchCriteria;
import org.apache.roller.weblogger.util.RollerMessages;
import org.apache.roller.weblogger.util.Utilities;
import org.apache.xmlrpc.XmlRpcException;


/**
 * Weblogger XML-RPC Handler for the MetaWeblog API.
 *
 * <p>MetaWeblog API spec can be found at http://www.xmlrpc.com/metaWeblogApi</p>
 *
 * <h3>Refactoring notes (vs. original):</h3>
 * <ul>
 *   <li>Dependency Injection: {@code Weblogger} is injected via constructor
 *       (with a backward-compatible no-arg constructor for framework use).</li>
 *   <li>Parameter Objects: {@link NewPostRequest} replaces long parameter lists;
 *       {@link MediaUploadRequest} encapsulates media struct parsing.</li>
 *   <li>DTO extraction: Hashtable parsing in {@link PostRequestAdapter};
 *       response building in {@link PostResponseBuilder}.</li>
 *   <li>Removed {@code Thread.sleep} — timestamp uniqueness is a persistence concern.</li>
 *   <li>Replaced legacy {@code Hashtable}/{@code Vector} with {@code HashMap}/{@code ArrayList}.</li>
 *   <li>Extracted shared entry-population logic into {@link #populateEntry}.</li>
 *   <li>Replaced generic {@code catch (Exception)} with {@code catch (WebloggerException)}.</li>
 *   <li>Extracted magic numbers into named constants.</li>
 *   <li>Fixed resource leak in newMediaObject (InputStream now closed via try-with-resources).</li>
 * </ul>
 *
 * @author David M Johnson
 */
public class MetaWeblogAPIHandler extends BloggerAPIHandler {

    static final long serialVersionUID = -1364456614935668629L;

    private static final Log mLogger = LogFactory.getLog(MetaWeblogAPIHandler.class);

    /** Maximum length for auto-generated titles truncated from description. */
    private static final int TITLE_TRUNCATE_MAX = 15;
    /** Suffix appended when auto-generated titles are truncated. */
    private static final String TITLE_TRUNCATE_SUFFIX = "...";

    /** Response struct key for the media file URL. */
    private static final String RESP_URL = "url";

    private final Weblogger weblogger;
    private final PostResponseBuilder responseBuilder;

    /**
     * Backward-compatible no-arg constructor for framework / XML-RPC servlet use.
     * Delegates to the static factory.
     */
    public MetaWeblogAPIHandler() {
        this(WebloggerFactory.getWeblogger());
    }

    /**
     * Constructor that accepts an injected {@link Weblogger} instance,
     * enabling unit testing without static coupling.
     */
    public MetaWeblogAPIHandler(Weblogger weblogger) {
        super();
        this.weblogger = weblogger;
        this.responseBuilder = new PostResponseBuilder(weblogger);
    }


    // =====================================================================
    // Public API methods (MetaWeblog spec)
    // =====================================================================

    /**
     * Authenticates a user and returns the categories available in the website.
     *
     * @param blogid   Dummy Value for Weblogger
     * @param userid   Login for a MetaWeblog user who has permission to post to the blog
     * @param password Password for said username
     * @return Map of category name to category struct
     * @throws XmlRpcException on any processing error
     */
    public Object getCategories(String blogid, String userid, String password)
            throws Exception {

        mLogger.debug("getCategories() Called =====[ SUPPORTED ]=====");
        mLogger.debug("     BlogId: " + blogid);
        mLogger.debug("     UserId: " + userid);

        Weblog website = validate(blogid, userid, password);
        try {
            Map<String, Object> result = new HashMap<>();
            WeblogEntryManager weblogMgr = weblogger.getWeblogEntryManager();
            List<WeblogCategory> cats = weblogMgr.getWeblogCategories(website);
            for (WeblogCategory category : cats) {
                result.put(category.getName(),
                        responseBuilder.createCategoryStruct(category, userid));
            }
            return result;
        } catch (WebloggerException e) {
            String msg = "ERROR in MetaWeblogAPIHandler.getCategories";
            mLogger.error(msg, e);
            throw new XmlRpcException(UNKNOWN_EXCEPTION, msg);
        }
    }


    /**
     * Edits a given post. Optionally, will publish the blog after making the edit.
     *
     * @param postid   Unique identifier of the post to be changed
     * @param userid   Login for a MetaWeblog user who has permission to post to the blog
     * @param password Password for said username
     * @param struct   Contents of the post
     * @param publish  If &gt; 0, the blog will be published immediately
     * @throws XmlRpcException on any processing error
     * @return true on success
     */
    public boolean editPost(String postid, String userid, String password,
            Hashtable<String, ?> struct, int publish) throws Exception {
        return editPost(postid, userid, password, struct, publish > 0);
    }


    /**
     * Edits a given post. Optionally, will publish the blog after making the edit.
     *
     * @param postid   Unique identifier of the post to be changed
     * @param userid   Login for a MetaWeblog user who has permission to post to the blog
     * @param password Password for said username
     * @param struct   Contents of the post
     * @param publish  If true, the blog will be published immediately
     * @throws XmlRpcException on any processing error
     * @return true on success
     */
    public boolean editPost(String postid, String userid, String password,
            Hashtable<String, ?> struct, boolean publish) throws Exception {

        mLogger.debug("editPost() Called ========[ SUPPORTED ]=====");
        mLogger.debug("     PostId: " + postid);
        mLogger.debug("     UserId: " + userid);
        mLogger.debug("    Publish: " + publish);

        WeblogEntry entry = lookupEntryOrThrow(postid);
        validate(entry.getWebsite().getHandle(), userid, password);

        PostRequestAdapter request = new PostRequestAdapter(struct);
        mLogger.debug("      Title: " + request.getTitle());
        mLogger.debug("   Category: " + request.getFirstCategory());

        try {
            applyEditFields(entry, request, publish);
            saveAndFlush(entry);
            return true;

        } catch (WebloggerException e) {
            String msg = "ERROR in MetaWeblogAPIHandler.editPost";
            mLogger.error(msg, e);
            throw new XmlRpcException(UNKNOWN_EXCEPTION, msg);
        }
    }


    /**
     * Makes a new post to a designated blog. Optionally, will publish the blog
     * after making the post.
     *
     * @param blogid   Unique identifier of the blog the post will be added to
     * @param userid   Login for a MetaWeblog user who has permission to post to the blog
     * @param password Password for said username
     * @param struct   Contents of the post
     * @param publish  If &gt; 0, the blog will be published immediately
     * @throws XmlRpcException on any processing error
     * @return the new entry's ID
     */
    public String newPost(String blogid, String userid, String password,
            Hashtable<String, ?> struct, int publish) throws Exception {
        return newPost(blogid, userid, password, struct, publish > 0);
    }


    /**
     * Makes a new post to a designated blog. Optionally, will publish the blog
     * after making the post.
     *
     * @param blogid   Unique identifier of the blog the post will be added to
     * @param userid   Login for a MetaWeblog user who has permission to post to the blog
     * @param password Password for said username
     * @param struct   Contents of the post
     * @param publish  If true, the blog will be published immediately
     * @throws XmlRpcException on any processing error
     * @return the new entry's ID
     */
    public String newPost(String blogid, String userid, String password,
            Hashtable<String, ?> struct, boolean publish) throws Exception {

        mLogger.debug("newPost() Called ===========[ SUPPORTED ]=====");
        mLogger.debug("     BlogId: " + blogid);
        mLogger.debug("     UserId: " + userid);
        mLogger.debug("    Publish: " + publish);

        Weblog website = validate(blogid, userid, password);
        PostRequestAdapter request = new PostRequestAdapter(struct);

        String title = resolveNewPostTitle(request);
        Date dateCreated = resolveNewPostDate(request);
        mLogger.debug("      Title: " + title);

        try {
            WeblogEntryManager weblogMgr = weblogger.getWeblogEntryManager();
            User user = weblogger.getUserManager().getUserByUserName(userid);
            Timestamp current = new Timestamp(System.currentTimeMillis());

            NewPostRequest postReq = new NewPostRequest(
                    title, request.getDescription(), website,
                    user, dateCreated, current, publish);

            WeblogEntry entry = new WeblogEntry();
            populateEntry(entry, postReq);

            // MetaWeblog supports multiple cats, Weblogger supports one per entry.
            // Accept the first category that resolves; fall back to the blog default.
            resolveAndSetCategory(weblogMgr, entry, website,
                    resolveFirstValidCategory(weblogMgr, website, request.getCategories()),
                    website.getBloggerCategory());

            saveAndFlush(entry);
            return entry.getId();

        } catch (WebloggerException e) {
            String msg = "ERROR in MetaWeblogAPIHandler.newPost";
            mLogger.error(msg, e);
            throw new XmlRpcException(UNKNOWN_EXCEPTION, msg);
        }
    }


    /**
     * Retrieves a single post by its ID.
     *
     * @param postid   Unique post identifier
     * @param userid   Login for a MetaWeblog user
     * @param password Password for said username
     * @return post struct
     * @throws XmlRpcException on any processing error
     */
    public Object getPost(String postid, String userid, String password)
            throws Exception {

        mLogger.debug("getPost() Called =========[ SUPPORTED ]=====");
        mLogger.debug("     PostId: " + postid);
        mLogger.debug("     UserId: " + userid);

        WeblogEntry entry = lookupEntryOrThrow(postid);
        validate(entry.getWebsite().getHandle(), userid, password);

        try {
            return responseBuilder.createPostStruct(entry, userid);
        } catch (RuntimeException e) {
            String msg = "ERROR in MetaWeblogAPIHandler.getPost";
            mLogger.error(msg, e);
            throw new XmlRpcException(UNKNOWN_EXCEPTION, msg);
        }
    }


    /**
     * Allows a user to post a binary object (file) to Weblogger. If the file is
     * allowed by the RollerConfig file-upload settings, the file will be placed
     * in the user's upload directory.
     */
    public Object newMediaObject(String blogid, String userid, String password,
            Hashtable<String, ?> struct) throws Exception {

        mLogger.debug("newMediaObject() Called =[ SUPPORTED ]=====");
        mLogger.debug("     BlogId: " + blogid);
        mLogger.debug("     UserId: " + userid);
        mLogger.debug("   Password: *********");

        Weblog website = validate(blogid, userid, password);
        try {
            MediaUploadRequest uploadReq = MediaUploadRequest.fromStruct(struct);
            mLogger.debug("newMediaObject name: " + uploadReq.getName());
            mLogger.debug("newMediaObject type: " + uploadReq.getContentType());

            return saveMediaFile(website, uploadReq);

        } catch (XmlRpcException xe) {
            throw xe;
        } catch (WebloggerException e) {
            String msg = "ERROR in MetaWeblogAPIHandler.newMediaObject";
            mLogger.error(msg, e);
            throw new XmlRpcException(UNKNOWN_EXCEPTION, msg);
        }
    }


    /**
     * Get a list of recent posts for a category.
     *
     * @param blogid   Unique identifier of the blog
     * @param userid   Login for a Blogger user who has permission to post
     * @param password Password for said username
     * @param numposts Number of posts to retrieve
     * @return list of post structs
     * @throws XmlRpcException on any processing error
     */
    public Object getRecentPosts(String blogid, String userid, String password,
            int numposts) throws Exception {

        mLogger.debug("getRecentPosts() Called ===========[ SUPPORTED ]=====");
        mLogger.debug("     BlogId: " + blogid);
        mLogger.debug("     UserId: " + userid);
        mLogger.debug("     Number: " + numposts);

        Weblog website = validate(blogid, userid, password);

        try {
            List<Object> results = new ArrayList<>();

            WeblogEntryManager weblogMgr = weblogger.getWeblogEntryManager();
            if (website != null) {
                WeblogEntrySearchCriteria wesc = new WeblogEntrySearchCriteria();
                wesc.setWeblog(website);
                wesc.setSortBy(WeblogEntrySearchCriteria.SortBy.UPDATE_TIME);
                wesc.setMaxResults(numposts);
                List<WeblogEntry> entries = weblogMgr.getWeblogEntries(wesc);

                for (WeblogEntry entry : entries) {
                    results.add(responseBuilder.createPostStruct(entry, userid));
                }
            }
            return results;

        } catch (WebloggerException e) {
            String msg = "ERROR in MetaWeblogAPIHandler.getRecentPosts";
            mLogger.error(msg, e);
            throw new XmlRpcException(UNKNOWN_EXCEPTION, msg);
        }
    }


    // =====================================================================
    // Private helpers — focused, single-responsibility methods
    // =====================================================================

    /**
     * Looks up a {@link WeblogEntry} by ID, throwing {@link XmlRpcException}
     * if not found. Centralises the null-check that was previously inconsistent
     * across methods.
     */
    private WeblogEntry lookupEntryOrThrow(String postid) throws XmlRpcException, WebloggerException {
        WeblogEntryManager weblogMgr = weblogger.getWeblogEntryManager();
        WeblogEntry entry = weblogMgr.getWeblogEntry(postid);
        if (entry == null) {
            throw new XmlRpcException(INVALID_POSTID, INVALID_POSTID_MSG);
        }
        return entry;
    }

    /**
     * Resolves the title for a new post, using auto-truncated description as
     * fallback. Throws if both title and description are empty.
     */
    private String resolveNewPostTitle(PostRequestAdapter request) throws XmlRpcException {
        String title = request.getTitle();
        String description = request.getDescription();

        if (StringUtils.isEmpty(title) && StringUtils.isEmpty(description)) {
            throw new XmlRpcException(
                    BLOGGERAPI_INCOMPLETE_POST, "Must specify title or description");
        }
        if (StringUtils.isEmpty(title)) {
            title = Utilities.truncateNicely(description,
                    TITLE_TRUNCATE_MAX, TITLE_TRUNCATE_MAX, TITLE_TRUNCATE_SUFFIX);
        }
        return title;
    }

    /**
     * Resolves the publication date for a new post, defaulting to now.
     */
    private Date resolveNewPostDate(PostRequestAdapter request) {
        Date dateCreated = request.getDateCreated();
        return dateCreated != null ? dateCreated : new Date();
    }

    /**
     * Applies edit-specific fields to an existing entry from the request adapter.
     */
    private void applyEditFields(WeblogEntry entry, PostRequestAdapter request,
                                 boolean publish) throws WebloggerException {
        if (!request.getTitle().isEmpty()) {
            entry.setTitle(request.getTitle());
        }
        entry.setText(request.getDescription());
        entry.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        entry.setStatus(publish ? PubStatus.PUBLISHED : PubStatus.DRAFT);

        if (request.getDateCreated() != null) {
            entry.setPubTime(new Timestamp(request.getDateCreated().getTime()));
        }

        WeblogEntryManager weblogMgr = weblogger.getWeblogEntryManager();
        resolveAndSetCategory(weblogMgr, entry, entry.getWebsite(),
                request.getFirstCategory(), null);
    }

    /**
     * Populates a new {@link WeblogEntry} from a {@link NewPostRequest}
     * parameter object.
     */
    private void populateEntry(WeblogEntry entry, NewPostRequest req) {
        entry.setTitle(req.getTitle());
        entry.setText(req.getDescription());
        entry.setLocale(req.getWebsite().getLocale());
        entry.setPubTime(new Timestamp(req.getDateCreated().getTime()));
        entry.setUpdateTime(req.getUpdateTime());
        entry.setWebsite(req.getWebsite());
        entry.setCreatorUserName(req.getUser().getUserName());
        entry.setCommentDays(req.getWebsite().getDefaultCommentDays());
        entry.setAllowComments(req.getWebsite().getDefaultAllowComments());
        entry.setStatus(req.isPublish() ? PubStatus.PUBLISHED : PubStatus.DRAFT);
    }

    /**
     * Persists the entry, flushes the session, and invalidates the page cache.
     */
    private void saveAndFlush(WeblogEntry entry) throws WebloggerException {
        WeblogEntryManager weblogMgr = weblogger.getWeblogEntryManager();
        weblogMgr.saveWeblogEntry(entry);
        weblogger.flush();
        try {
            flushPageCache(entry.getWebsite());
        } catch (Exception e) {
            mLogger.warn("Failed to flush page cache for " + entry.getWebsite().getHandle(), e);
        }
    }

    /**
     * Creates and saves a media file from the upload request. Returns the
     * response struct containing the file URL.
     */
    private Map<String, String> saveMediaFile(Weblog website, MediaUploadRequest uploadReq)
            throws WebloggerException, XmlRpcException {

        MediaFileManager fmgr = weblogger.getMediaFileManager();
        MediaFileDirectory root = fmgr.getDefaultMediaFileDirectory(website);

        MediaFile mf = new MediaFile();
        mf.setDirectory(root);
        mf.setWeblog(website);
        mf.setName(uploadReq.getName());
        mf.setContentType(uploadReq.getContentType());
        mf.setLength(uploadReq.getBits().length);

        try (InputStream inputStream = new ByteArrayInputStream(uploadReq.getBits())) {
            mf.setInputStream(inputStream);
            String fileLink = mf.getPermalink();

            RollerMessages errors = new RollerMessages();
            fmgr.createMediaFile(website, mf, errors);

            if (errors.getErrorCount() > 0) {
                throw new XmlRpcException(UPLOAD_DENIED_EXCEPTION, errors.toString());
            }

            weblogger.flush();

            Map<String, String> returnStruct = new HashMap<>();
            returnStruct.put(RESP_URL, fileLink);
            return returnStruct;
        } catch (XmlRpcException xe) {
            throw xe;
        } catch (java.io.IOException e) {
            throw new WebloggerException("Failed to process media upload stream", e);
        }
    }

    /**
     * Finds the first category name from the request list that actually exists
     * in the given weblog. Returns null if none match.
     */
    private String resolveFirstValidCategory(WeblogEntryManager weblogMgr,
            Weblog website, List<String> categoryNames) throws WebloggerException {

        for (String catName : categoryNames) {
            WeblogCategory resolved = weblogMgr.getWeblogCategoryByName(website, catName);
            if (resolved != null) {
                return catName;
            }
        }
        return null;
    }

    /**
     * Sets the category on an entry. If {@code categoryName} is non-null, resolves it;
     * otherwise falls back to {@code defaultCategory} (which may also be null for edits).
     */
    private void resolveAndSetCategory(WeblogEntryManager weblogMgr,
            WeblogEntry entry, Weblog website,
            String categoryName, WeblogCategory defaultCategory) throws WebloggerException {

        if (categoryName != null) {
            WeblogCategory resolved = weblogMgr.getWeblogCategoryByName(website, categoryName);
            if (resolved != null) {
                entry.setCategory(resolved);
                return;
            }
        }
        if (defaultCategory != null) {
            entry.setCategory(defaultCategory);
        }
    }
}
