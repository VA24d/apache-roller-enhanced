package org.apache.roller.weblogger.pojos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.UserManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class WeblogEntryTest {

    private WeblogEntry entry;
    private Weblog website;
    private User user;
    private UserManager userManager;

    @BeforeEach
    public void setUp() {
        entry = new WeblogEntry();
        website = mock(Weblog.class);
        user = mock(User.class);
        userManager = mock(UserManager.class);

        entry.setWebsite(website);
        entry.setCreator(user);
    }

    @Test
    public void testBuilder() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        WeblogEntry builtEntry = new WeblogEntry.Builder()
                .title("Builder Title")
                .text("Builder Text")
                .pubTime(now)
                .status(WeblogEntry.PubStatus.PUBLISHED)
                .build();

        assertEquals("Builder Title", builtEntry.getTitle());
        assertEquals("Builder Text", builtEntry.getText());
        assertEquals(now, builtEntry.getPubTime());
        assertEquals(WeblogEntry.PubStatus.PUBLISHED, builtEntry.getStatus());
    }

    @Test
    public void testCreateAnchorBase_FromTitle() {
        entry.setTitle("My First Blog Post 2026");
        String anchor = entry.createAnchorBase();
        // createAnchorBase logic: lowercase, alphanumeric spaced with separator, max 5 words
        // "My" "First" "Blog" "Post" "2026"
        assertEquals("my-first-blog-post-2026", anchor);
    }

    @Test
    public void testCreateAnchorBase_FromTitleTruncated() {
        entry.setTitle("This is a very long title that should be truncated");
        String anchor = entry.createAnchorBase();
        // 5 words: this, is, a, very, long
        assertEquals("this-is-a-very-long", anchor);
    }

    @Test
    public void testCreateAnchorBase_FromTextFallback() {
        entry.setTitle(null);
        entry.setText("This is some text content for the blog entry.");
        String anchor = entry.createAnchorBase();
        assertEquals("this-is-some-text-content", anchor);
    }
    
    @Test
    public void testGetCommentsStillAllowed_GlobalDisabled() {
        assertFalse(entry.getCommentsStillAllowed(false));
    }

    @Test
    public void testGetCommentsStillAllowed_WebsiteDisabled() {
        when(website.getAllowComments()).thenReturn(false);
        assertFalse(entry.getCommentsStillAllowed(true));
    }

    @Test
    public void testGetCommentsStillAllowed_EntryDisabled() {
        when(website.getAllowComments()).thenReturn(true);
        entry.setAllowComments(false);
        assertFalse(entry.getCommentsStillAllowed(true));
    }

    @Test
    public void testGetCommentsStillAllowed_UnlimitedDays() {
        when(website.getAllowComments()).thenReturn(true);
        entry.setAllowComments(true);
        entry.setCommentDays(0);
        assertTrue(entry.getCommentsStillAllowed(true));
    }

    @Test
    public void testGetCommentsStillAllowed_Expired() {
        when(website.getAllowComments()).thenReturn(true);
        when(website.getLocaleInstance()).thenReturn(java.util.Locale.US);
        entry.setAllowComments(true);
        entry.setCommentDays(10);
        
        // 20 days ago
        Instant past = Instant.now().minus(20, ChronoUnit.DAYS);
        entry.setPubTime(Timestamp.from(past));
        
        assertFalse(entry.getCommentsStillAllowed(true));
    }

    @Test
    public void testGetCommentsStillAllowed_Active() {
        when(website.getAllowComments()).thenReturn(true);
        when(website.getLocaleInstance()).thenReturn(java.util.Locale.US);
        entry.setAllowComments(true);
        entry.setCommentDays(10);
        
        // 5 days ago (within 10 day window)
        Instant recent = Instant.now().minus(5, ChronoUnit.DAYS);
        entry.setPubTime(Timestamp.from(recent));
        
        assertTrue(entry.getCommentsStillAllowed(true));
    }

    @Test
    public void testHasWritePermissions_GlobalAdmin() throws WebloggerException {
        // Mock permission check for Global Admin
        when(userManager.checkPermission(any(GlobalPermission.class), eq(user))).thenReturn(true);

        assertTrue(entry.hasWritePermissions(user, userManager));
    }

    @Test
    public void testHasWritePermissions_Author() throws WebloggerException {
        // Not admin
        when(userManager.checkPermission(any(GlobalPermission.class), eq(user))).thenReturn(false);
        
        WeblogPermission perm = mock(WeblogPermission.class);
        when(perm.hasAction(WeblogPermission.POST)).thenReturn(true);
        when(userManager.getWeblogPermission(website, user)).thenReturn(perm);

        assertTrue(entry.hasWritePermissions(user, userManager));
    }

    @Test
    public void testHasWritePermissions_Limited_Draft() throws WebloggerException {
        // Not admin
        when(userManager.checkPermission(any(GlobalPermission.class), eq(user))).thenReturn(false);
        
        WeblogPermission perm = mock(WeblogPermission.class);
        when(perm.hasAction(WeblogPermission.POST)).thenReturn(false); // Not author
        when(perm.hasAction(WeblogPermission.ADMIN)).thenReturn(false);
        when(perm.hasAction(WeblogPermission.EDIT_DRAFT)).thenReturn(true); // Limited
        
        when(userManager.getWeblogPermission(website, user)).thenReturn(perm);

        entry.setStatus(WeblogEntry.PubStatus.DRAFT);
        assertTrue(entry.hasWritePermissions(user, userManager));
    }

    @Test
    public void testHasWritePermissions_Limited_Published() throws WebloggerException {
        // Not admin
        when(userManager.checkPermission(any(GlobalPermission.class), eq(user))).thenReturn(false);
        
        WeblogPermission perm = mock(WeblogPermission.class);
        when(perm.hasAction(WeblogPermission.POST)).thenReturn(false);
        when(perm.hasAction(WeblogPermission.ADMIN)).thenReturn(false);
        when(perm.hasAction(WeblogPermission.EDIT_DRAFT)).thenReturn(true);
        
        when(userManager.getWeblogPermission(website, user)).thenReturn(perm);

        entry.setStatus(WeblogEntry.PubStatus.PUBLISHED);
        assertFalse(entry.hasWritePermissions(user, userManager));
    }

    @Test
    public void testWeblogEntryAttributeSerialization() throws Exception {
        WeblogEntryAttribute att = new WeblogEntryAttribute();
        att.setId("test-id");
        att.setName("foo");
        att.setValue("bar");
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(att);
        oos.close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        WeblogEntryAttribute deserialized = (WeblogEntryAttribute) ois.readObject();
        
        assertNotNull(deserialized);
        assertEquals("test-id", deserialized.getId());
        assertEquals("foo", deserialized.getName());
    }

    @Test
    public void testGetRelativePermalink() {
        when(website.getHandle()).thenReturn("myblog");
        entry.setAnchor("my-post-anchor");
        
        // Expected: /<handle>/entry/<encoded_anchor>
        String expected = "/myblog/entry/my-post-anchor";
        assertEquals(expected, entry.getRelativePermalink());
    }
}
