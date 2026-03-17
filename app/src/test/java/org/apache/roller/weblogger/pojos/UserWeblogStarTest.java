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
 * Unit tests for UserWeblogStar POJO.
 */
public class UserWeblogStarTest {

    @Test
    public void testDefaultConstructor() {
        UserWeblogStar star = new UserWeblogStar();
        assertNotNull(star.getId(), "ID should be auto-generated");
        assertNull(star.getUser());
        assertNull(star.getWeblog());
        assertNull(star.getStarredTime());
    }

    @Test
    public void testGettersAndSetters() {
        UserWeblogStar star = new UserWeblogStar();

        User user = new User();
        user.setUserName("testuser");
        star.setUser(user);
        assertEquals(user, star.getUser());

        Weblog weblog = new Weblog();
        weblog.setHandle("testblog");
        star.setWeblog(weblog);
        assertEquals(weblog, star.getWeblog());

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

        Weblog weblog = new Weblog();
        weblog.setHandle("testblog");

        UserWeblogStar star1 = new UserWeblogStar();
        star1.setUser(user);
        star1.setWeblog(weblog);

        UserWeblogStar star2 = new UserWeblogStar();
        star2.setUser(user);
        star2.setWeblog(weblog);

        assertEquals(star1, star2);
        assertEquals(star1.hashCode(), star2.hashCode());
    }

    @Test
    public void testNotEquals() {
        User user1 = new User();
        user1.setUserName("user1");

        User user2 = new User();
        user2.setUserName("user2");

        Weblog weblog = new Weblog();
        weblog.setHandle("testblog");

        UserWeblogStar star1 = new UserWeblogStar();
        star1.setUser(user1);
        star1.setWeblog(weblog);

        UserWeblogStar star2 = new UserWeblogStar();
        star2.setUser(user2);
        star2.setWeblog(weblog);

        assertNotEquals(star1, star2);
    }

    @Test
    public void testToString() {
        UserWeblogStar star = new UserWeblogStar();
        User user = new User();
        user.setUserName("testuser");
        star.setUser(user);

        Weblog weblog = new Weblog();
        weblog.setHandle("testblog");
        star.setWeblog(weblog);

        String str = star.toString();
        assertTrue(str.contains("testuser"));
        assertTrue(str.contains("testblog"));
    }
}
