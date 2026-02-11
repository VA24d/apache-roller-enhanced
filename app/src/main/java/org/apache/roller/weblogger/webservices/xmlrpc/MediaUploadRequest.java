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

/**
 * Parameter object encapsulating binary media upload metadata.
 *
 * <p>Replaces raw {@code Hashtable} field extraction in
 * {@code MetaWeblogAPIHandler.newMediaObject()} with typed access.</p>
 */
public class MediaUploadRequest {

    /** Struct field name for the file name. */
    static final String FIELD_NAME = "name";
    /** Struct field name for the MIME type. */
    static final String FIELD_TYPE = "type";
    /** Struct field name for the binary content. */
    static final String FIELD_BITS = "bits";

    private final String name;
    private final String contentType;
    private final byte[] bits;

    public MediaUploadRequest(String name, String contentType, byte[] bits) {
        if (name == null || bits == null) {
            throw new IllegalArgumentException("name and bits must not be null");
        }
        // Sanitize path separators in the filename
        this.name = name.replace("/", "_");
        this.contentType = contentType;
        this.bits = bits;
    }

    /**
     * Factory that builds a request from the raw XML-RPC struct.
     */
    public static MediaUploadRequest fromStruct(java.util.Map<String, ?> struct) {
        String name = (String) struct.get(FIELD_NAME);
        String type = (String) struct.get(FIELD_TYPE);
        byte[] bits = (byte[]) struct.get(FIELD_BITS);
        return new MediaUploadRequest(name, type, bits);
    }

    public String getName() {
        return name;
    }

    public String getContentType() {
        return contentType;
    }

    public byte[] getBits() {
        return bits;
    }
}
