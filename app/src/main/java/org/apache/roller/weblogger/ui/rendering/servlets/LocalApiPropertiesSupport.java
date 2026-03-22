package org.apache.roller.weblogger.ui.rendering.servlets;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.config.WebloggerConfig;

/**
 * Loads API keys from Roller config first, then from the local untracked
 * translation-api.properties file.
 */
public final class LocalApiPropertiesSupport {

    private static final Log log = LogFactory.getLog(LocalApiPropertiesSupport.class);
    private static volatile Properties localProperties;

    private LocalApiPropertiesSupport() {
    }

    public static String getProperty(String primaryKey, String... fallbackKeys) {
        String configured = findConfiguredValue(primaryKey, fallbackKeys);
        if (StringUtils.isNotBlank(configured)) {
            return configured;
        }

        Properties properties = getLocalProperties();
        configured = findProperty(properties, primaryKey);
        if (StringUtils.isNotBlank(configured)) {
            return configured;
        }

        for (String fallbackKey : fallbackKeys) {
            configured = findProperty(properties, fallbackKey);
            if (StringUtils.isNotBlank(configured)) {
                return configured;
            }
        }

        return null;
    }

    private static String findConfiguredValue(String primaryKey, String... fallbackKeys) {
        String configured = WebloggerConfig.getProperty(primaryKey);
        if (StringUtils.isNotBlank(configured)) {
            return configured;
        }

        for (String fallbackKey : fallbackKeys) {
            configured = WebloggerConfig.getProperty(fallbackKey);
            if (StringUtils.isNotBlank(configured)) {
                return configured;
            }
        }

        return null;
    }

    private static String findProperty(Properties properties, String key) {
        if (properties == null || StringUtils.isBlank(key)) {
            return null;
        }
        return properties.getProperty(key);
    }

    private static Properties getLocalProperties() {
        Properties snapshot = localProperties;
        if (snapshot != null) {
            return snapshot;
        }

        synchronized (LocalApiPropertiesSupport.class) {
            if (localProperties == null) {
                localProperties = loadLocalProperties();
            }
            return localProperties;
        }
    }

    private static Properties loadLocalProperties() {
        File propertyFile = resolvePropertyFile();
        Properties props = new Properties();
        if (propertyFile == null || !propertyFile.exists()) {
            return props;
        }

        try (InputStream inputStream = new FileInputStream(propertyFile)) {
            props.load(inputStream);
        } catch (Exception e) {
            log.warn("Could not load translation-api.properties", e);
        }
        return props;
    }

    private static File resolvePropertyFile() {
        String userDir = System.getProperty("user.dir");
        File propertyFile = new File(userDir, "translation-api.properties");
        if (propertyFile.exists()) {
            return propertyFile;
        }

        propertyFile = new File(userDir, "../translation-api.properties");
        return propertyFile.exists() ? propertyFile : null;
    }
}
