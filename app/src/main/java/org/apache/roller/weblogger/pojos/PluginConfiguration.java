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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

/**
 * Immutable value object representing the plugin configuration for a weblog entry.
 * Encapsulates the comma-delimited plugin string and provides structured access.
 *
 * <p>Replaces raw String "plugins" field to eliminate Primitive Obsession
 * and centralize parsing/validation logic.</p>
 */
public final class PluginConfiguration implements Serializable {

    private static final long serialVersionUID = 1L;

    /** The empty plugin configuration (no plugins enabled). */
    public static final PluginConfiguration EMPTY = new PluginConfiguration(null);

    private final String raw;
    private final List<String> pluginNames;

    /**
     * Create a PluginConfiguration from a comma-delimited plugin string.
     * @param commaSeparatedPlugins comma-delimited list, or null/empty for none
     */
    public PluginConfiguration(String commaSeparatedPlugins) {
        this.raw = commaSeparatedPlugins;
        if (commaSeparatedPlugins != null && !commaSeparatedPlugins.isBlank()) {
            this.pluginNames = Collections.unmodifiableList(
                    Arrays.asList(StringUtils.split(commaSeparatedPlugins, ",")));
        } else {
            this.pluginNames = Collections.emptyList();
        }
    }

    /**
     * Create a PluginConfiguration from an explicit list of plugin names.
     * @param plugins list of plugin names
     * @return a new immutable PluginConfiguration
     */
    public static PluginConfiguration of(List<String> plugins) {
        if (plugins == null || plugins.isEmpty()) {
            return EMPTY;
        }
        return new PluginConfiguration(String.join(",", plugins));
    }

    /**
     * @return the raw comma-delimited string, or null if no plugins configured
     */
    public String getRaw() {
        return raw;
    }

    /**
     * @return unmodifiable list of plugin names; never null
     */
    public List<String> getPluginNames() {
        return pluginNames;
    }

    /**
     * @return true if the given plugin name is enabled in this configuration
     */
    public boolean containsPlugin(String pluginName) {
        return pluginNames.contains(pluginName);
    }

    /**
     * @return true if no plugins are configured
     */
    public boolean isEmpty() {
        return pluginNames.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PluginConfiguration)) return false;
        PluginConfiguration that = (PluginConfiguration) o;
        return Objects.equals(raw, that.raw);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(raw);
    }

    @Override
    public String toString() {
        return raw == null ? "" : raw;
    }
}
