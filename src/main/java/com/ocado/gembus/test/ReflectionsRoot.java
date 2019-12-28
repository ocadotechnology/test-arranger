package com.ocado.gembus.test;

import java.io.InputStream;
import java.util.Properties;

public class ReflectionsRoot {

    private static final String key = "arranger.root";
    private static final String defaultRoot = "com.ocado";
    private static final String propertiesFile = "/arranger.properties";

    public static String getRootPackage() {
        String root;
        try {
            root = getPropertyAssertValuePresent(System.getProperties());
        } catch (Exception e) {
            try (final InputStream is = ReflectionsRoot.class.getResourceAsStream(propertiesFile)) {
                final Properties properties = new Properties();
                properties.load(is);
                root = getPropertyAssertValuePresent(properties);
            } catch (Exception ex) {
                root = defaultRoot;
            }
        }
        return root;
    }

    private static String getPropertyAssertValuePresent(Properties properties) {
        final String value = properties.getProperty(key);
        if(value == null) {
            throw new NullPointerException("Property " + key + " not found");
        }
        return value;
    }
}
