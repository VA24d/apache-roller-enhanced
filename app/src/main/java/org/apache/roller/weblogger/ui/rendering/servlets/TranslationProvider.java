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

import java.util.List;

/**
 * Strategy interface for translation providers.
 */
public interface TranslationProvider {

    /**
     * Translates a list of strings from the source language to the target language.
     * 
     * @param texts      The list of strings to translate.
     * @param sourceLang The source language code (e.g., "en"). Use "auto" if
     *                   supported by the provider.
     * @param targetLang The target language code (e.g., "hi").
     * @return A list of translated strings matching the order of the input list.
     * @throws Exception if translation fails.
     */
    List<String> translate(List<String> texts, String sourceLang, String targetLang) throws Exception;
}
