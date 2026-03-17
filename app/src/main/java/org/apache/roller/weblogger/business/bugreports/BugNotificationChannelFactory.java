/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 */

package org.apache.roller.weblogger.business.bugreports;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;

/**
 * Factory for resolving channel implementations from runtime config.
 */
public class BugNotificationChannelFactory {

    private static final String CHANNELS_PROPERTY = "bugreport.notification.channels";

    public List<BugNotificationChannel> createChannels() {
        String configuredChannels = WebloggerRuntimeConfig.getProperty(CHANNELS_PROPERTY);
        if (configuredChannels == null || configuredChannels.isBlank()) {
            configuredChannels = "email";
        }

        List<BugNotificationChannel> channels = new ArrayList<>();
        for (String token : configuredChannels.split(",")) {
            String channelId = token.trim().toLowerCase(Locale.ROOT);
            if ("email".equals(channelId)) {
                channels.add(new EmailBugNotificationChannel());
            }
            // Future extensions such as Slack or Teams can be added here.
        }

        return Collections.unmodifiableList(channels);
    }
}
