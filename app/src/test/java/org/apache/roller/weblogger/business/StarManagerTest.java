/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
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

package org.apache.roller.weblogger.business;

import java.sql.Timestamp;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.UserEntryStar;
import org.apache.roller.weblogger.pojos.UserWeblogStar;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Tests for StarManager — star (favourite) operations on weblogs and entries.
 */
public class StarManagerTest {

    private static final Log LOG = LogFactory.getLog(StarManagerTest.class);

    private User testUser = null;
    private Weblog testWeblog = null;


    @BeforeEach
    public void setUp() throws Exception {
        TestUtils.setupWeblogger();

        try {
            testUser = TestUtils.setupUser("starTestUser");
            testWeblog = TestUtils.setupWeblog("starTestWeblog", testUser);
            TestUtils.endSession(true);
        } catch (Exception ex) {
            LOG.error(ex);
            throw new Exception("Test setup failed", ex);
        }
    }


    @AfterEach
    public void tearDown() throws Exception {
        try {
            TestUtils.teardownWeblog(testWeblog.getId());
            TestUtils.teardownUser(testUser.getUserName());
            TestUtils.endSession(true);
        } catch (Exception ex) {
            LOG.error(ex);
            throw new Exception("Test teardown failed", ex);
        }
    }


    /**
     * Test basic weblog star CRUD operations.
     */
    @Test
    public void testWeblogStarCRUD() throws Exception {
        StarManager mgr = WebloggerFactory.getWeblogger().getStarManager();

        testUser = TestUtils.getManagedUser(testUser);
        testWeblog = TestUtils.getManagedWebsite(testWeblog);

        // create
        UserWeblogStar star = new UserWeblogStar();
        star.setUser(testUser);
        star.setWeblog(testWeblog);
        star.setStarredTime(new Timestamp(System.currentTimeMillis()));
        mgr.saveWeblogStar(star);
        String id = star.getId();
        TestUtils.endSession(true);

        // read by id
        UserWeblogStar fetched = mgr.getWeblogStar(id);
        assertNotNull(fetched);
        assertEquals(id, fetched.getId());

        // read by user+weblog
        testUser = TestUtils.getManagedUser(testUser);
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        UserWeblogStar byCombo = mgr.getWeblogStarByUserAndWeblog(testUser, testWeblog);
        assertNotNull(byCombo);
        assertEquals(id, byCombo.getId());

        // delete
        mgr.removeWeblogStar(byCombo);
        TestUtils.endSession(true);

        // verify deleted
        assertNull(mgr.getWeblogStar(id));
    }


    /**
     * Test basic entry star CRUD operations.
     */
    @Test
    public void testEntryStarCRUD() throws Exception {
        StarManager mgr = WebloggerFactory.getWeblogger().getStarManager();

        testUser = TestUtils.getManagedUser(testUser);
        testWeblog = TestUtils.getManagedWebsite(testWeblog);

        // setup a weblog entry
        WeblogEntry entry = TestUtils.setupWeblogEntry("starTestEntry",
                testWeblog, testUser);
        TestUtils.endSession(true);

        testUser = TestUtils.getManagedUser(testUser);
        WeblogEntryManager entryMgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();
        entry = entryMgr.getWeblogEntry(entry.getId());

        // create star
        UserEntryStar star = new UserEntryStar();
        star.setUser(testUser);
        star.setEntry(entry);
        star.setStarredTime(new Timestamp(System.currentTimeMillis()));
        mgr.saveEntryStar(star);
        String id = star.getId();
        TestUtils.endSession(true);

        // read by id
        UserEntryStar fetched = mgr.getEntryStar(id);
        assertNotNull(fetched);
        assertEquals(id, fetched.getId());

        // read by user+entry
        testUser = TestUtils.getManagedUser(testUser);
        entry = entryMgr.getWeblogEntry(entry.getId());
        UserEntryStar byCombo = mgr.getEntryStarByUserAndEntry(testUser, entry);
        assertNotNull(byCombo);
        assertEquals(id, byCombo.getId());

        // delete
        mgr.removeEntryStar(byCombo);
        TestUtils.endSession(true);

        // verify deleted
        assertNull(mgr.getEntryStar(id));

        // cleanup entry
        TestUtils.teardownWeblogEntry(entry.getId());
        TestUtils.endSession(true);
    }


