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

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Builds a Markov chain in the format read by test-arranger: '##key##-->>probability##target##probability##target##'.
 */
final class ChainBuilder {

    private static final String SEPARATOR = "##";
    private static final String TRANSITION_OPERATOR = "-->>";

    private final int minWordOccurrences;

    /**
     * @param minWordOccurrences words that occur less often are ignored, which keeps the chain small
     */
    ChainBuilder(int minWordOccurrences) {
        this.minWordOccurrences = minWordOccurrences;
    }

    String build(List<String> words) {
        final Map<String, Long> occurrences = words.stream()
                .collect(Collectors.groupingBy(w -> w, Collectors.counting()));
        final List<String> frequent = words.stream()
                .filter(w -> occurrences.get(w) >= minWordOccurrences)
                .collect(Collectors.toList());

        final Map<String, Map<String, Integer>> transitions = new TreeMap<>();
        for (int i = 1; i < frequent.size(); i++) {
            transitions.computeIfAbsent(frequent.get(i - 1), k -> new HashMap<>())
                    .merge(frequent.get(i), 1, Integer::sum);
        }
        return transitions.entrySet().stream()
                .map(entry -> toLine(entry.getKey(), entry.getValue()))
                .collect(Collectors.joining("\n", "", "\n"));
    }

    private String toLine(String key, Map<String, Integer> targets) {
        final double sum = targets.values().stream().mapToInt(Integer::intValue).sum();
        final StringBuilder line = new StringBuilder(SEPARATOR + key + SEPARATOR + TRANSITION_OPERATOR);
        targets.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()))
                .forEach(t -> line.append(probability(t.getValue() / sum)).append(SEPARATOR).append(t.getKey()).append(SEPARATOR));
        return line.toString();
    }

    private String probability(double value) {
        return String.format(Locale.ROOT, "%.6f", value).replaceAll("0+$", "").replaceAll("\\.$", ".0");
    }
}
