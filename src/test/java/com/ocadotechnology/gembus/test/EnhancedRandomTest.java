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

import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

class EnhancedRandomTest {

    @Test
    void nextObjectShouldRespectExcludedFields() {
        //given
        final EnhancedRandom enhancedRandom = new EnhancedRandom.Builder(ArrangersConfigurer::getEasyRandomDefaultParameters).build(new HashMap<>(), 1L);

        //when
        final SomeClass actual = enhancedRandom.nextObject(SomeClass.class, "text");

        //then
        assertThat(actual.text).isNull();
    }

    @Test
    void nextObjectShouldNotRememberExcludedFields() {
        //given
        final EnhancedRandom enhancedRandom = new EnhancedRandom.Builder(ArrangersConfigurer::getEasyRandomDefaultParameters).build(new HashMap<>(), 1L);
        enhancedRandom.nextObject(SomeClass.class, "text");

        //when
        final SomeClass actual = enhancedRandom.nextObject(SomeClass.class, "number");

        //then
        assertThat(actual.text).isNotNull();
    }
}