    /**
     * Test starred entries pagination.
     */
    @Test
    public void testStarredEntriesPagination() throws Exception {
        StarManager mgr = WebloggerFactory.getWeblogger().getStarManager();

        testUser = TestUtils.getManagedUser(testUser);
        testWeblog = TestUtils.getManagedWebsite(testWeblog);

        // create 3 entries and star them
        WeblogEntry e1 = TestUtils.setupWeblogEntry("starPagEntry1", testWeblog, testUser);
        WeblogEntry e2 = TestUtils.setupWeblogEntry("starPagEntry2", testWeblog, testUser);
        WeblogEntry e3 = TestUtils.setupWeblogEntry("starPagEntry3", testWeblog, testUser);
        TestUtils.endSession(true);

        testUser = TestUtils.getManagedUser(testUser);
        WeblogEntryManager entryMgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();
        e1 = entryMgr.getWeblogEntry(e1.getId());
        e2 = entryMgr.getWeblogEntry(e2.getId());
        e3 = entryMgr.getWeblogEntry(e3.getId());

        UserEntryStar s1 = new UserEntryStar();
        s1.setUser(testUser); s1.setEntry(e1);
        s1.setStarredTime(new Timestamp(System.currentTimeMillis() - 3000));
        mgr.saveEntryStar(s1);

        UserEntryStar s2 = new UserEntryStar();
        s2.setUser(testUser); s2.setEntry(e2);
        s2.setStarredTime(new Timestamp(System.currentTimeMillis() - 2000));
        mgr.saveEntryStar(s2);

        UserEntryStar s3 = new UserEntryStar();
        s3.setUser(testUser); s3.setEntry(e3);
        s3.setStarredTime(new Timestamp(System.currentTimeMillis() - 1000));
        mgr.saveEntryStar(s3);
        TestUtils.endSession(true);

        testUser = TestUtils.getManagedUser(testUser);

        // all 3
        List<UserEntryStar> all = mgr.getStarredEntries(testUser, 0, -1);
        assertEquals(3, all.size());

        // page of 2
        List<UserEntryStar> page1 = mgr.getStarredEntries(testUser, 0, 2);
        assertEquals(2, page1.size());

        // page 2 (offset 2, length 2)
        List<UserEntryStar> page2 = mgr.getStarredEntries(testUser, 2, 2);
        assertEquals(1, page2.size());

        // cleanup
        mgr.removeEntryStar(mgr.getEntryStar(s1.getId()));
        mgr.removeEntryStar(mgr.getEntryStar(s2.getId()));
        mgr.removeEntryStar(mgr.getEntryStar(s3.getId()));
        TestUtils.endSession(true);

        TestUtils.teardownWeblogEntry(e1.getId());
        TestUtils.teardownWeblogEntry(e2.getId());
        TestUtils.teardownWeblogEntry(e3.getId());
        TestUtils.endSession(true);
    }


    /**
     * Test trending weblogs — uses aggregate query (GROUP BY + COUNT).
     */
    @Test
    public void testTrendingWeblogs() throws Exception {
        StarManager mgr = WebloggerFactory.getWeblogger().getStarManager();

        // create additional users and weblogs
        User user2 = TestUtils.setupUser("starTrendUser2");
        User user3 = TestUtils.setupUser("starTrendUser3");
        testUser = TestUtils.getManagedUser(testUser);
        Weblog blog1 = TestUtils.setupWeblog("starTrendBlog1", testUser);
        Weblog blog2 = TestUtils.setupWeblog("starTrendBlog2", testUser);
        TestUtils.endSession(true);

        testUser = TestUtils.getManagedUser(testUser);
        user2 = TestUtils.getManagedUser(user2);
        user3 = TestUtils.getManagedUser(user3);
        blog1 = TestUtils.getManagedWebsite(blog1);
        blog2 = TestUtils.getManagedWebsite(blog2);

        // blog1 gets 2 stars, blog2 gets 1 star
        UserWeblogStar ws1 = new UserWeblogStar();
        ws1.setUser(testUser); ws1.setWeblog(blog1);
        ws1.setStarredTime(new Timestamp(System.currentTimeMillis()));
        mgr.saveWeblogStar(ws1);

        UserWeblogStar ws2 = new UserWeblogStar();
        ws2.setUser(user2); ws2.setWeblog(blog1);
        ws2.setStarredTime(new Timestamp(System.currentTimeMillis()));
        mgr.saveWeblogStar(ws2);

        UserWeblogStar ws3 = new UserWeblogStar();
        ws3.setUser(user3); ws3.setWeblog(blog2);
        ws3.setStarredTime(new Timestamp(System.currentTimeMillis()));
        mgr.saveWeblogStar(ws3);
        TestUtils.endSession(true);

        // trending query — efficient aggregate
        List<Object[]> trending = mgr.getTrendingWeblogs(5);
        assertNotNull(trending);
        assertTrue(trending.size() >= 2);

        // first result should have highest count
        long firstCount = (Long) trending.get(0)[1];
        long secondCount = (Long) trending.get(1)[1];
        assertTrue(firstCount >= secondCount, "Trending should be ordered by count descending");

        // star counts
        blog1 = TestUtils.getManagedWebsite(blog1);
        assertEquals(2, mgr.getWeblogStarCount(blog1));
        blog2 = TestUtils.getManagedWebsite(blog2);
        assertEquals(1, mgr.getWeblogStarCount(blog2));

        // cleanup
        mgr.removeWeblogStar(mgr.getWeblogStar(ws1.getId()));
        mgr.removeWeblogStar(mgr.getWeblogStar(ws2.getId()));
        mgr.removeWeblogStar(mgr.getWeblogStar(ws3.getId()));
        TestUtils.endSession(true);

        TestUtils.teardownWeblog(blog1.getId());
        TestUtils.teardownWeblog(blog2.getId());
        TestUtils.teardownUser(user2.getUserName());
        TestUtils.teardownUser(user3.getUserName());
        TestUtils.endSession(true);
    }


