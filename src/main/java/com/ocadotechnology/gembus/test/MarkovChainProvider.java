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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Delivers the Markov chain of the configured language. Parsing a chain is expensive, hence the chains are cached.
 * A chain is looked up on the classpath, so any language can be supported by putting a file named
 * '{language}MarkovChain' there.
 */
class MarkovChainProvider {

    static final String FILE_NAME_SUFFIX = "MarkovChain";

    private static final Map<String, MarkovChain> chainsByLanguage = new ConcurrentHashMap<>();

    static MarkovChain forConfiguredLanguage() {
        return forLanguage(PropertiesWrapper.getTextLanguage());
    }

    static MarkovChain forLanguage(String language) {
        return chainsByLanguage.computeIfAbsent(language, it -> new MarkovChain(fileNameFor(it)));
    }

    static String fileNameFor(String language) {
        return language + FILE_NAME_SUFFIX;
    }
}
