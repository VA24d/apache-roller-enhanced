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
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.util.MailUtil;

public class EmailBugNotificationChannel implements BugNotificationChannel {

    private static final Log LOG = LogFactory.getLog(EmailBugNotificationChannel.class);

    @Override
    public String id() {
        return "email";
    }

    @Override
    public void notify(BugReportEvent event, BugReportAudience audience, List<String> recipients,
                       BugNotificationMessage message) throws Exception {
        if (!MailUtil.isMailConfigured()) {
            LOG.info("Mail not configured; skipping bug notification email for event " + event.getEventType());
            return;
        }
        if (recipients == null || recipients.isEmpty()) {
            return;
        }

        // site.adminemail is a DB-backed runtime property — set it via Admin -> Global Config.
        // Fall back to mail.username (static/classpath property) so local dev works without
        // touching the admin UI.
        String from = WebloggerRuntimeConfig.getProperty("site.adminemail");
        if (from == null || from.isBlank()) {
            from = WebloggerConfig.getProperty("mail.username");
        }
        if (from == null || from.isBlank()) {
            from = "noreply@localhost";
        }
        LOG.info("Bug notification email: from=" + from + ", recipients=" + recipients
                + ", event=" + event.getEventType());

        List<String> valid = new ArrayList<>();
        for (String recipient : recipients) {
            if (recipient != null && !recipient.isBlank()) {
                valid.add(recipient);
            }
        }

        if (valid.isEmpty()) {
            return;
        }

        MailUtil.sendTextMessage(from, valid.toArray(String[]::new), null, null,
                message.getSubject(), message.getBody());
    }
}
