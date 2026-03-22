package org.apache.roller.weblogger.ui.rendering.servlets;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.URLStrategy;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntry.PubStatus;
import org.apache.roller.weblogger.pojos.WeblogEntrySearchCriteria;
import org.apache.roller.weblogger.pojos.WeblogEntrySearchCriteria.SortBy;
import org.apache.roller.weblogger.pojos.WeblogEntrySearchCriteria.SortOrder;
import org.apache.roller.weblogger.util.Utilities;

/**
 * Loads QA documents from Roller managers.
 */
public class BusinessLayerWeblogQaEntryRepository implements WeblogQaEntryRepository {

    @Override
    public List<WeblogQaEntryDocument> getPublishedEntries(String weblogHandle) throws WebloggerException {
        WeblogManager weblogManager = WebloggerFactory.getWeblogger().getWeblogManager();
        Weblog weblog = weblogManager.getWeblogByHandle(weblogHandle, true);
        if (weblog == null) {
            throw new WebloggerException("Weblog not found for handle: " + weblogHandle);
        }

        WeblogEntrySearchCriteria criteria = new WeblogEntrySearchCriteria();
        criteria.setWeblog(weblog);
        criteria.setStatus(PubStatus.PUBLISHED);
        criteria.setMaxResults(-1);
        criteria.setSortBy(SortBy.PUBLICATION_TIME);
        criteria.setSortOrder(SortOrder.DESCENDING);

        WeblogEntryManager entryManager = WebloggerFactory.getWeblogger().getWeblogEntryManager();
        URLStrategy urlStrategy = WebloggerFactory.getWeblogger().getUrlStrategy();

        List<WeblogQaEntryDocument> documents = new ArrayList<>();
        for (WeblogEntry entry : entryManager.getWeblogEntries(criteria)) {
            String title = sanitize(entry.getTitle());
            String summary = sanitize(entry.getSummary());
            String content = sanitize(entry.getText());

            if (StringUtils.isBlank(title) && StringUtils.isBlank(summary) && StringUtils.isBlank(content)) {
                continue;
            }

            documents.add(new WeblogQaEntryDocument(
                    entry.getId(),
                    title,
                    summary,
                    content,
                    entry.getPermalink(urlStrategy),
                    entry.getPubTime()));
        }

        return documents;
    }

    private String sanitize(String value) {
        if (StringUtils.isBlank(value)) {
            return "";
        }
        return Utilities.removeHTML(value)
                .replace('\u00A0', ' ')
                .replaceAll("\\s+", " ")
                .trim();
    }
}
