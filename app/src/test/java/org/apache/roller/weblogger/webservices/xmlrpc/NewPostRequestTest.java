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

import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;

/**
 * Unit tests for {@link NewPostRequest} parameter object.
 */
class NewPostRequestTest {

    @Test
    void constructorAndGetters_returnCorrectValues() {
        Weblog website = mock(Weblog.class);
        User user = mock(User.class);
        Date dateCreated = new Date(1000000L);
        Timestamp updateTime = new Timestamp(2000000L);

        NewPostRequest req = new NewPostRequest(
                "Title", "Desc", website, user, dateCreated, updateTime, true);

        assertEquals("Title", req.getTitle());
        assertEquals("Desc", req.getDescription());
        assertSame(website, req.getWebsite());
        assertSame(user, req.getUser());
        assertEquals(dateCreated, req.getDateCreated());
        assertEquals(updateTime, req.getUpdateTime());
        assertTrue(req.isPublish());
    }

    @Test
    void isPublish_returnsFalse_whenNotPublished() {
        Weblog website = mock(Weblog.class);
        User user = mock(User.class);

        NewPostRequest req = new NewPostRequest(
                "T", "D", website, user, new Date(), new Timestamp(0), false);

        assertFalse(req.isPublish());
    }
}
