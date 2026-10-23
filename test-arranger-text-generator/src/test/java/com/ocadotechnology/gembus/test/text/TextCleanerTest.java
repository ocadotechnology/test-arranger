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

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class TextCleanerTest {

    @Test
    @DisplayName("SHOULD cut the licence footer WHEN the book comes from Wolne Lektury")
    void stripWolneLektury() {
        //when
        String actual = TextCleaner.stripWolneLektury("Treść książki.\n-----\nTa lektura, podobnie jak tysiące innych");

        //then
        assertThat(actual).startsWith("Treść książki.").doesNotContain("Ta lektura");
    }

    @Test
    @DisplayName("SHOULD keep only the text between the START and END lines WHEN the book comes from Gutenberg")
    void stripGutenberg() {
        //when
        String actual = TextCleaner.stripGutenberg("licence\n*** START OF THE PROJECT GUTENBERG EBOOK X ***\nDer Text.\n*** END OF THE PROJECT GUTENBERG EBOOK X ***\nlicence");

        //then
        assertThat(actual).isEqualTo("Der Text.\n");
    }

    @Test
    @DisplayName("SHOULD return capitalized words of the alphabet WHEN the text contains punctuation and foreign characters")
    void toWords() {
        //when
        var actual = TextCleaner.toWords("– Żółć, gęślą; \"jaźń\" 123 abc-def 'x' q", "a-ząćęłńóśźż", new Locale("pl"));

        //then
        assertThat(actual).containsExactly("Żółć", "Gęślą", "Jaźń", "Abc-def", "X", "Q");
    }
}
