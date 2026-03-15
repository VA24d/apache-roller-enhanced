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

import java.lang.reflect.Field;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.Collections;
import org.apache.commons.lang3.StringUtils;
import org.apache.roller.weblogger.business.MailProvider;
import org.apache.roller.weblogger.business.startup.WebloggerStartup;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.pojos.BugReport;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Optional real-SMTP smoke test.
 *
 * This test is disabled by default. Enable explicitly when you want to verify
 * end-to-end provider connectivity:
 *
 * mvn -pl app -Dtest=EmailBugNotificationSmtpSmokeTest \
 *   -Dbugreport.smtp.smoke=true -Dbugreport.smtp.to=you@example.com test
 */
@EnabledIfSystemProperty(named = "bugreport.smtp.smoke", matches = "true")
public class EmailBugNotificationSmtpSmokeTest {

    private static final String DEFAULT_SMTP_TEST_RECIPIENT = "naveenhardik12@gmail.com";

    private static Object originalMailProvider;
    private static final Map<String, String> originalConfigValues = new HashMap<>();
    private static Path mainConfigPath;

    @BeforeAll
    public static void setUp() throws Exception {
        Path appCustomConfig = Paths.get("src", "main", "resources", "roller-custom.properties")
            .toAbsolutePath();
        mainConfigPath = appCustomConfig;
        System.setProperty("roller.custom.config", appCustomConfig.toString());

        // WebloggerConfig is a static singleton and may already be initialized by earlier tests
        // with test defaults (mail.configurationType=jndi). Force SMTP values in-memory so this
        // smoke test is stable regardless of execution order.
        forceSmtpConfigForSmokeTest();

        Field mailProviderField = WebloggerStartup.class.getDeclaredField("mailProvider");
        mailProviderField.setAccessible(true);

        originalMailProvider = mailProviderField.get(null);
        mailProviderField.set(null, new MailProvider());
    }

    @AfterAll
    public static void tearDown() throws Exception {
        Field mailProviderField = WebloggerStartup.class.getDeclaredField("mailProvider");
        mailProviderField.setAccessible(true);
        mailProviderField.set(null, originalMailProvider);
        restoreOriginalConfigValues();
        System.clearProperty("roller.custom.config");
    }

    @Test
    public void sendsEmailUsingConfiguredSmtpProvider() throws Exception {
        String to = System.getProperty("bugreport.smtp.to", DEFAULT_SMTP_TEST_RECIPIENT);
        assertFalse(StringUtils.isBlank(to),
                "Set -Dbugreport.smtp.to=<recipient@domain> when running this smoke test");

        BugReport report = new BugReport();
        report.setTitle("SMTP Smoke Test");
        report.setDescription("Notification path smoke test");
        report.setReporterUserName("smtp-smoke-user");
        report.touch("smtp-smoke-user");

        BugReportEvent event = new BugReportEvent(BugReportEventType.CREATED, report, "smtp-smoke-user");
        BugNotificationMessage message = new BugNotificationMessage(
                "[Roller SMTP Smoke] Bug notification test",
                "If you received this email, Roller SMTP notification wiring is working.");

        EmailBugNotificationChannel channel = new EmailBugNotificationChannel();
        channel.notify(event, BugReportAudience.ADMINS, Collections.singletonList(to), message);
    }

    @SuppressWarnings("unchecked")
    private static void forceSmtpConfigForSmokeTest() throws Exception {
        Field configField = WebloggerConfig.class.getDeclaredField("config");
        configField.setAccessible(true);
        Properties config = (Properties) configField.get(null);

        Properties fileProps = new Properties();
        try (InputStream in = java.nio.file.Files.newInputStream(mainConfigPath)) {
            fileProps.load(in);
        }

        setConfigValue(config, fileProps, "mail.configurationType", "properties");
        setConfigValue(config, fileProps, "mail.hostname", "smtp.mailersend.net");
        setConfigValue(config, fileProps, "mail.port", "587");
        setConfigValue(config, fileProps, "mail.username", "");
        setConfigValue(config, fileProps, "mail.password", "");
    }

    private static void setConfigValue(Properties config, Properties fileProps, String key, String forcedValue) {
        originalConfigValues.put(key, config.getProperty(key));

        String valueFromFile = fileProps.getProperty(key);
        if (!StringUtils.isBlank(valueFromFile)) {
            config.setProperty(key, valueFromFile);
        } else {
            config.setProperty(key, forcedValue);
        }
    }

    private static void restoreOriginalConfigValues() throws Exception {
        Field configField = WebloggerConfig.class.getDeclaredField("config");
        configField.setAccessible(true);
        Properties config = (Properties) configField.get(null);

        for (Map.Entry<String, String> entry : originalConfigValues.entrySet()) {
            if (entry.getValue() == null) {
                config.remove(entry.getKey());
            } else {
                config.setProperty(entry.getKey(), entry.getValue());
            }
        }
        originalConfigValues.clear();
    }
}
