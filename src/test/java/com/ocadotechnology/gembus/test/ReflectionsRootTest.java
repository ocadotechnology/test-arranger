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

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReflectionsRootTest {

    private final String key = "arranger.root";

    @AfterEach
    public void cleanupProperties() {
        System.getProperties().remove(key);
    }

    @Test
    public void shouldFindRootInSystemProperties() {
        //given
        String expected = Arranger.some(String.class);
        System.setProperty(key, expected);

        //when
        final String actual = ReflectionsRoot.getRootPackage();

        //then
        assertEquals(expected, actual);
    }

    @Test
    public void shouldFindRootInPropertiesFile() {
        //when
        final String actual = ReflectionsRoot.getRootPackage();

        //then
        assertEquals("com.ocadotechnology.gembus", actual);
    }
}
