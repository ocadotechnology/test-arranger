/*
 * Copyright © 2020 Ocado (marian.jureczko@ocado.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
