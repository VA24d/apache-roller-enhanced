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

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link MediaUploadRequest}.
 */
class MediaUploadRequestTest {

    @Test
    void fromStruct_extractsFields() {
        Map<String, Object> struct = new HashMap<>();
        struct.put("name", "photo.jpg");
        struct.put("type", "image/jpeg");
        struct.put("bits", new byte[]{1, 2, 3});

        MediaUploadRequest req = MediaUploadRequest.fromStruct(struct);

        assertEquals("photo.jpg", req.getName());
        assertEquals("image/jpeg", req.getContentType());
        assertArrayEquals(new byte[]{1, 2, 3}, req.getBits());
    }

    @Test
    void fromStruct_sanitizesSlashesInName() {
        Map<String, Object> struct = new HashMap<>();
        struct.put("name", "path/to/photo.jpg");
        struct.put("type", "image/jpeg");
        struct.put("bits", new byte[]{1});

        MediaUploadRequest req = MediaUploadRequest.fromStruct(struct);

        assertEquals("path_to_photo.jpg", req.getName());
    }

    @Test
    void constructor_throwsOnNullName() {
        assertThrows(IllegalArgumentException.class,
                () -> new MediaUploadRequest(null, "image/jpeg", new byte[]{1}));
    }

    @Test
    void constructor_throwsOnNullBits() {
        assertThrows(IllegalArgumentException.class,
                () -> new MediaUploadRequest("photo.jpg", "image/jpeg", null));
    }

    @Test
    void constructor_allowsNullContentType() {
        MediaUploadRequest req = new MediaUploadRequest("file.bin", null, new byte[]{1});
        assertNull(req.getContentType());
        assertEquals("file.bin", req.getName());
    }
}
