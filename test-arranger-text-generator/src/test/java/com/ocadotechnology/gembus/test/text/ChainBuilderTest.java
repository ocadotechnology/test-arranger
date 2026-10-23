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
package com.ocadotechnology.gembus.test.text;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChainBuilderTest {

    @Test
    @DisplayName("SHOULD write a line with probabilities for every word that has a successor")
    void build() {
        //when
        String actual = new ChainBuilder(1).build(List.of("A", "B", "A", "B", "A", "C"));

        //then
        assertThat(actual).isEqualTo("##A##-->>0.666667##B##0.333333##C##\n##B##-->>1.0##A##\n");
    }

    @Test
    @DisplayName("SHOULD ignore the words that are rarer than the threshold")
    void ignoreRareWords() {
        //when
        String actual = new ChainBuilder(2).build(List.of("A", "B", "A", "B", "A", "C"));

        //then
        assertThat(actual).isEqualTo("##A##-->>1.0##B##\n##B##-->>1.0##A##\n");
    }
}
