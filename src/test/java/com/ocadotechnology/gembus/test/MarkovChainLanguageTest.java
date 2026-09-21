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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class MarkovChainLanguageTest {

    private static final int MIN_LENGTH = 20;
    private static final int MAX_LENGTH = 40;

    @AfterEach
    public void cleanupProperties() {
        System.getProperties().remove(PropertiesWrapperTest.textLanguageKey);
    }

    @ParameterizedTest
    @CsvSource({
            "pl, '[a-ząćęłńóśźż ,.]+'",
            "de, '[a-zäöüß ,.]+'"
    })
    @DisplayName("SHOULD generate text using the alphabet of the configured language")
    void generateTextInConfiguredLanguage(String language, String alphabet) {
        //given
        System.setProperty(PropertiesWrapperTest.textLanguageKey, language);

        //when
        List<String> actual = someTexts();

        //then
        assertThat(actual)
                .allMatch(it -> it.matches(alphabet))
                .allMatch(it -> it.length() >= MIN_LENGTH && it.length() <= MAX_LENGTH);
    }

    @ParameterizedTest
    @CsvSource({
            "pl, 'ąćęłńóśźż'",
            "de, 'äöüß'"
    })
    @DisplayName("SHOULD use the characters specific for the configured language")
    void useLanguageSpecificCharacters(String language, String specificCharacters) {
        //given
        System.setProperty(PropertiesWrapperTest.textLanguageKey, language);

        //when
        String actual = String.join("", someTexts());

        //then
        assertThat(actual).containsAnyOf(specificCharacters.split(""));
    }

    @Test
    @DisplayName("SHOULD use English WHEN no language is configured")
    void defaultLanguageIsEnglish() {
        //expect
        assertThat(PropertiesWrapper.getTextLanguage()).isEqualTo("en");
        assertThat(MarkovChainProvider.fileNameFor(PropertiesWrapper.getTextLanguage())).isEqualTo("enMarkovChain");
    }

    @Test
    @DisplayName("SHOULD report the missing file WHEN there is no chain for the configured language")
    void reportMissingChain() {
        //given
        System.setProperty(PropertiesWrapperTest.textLanguageKey, "xx");

        //expect
        assertThatThrownBy(() -> Arranger.someText(MIN_LENGTH, MAX_LENGTH))
                .hasRootCauseInstanceOf(IllegalStateException.class)
                .hasMessageContaining("xxMarkovChain");
    }

    @Test
    @DisplayName("SHOULD parse the chain only once WHEN the same language is requested again")
    void cacheChainPerLanguage() {
        //expect
        assertThat(MarkovChainProvider.forLanguage("de")).isSameAs(MarkovChainProvider.forLanguage("de"));
        assertThat(MarkovChainProvider.forLanguage("de")).isNotSameAs(MarkovChainProvider.forLanguage("pl"));
    }

    private List<String> someTexts() {
        return IntStream.range(0, 50)
                .mapToObj(i -> Arranger.someText(MIN_LENGTH, MAX_LENGTH))
                .collect(Collectors.toList());
    }
}