    /**
     * Test trending entries — uses aggregate query (GROUP BY + COUNT).
     */
    @Test
    public void testTrendingEntries() throws Exception {
        StarManager mgr = WebloggerFactory.getWeblogger().getStarManager();

        User user2 = TestUtils.setupUser("starTrendEUser2");
        testUser = TestUtils.getManagedUser(testUser);
        testWeblog = TestUtils.getManagedWebsite(testWeblog);

        WeblogEntry entry1 = TestUtils.setupWeblogEntry("starTrendE1", testWeblog, testUser);
        WeblogEntry entry2 = TestUtils.setupWeblogEntry("starTrendE2", testWeblog, testUser);
        TestUtils.endSession(true);

        testUser = TestUtils.getManagedUser(testUser);
        user2 = TestUtils.getManagedUser(user2);
        WeblogEntryManager entryMgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();
        entry1 = entryMgr.getWeblogEntry(entry1.getId());
        entry2 = entryMgr.getWeblogEntry(entry2.getId());

        // entry1 gets 2 stars, entry2 gets 1 star
        UserEntryStar es1 = new UserEntryStar();
        es1.setUser(testUser); es1.setEntry(entry1);
        es1.setStarredTime(new Timestamp(System.currentTimeMillis()));
        mgr.saveEntryStar(es1);

        UserEntryStar es2 = new UserEntryStar();
        es2.setUser(user2); es2.setEntry(entry1);
        es2.setStarredTime(new Timestamp(System.currentTimeMillis()));
        mgr.saveEntryStar(es2);

        UserEntryStar es3 = new UserEntryStar();
        es3.setUser(testUser); es3.setEntry(entry2);
        es3.setStarredTime(new Timestamp(System.currentTimeMillis()));
        mgr.saveEntryStar(es3);
        TestUtils.endSession(true);

        // trending query — efficient aggregate
        List<Object[]> trending = mgr.getTrendingEntries(5);
        assertNotNull(trending);
        assertTrue(trending.size() >= 2);

        // first result should have highest count
        long firstCount = (Long) trending.get(0)[1];
        long secondCount = (Long) trending.get(1)[1];
        assertTrue(firstCount >= secondCount, "Trending should be ordered by count descending");

        // star counts
        entry1 = entryMgr.getWeblogEntry(entry1.getId());
        assertEquals(2, mgr.getEntryStarCount(entry1));
        entry2 = entryMgr.getWeblogEntry(entry2.getId());
        assertEquals(1, mgr.getEntryStarCount(entry2));

        // cleanup
        mgr.removeEntryStar(mgr.getEntryStar(es1.getId()));
        mgr.removeEntryStar(mgr.getEntryStar(es2.getId()));
        mgr.removeEntryStar(mgr.getEntryStar(es3.getId()));
        TestUtils.endSession(true);

        TestUtils.teardownWeblogEntry(entry1.getId());
        TestUtils.teardownWeblogEntry(entry2.getId());
        TestUtils.teardownUser(user2.getUserName());
        TestUtils.endSession(true);
    }
}
