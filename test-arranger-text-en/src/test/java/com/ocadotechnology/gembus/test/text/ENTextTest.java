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

import com.ocadotechnology.gembus.test.Arranger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class ENTextTest {

    private static final String WORD = "[A-Za-z'\\-]+";

    @Test
    @DisplayName("SHOULD generate text in the language of the artifact WHEN it is on the classpath")
    void someTextUsesChainFromArtifact() {
        //when
        List<String> actual = IntStream.range(0, 200)
                .mapToObj(i -> Arranger.someText(40, 60))
                .collect(Collectors.toList());

        //then
        assertThat(actual).allMatch(it -> it.matches(WORD + "( " + WORD + ")*"));
    }
}
