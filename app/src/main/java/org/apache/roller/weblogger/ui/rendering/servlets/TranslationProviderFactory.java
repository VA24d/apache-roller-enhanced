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

package org.apache.roller.weblogger.ui.rendering.servlets;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.config.WebloggerConfig;

/**
 * Factory for creating TranslationProvider instances.
 */
public class TranslationProviderFactory {

    private static final Log log = LogFactory.getLog(TranslationProviderFactory.class);

    public static TranslationProvider getProvider(String providerName) {
        if (providerName == null || providerName.trim().isEmpty()) {
            providerName = WebloggerConfig.getProperty("translation.default.provider", "mymemory");
        }

        providerName = providerName.toLowerCase().trim();

        if ("sarvam".equals(providerName)) {
            return new SarvamTranslationProvider();
        } else if ("mymemory".equals(providerName)) {
            return new MyMemoryTranslationProvider();
        }

        log.warn("Unknown translation provider: " + providerName + ". Falling back to MyMemory.");
        return new MyMemoryTranslationProvider();
    }
}
