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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Turns the downloaded books into a list of words.
 */
final class TextCleaner {

    private static final String WOLNE_LEKTURY_FOOTER = "Ta lektura, podobnie jak tysi";
    private static final String GUTENBERG_START = "*** START OF";
    private static final String GUTENBERG_END = "*** END OF";
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    private TextCleaner() {
    }

    /**
     * Wolne Lektury ends every book with the licence information.
     */
    static String stripWolneLektury(String book) {
        final int footer = book.indexOf(WOLNE_LEKTURY_FOOTER);
        return footer < 0 ? book : book.substring(0, footer);
    }

    /**
     * Project Gutenberg wraps every book with the licence information, marked with START and END lines.
     */
    static String stripGutenberg(String book) {
        final int start = book.indexOf(GUTENBERG_START);
        final int end = book.indexOf(GUTENBERG_END);
        final int from = start < 0 ? 0 : book.indexOf('\n', start) + 1;
        final int to = end < 0 ? book.length() : end;
        return book.substring(from, Math.max(from, to));
    }

    /**
     * @param alphabet a regular expression character class content, e.g. 'a-zäöüß'; everything outside of it, apart
     *                 from apostrophes and hyphens inside words, is dropped
     */
    static List<String> toWords(String text, String alphabet, Locale locale) {
        final Pattern forbidden = Pattern.compile("[^" + alphabet + "'\\-]");
        final List<String> result = new ArrayList<>();
        for (String token : WHITESPACE.split(text.toLowerCase(locale))) {
            final String word = forbidden.matcher(token).replaceAll("").replaceAll("^['\\-]+|['\\-]+$", "");
            if (!word.isEmpty()) {
                result.add(word.substring(0, 1).toUpperCase(locale) + word.substring(1));
            }
        }
        return result;
    }
}
