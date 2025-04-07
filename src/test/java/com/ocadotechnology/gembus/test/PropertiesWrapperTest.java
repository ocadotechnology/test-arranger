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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.ocadotechnology.gembus.test.Arranger.someString;
import static org.junit.jupiter.api.Assertions.*;

public class PropertiesWrapperTest {

    private final String rootKey = "arranger.root";
    private final String cacheKey = "arranger.cache.enable";
    final static String overrideKey = "arranger.overridedefaults";
    private final String maxDepthKey = "arranger.maxRandomizationDepth";
    final static String androidCustomArrangers = "arranger.android.customArrangers";

    @AfterEach
    public void cleanupProperties() {
        System.getProperties().remove(rootKey);
        System.getProperties().remove(cacheKey);
        System.getProperties().remove(overrideKey);
        System.getProperties().remove(maxDepthKey);
        System.getProperties().remove(androidCustomArrangers);
    }

    @Test
    void shouldReturnListOfAllAndroidCustomArranger_whenSeveralAreConfigured() {
        //given
        String customArranger1 = someString();
        String customArranger2 = someString();
        System.setProperty(androidCustomArrangers, customArranger1 + " , " + customArranger2);

        //when
        List<String> actual = PropertiesWrapper.getAndroidCustomArrangers();

        //then
        assertEquals(2, actual.size());
        assertTrue(actual.contains(customArranger1));
        assertTrue(actual.contains(customArranger2));
    }

    @Test
    void shouldReturnEmptyList_whenNoAndroidCustomArrangersConfigured() {
        //when
        List<String> actual = PropertiesWrapper.getAndroidCustomArrangers();

        //then
        assertTrue(actual.isEmpty());
    }

    @Test
    void shouldFindMaxDepthInSystemProperties() {
        //given
        int expected = Arranger.somePositiveInt(10);
        System.setProperty(maxDepthKey, String.valueOf(expected));

        //when
        int actual = PropertiesWrapper.getMaxRandomizationDepth();

        //then
        assertEquals(expected, actual);
    }

    @Test
    void shouldUseDefaultMaxDepth_whenNotSetInPropertiesFile() {
        //when
        int actual = PropertiesWrapper.getMaxRandomizationDepth();

        //then
        assertEquals(4, actual);
    }

    @Test
    public void shouldFindRootInSystemProperties() {
        //given
        String expected = Arranger.some(String.class);
        System.setProperty(rootKey, expected);

        //when
        final String actual = PropertiesWrapper.getRootPackage();

        //then
        assertEquals(expected, actual);
    }

    @Test
    public void shouldFindRootInPropertiesFile() {
        //when
        final String actual = PropertiesWrapper.getRootPackage();

        //then
        assertEquals("com.ocadotechnology.gembus", actual);
    }

    @Test
    public void shouldReturnTrueWhenCacheEnableIsSetToTrue() {
        //given
        System.setProperty(cacheKey, "true");

        //when
        boolean actual = PropertiesWrapper.getCacheEnable();

        //then
        assertTrue(actual);
    }

    @Test
    public void shouldReturnFalseWhenCacheEnableIsNotSet() {
        //when
        boolean actual = PropertiesWrapper.getCacheEnable();

        //then
        assertFalse(actual);
    }

    @Test
    public void shouldReturnFalseWhenCacheEnableIsSetToInvalidValue() {
        //given
        System.setProperty(cacheKey, Arranger.some(String.class));

        //when
        boolean actual = PropertiesWrapper.getCacheEnable();

        //then
        assertFalse(actual);
    }

    @Test
    public void shouldReturnFalseWhenOverrideDefaultsIsNotSet() {
        //when
        boolean actual = PropertiesWrapper.getOverrideDefaults();

        //then
        assertFalse(actual);
    }

    @Test
    public void shouldReturnTrueWhenOverrideDefaultsIsSetToTrue() {
        //given
        System.setProperty(overrideKey, "true");

        //when
        boolean actual = PropertiesWrapper.getOverrideDefaults();

        //then
        assertTrue(actual);
    }
}
