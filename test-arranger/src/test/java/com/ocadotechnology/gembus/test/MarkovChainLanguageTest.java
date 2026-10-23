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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class MarkovChainLanguageTest {

    @Test
    @DisplayName("SHOULD use the bundled chain WHEN there is no language artifact on the classpath")
    void fallBackToBundledChain() {
        // when
        String actual = MarkovChainProvider.detectChainFile(file -> false);

        // then
        assertThat(actual).isEqualTo("enMarkovChain");
    }

    @Test
    @DisplayName("SHOULD use the chain of the only language artifact on the classpath")
    void useSingleLanguageArtifact() {
        // when
        String actual = MarkovChainProvider.detectChainFile(Set.of("markov/deMarkovChain")::contains);

        // then
        assertThat(actual).isEqualTo("markov/deMarkovChain");
    }

    @Test
    @DisplayName("SHOULD probe the languages in the order en, pl, de WHEN many language artifacts are on the classpath")
    void probeLanguagesInOrder() {
        // then
        assertThat(MarkovChainProvider.detectChainFile(file -> true)).isEqualTo("markov/enMarkovChain");
        assertThat(MarkovChainProvider.detectChainFile(Set.of("markov/plMarkovChain", "markov/deMarkovChain")::contains))
                .isEqualTo("markov/plMarkovChain");
    }

    @Test
    @DisplayName("SHOULD detect and parse the chain only once WHEN it is requested again")
    void detectChainOnce() {
        // then
        assertThat(MarkovChainProvider.detect())
                .isSameAs(MarkovChainProvider.detect());
    }

    @Test
    @DisplayName("SHOULD report the missing file WHEN the chain does not exist")
    void reportMissingChain() {
        // then
        assertThatThrownBy(() -> new MarkovChain("xxMarkovChain"))
                .hasRootCauseInstanceOf(IllegalStateException.class)
                .hasMessageContaining("xxMarkovChain");
    }
}
