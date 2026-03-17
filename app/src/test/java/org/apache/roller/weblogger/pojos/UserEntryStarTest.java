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

package org.apache.roller.weblogger.pojos;

import java.sql.Timestamp;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Unit tests for UserEntryStar POJO.
 */
public class UserEntryStarTest {

    @Test
    public void testDefaultConstructor() {
        UserEntryStar star = new UserEntryStar();
        assertNotNull(star.getId(), "ID should be auto-generated");
        assertNull(star.getUser());
        assertNull(star.getEntry());
        assertNull(star.getStarredTime());
    }

    @Test
    public void testGettersAndSetters() {
        UserEntryStar star = new UserEntryStar();

        User user = new User();
        user.setUserName("testuser");
        star.setUser(user);
        assertEquals(user, star.getUser());

        WeblogEntry entry = new WeblogEntry();
        entry.setTitle("Test Entry");
        star.setEntry(entry);
        assertEquals(entry, star.getEntry());

        Timestamp ts = new Timestamp(System.currentTimeMillis());
        star.setStarredTime(ts);
        assertEquals(ts, star.getStarredTime());

        star.setId("custom-id");
        assertEquals("custom-id", star.getId());
    }

    @Test
    public void testEqualsAndHashCode() {
        User user = new User();
        user.setUserName("testuser");

        WeblogEntry entry = new WeblogEntry();
        entry.setAnchor("test-anchor");

        UserEntryStar star1 = new UserEntryStar();
        star1.setUser(user);
        star1.setEntry(entry);

        UserEntryStar star2 = new UserEntryStar();
        star2.setUser(user);
        star2.setEntry(entry);

        assertEquals(star1, star2);
        assertEquals(star1.hashCode(), star2.hashCode());
    }

    @Test
    public void testNotEquals() {
        User user1 = new User();
        user1.setUserName("user1");

        User user2 = new User();
        user2.setUserName("user2");

        WeblogEntry entry = new WeblogEntry();
        entry.setAnchor("test-anchor");

        UserEntryStar star1 = new UserEntryStar();
        star1.setUser(user1);
        star1.setEntry(entry);

        UserEntryStar star2 = new UserEntryStar();
        star2.setUser(user2);
        star2.setEntry(entry);

        assertNotEquals(star1, star2);
    }

    @Test
    public void testToString() {
        UserEntryStar star = new UserEntryStar();
        User user = new User();
        user.setUserName("testuser");
        star.setUser(user);

        WeblogEntry entry = new WeblogEntry();
        entry.setAnchor("my-entry");
        star.setEntry(entry);

        String str = star.toString();
        assertTrue(str.contains("testuser"));
        assertTrue(str.contains("my-entry"));
    }
}
