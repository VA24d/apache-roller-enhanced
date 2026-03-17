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
package org.apache.roller.selenium.editor;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.apache.roller.selenium.AbstractRollerPage;

/**
 * Base class for the new/edit entry pages
 */
public abstract class AbstractEntryPage extends AbstractRollerPage {

    public void setTitle(String value) {
        setFieldValue("entry_bean_title", value);
    }

    public void setText(String value) {
        // Roller uses either a plain textarea editor or a Summernote rich-text editor.
        // On CI/headless runs the rich-text editor is commonly enabled; setting the textarea
        // directly may be overwritten by the editor's submit sync, resulting in empty content.
        try {
            boolean hasSummernoteEditable = !driver.findElements(By.cssSelector(".note-editor .note-editable")).isEmpty();
            if (hasSummernoteEditable) {
                ((JavascriptExecutor) driver).executeScript(
                        "var textarea = document.getElementById('edit_content');"
                                + "if (!textarea) { return; }"
                                + "if (window.jQuery && jQuery(textarea).data('summernote')) {"
                                + "  jQuery(textarea).summernote('code', arguments[0]);"
                                + "} else {"
                                + "  textarea.value = arguments[0];"
                                + "  textarea.dispatchEvent(new Event('input', { bubbles: true }));"
                                + "  textarea.dispatchEvent(new Event('change', { bubbles: true }));"
                                + "}",
                        value);
                return;
            }
        } catch (RuntimeException ignored) {
            // Fall back to plain textarea interaction below.
        }

        setFieldValue("edit_content", value);
    }

    public void setSummary(String value) {
        setFieldValue("entry_bean_summary", value);
    }

    public EntryEditPage postBlogEntry() {
        clickById("entry_%{#mainAction}!publish");
        return new EntryEditPage(driver);
    }
}
