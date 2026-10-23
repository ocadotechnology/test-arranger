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

import java.util.List;
import java.util.function.Predicate;

/**
 * Delivers the Markov chain used to generate texts.
 * The chain of a language is delivered by the 'test-arranger-text-{lang}' artifact as the classpath resource
 * 'markov/{lang}MarkovChain'. The supported languages are probed in a fixed order and the first chain found is used.
 * When there is no such artifact on the classpath, the light 'enMarkovChain' bundled with the library is used.
 * Parsing a chain is expensive and the classpath does not change, hence the chain is detected and parsed only once.
 */
class MarkovChainProvider {

    static final List<String> SUPPORTED_LANGUAGES = List.of("en", "pl", "de");
    static final String DEFAULT_CHAIN_FILE = "enMarkovChain";

    private static final class DetectedChain {
        static final MarkovChain INSTANCE = new MarkovChain(detectChainFile(MarkovChainProvider::existsOnClasspath));
    }

    static MarkovChain detect() {
        return DetectedChain.INSTANCE;
    }

    static String detectChainFile(Predicate<String> existsOnClasspath) {
        return SUPPORTED_LANGUAGES.stream()
                .map(MarkovChainProvider::fileNameFor)
                .filter(existsOnClasspath)
                .findFirst()
                .orElse(DEFAULT_CHAIN_FILE);
    }

    static String fileNameFor(String language) {
        return "markov/" + language + "MarkovChain";
    }

    private static boolean existsOnClasspath(String file) {
        return MarkovChainProvider.class.getResource("/" + file) != null;
    }
}
