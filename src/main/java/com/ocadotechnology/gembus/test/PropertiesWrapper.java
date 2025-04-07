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
package com.ocadotechnology.gembus.test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PropertiesWrapper {

    private static final String rootKey = "arranger.root";
    private static final String defaultRoot = "com.ocado";
    private static final String cacheKey = "arranger.cache.enable";
    private static final String defaultCache = "false";
    private static final String randomSeedKey = "arranger.randomseed";
    private static final String defaultRandomSeed = "false";
    private static final String overrideDefaults = "arranger.overridedefaults";
    private static final String defaultOverrideDefaults = "false";
    static final String maxRandomizationDepth = "arranger.maxRandomizationDepth";
    private static final String androidCustomArrangers = "arranger.android.customArrangers";
    private static final String defaultAndroidCustomArrangers = "";
    private static final String defaultMaxRandomizationDepth = "4";
    private static final PropertiesFromFile propertiesFromFile = new PropertiesFromFile();

    public static String getRootPackage() {
        return getPropertyValue(rootKey, defaultRoot);
    }

    public static boolean getCacheEnable() {
        return Boolean.parseBoolean(getPropertyValue(cacheKey, defaultCache));
    }

    public static boolean getRandomSeedEnabled() {
        return Boolean.parseBoolean(getPropertyValue(randomSeedKey, defaultRandomSeed));
    }

    public static boolean getOverrideDefaults() {
        return Boolean.parseBoolean(getPropertyValue(overrideDefaults, defaultOverrideDefaults));
    }

    public static int getMaxRandomizationDepth() {
        return Integer.parseInt(getPropertyValue(maxRandomizationDepth, defaultMaxRandomizationDepth));
    }

    public static List<String> getAndroidCustomArrangers() {
        String values = getPropertyValue(androidCustomArrangers, defaultAndroidCustomArrangers);
        return Arrays.stream(values.split(","))
                .map(String::trim)
                .filter(it -> !it.isEmpty())
                .collect(Collectors.toList());
    }

    private static String getPropertyValue(String key, String defaultValue) {
        String value = System.getProperties().getProperty(key);
        if (value == null) {
            value = propertiesFromFile.getProperty(key);
        }
        if (value == null) {
            value = defaultValue;
        }
        return value;
    }
}
