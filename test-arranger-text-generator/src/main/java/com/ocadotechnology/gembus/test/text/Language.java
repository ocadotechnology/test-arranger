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

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.UnaryOperator;

/**
 * The languages with a generated chain: the public domain books used as the corpus and the alphabet of the words.
 */
enum Language {

    PL("pl", "a-ząćęłńóśźż", 20, TextCleaner::stripWolneLektury,
            wolneLektury("potop-tom-pierwszy", "potop-tom-drugi", "ogniem-i-mieczem-tom-pierwszy", "quo-vadis",
                    "w-pustyni-i-w-puszczy",
                    "lalka-tom-pierwszy", "lalka-tom-drugi", "faraon-tom-pierwszy",
                    "chlopi-czesc-pierwsza-jesien",
                    "przedwiosnie",
                    "nad-niemnem-tom-pierwszy", "nad-niemnem-tom-drugi")),
    DE("de", "a-zäöüß", 10, TextCleaner::stripGutenberg,
            gutenberg(22367, 5323, 2407, 2229, 34811, 6696, 28042));

    final String code;
    final String alphabet;
    final int minWordOccurrences;
    final UnaryOperator<String> boilerplateStripper;
    final List<String> sources;

    Language(String code, String alphabet, int minWordOccurrences, UnaryOperator<String> boilerplateStripper, List<String> sources) {
        this.code = code;
        this.alphabet = alphabet;
        this.minWordOccurrences = minWordOccurrences;
        this.boilerplateStripper = boilerplateStripper;
        this.sources = sources;
    }

    Locale locale() {
        return Locale.forLanguageTag(code);
    }

    static Language of(String code) {
        return valueOf(code.toUpperCase(Locale.ROOT));
    }

    private static List<String> wolneLektury(String... slugs) {
        return Arrays.stream(slugs)
                .map(slug -> "https://wolnelektury.pl/media/book/txt/" + slug + ".txt")
                .toList();
    }

    private static List<String> gutenberg(int... ids) {
        return Arrays.stream(ids)
                .mapToObj(id -> "https://www.gutenberg.org/cache/epub/" + id + "/pg" + id + ".txt")
                .toList();
    }
}
