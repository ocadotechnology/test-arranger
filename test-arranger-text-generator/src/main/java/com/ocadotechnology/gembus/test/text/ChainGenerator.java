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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Downloads the public domain books of a language (see {@link Language}) and writes a word based Markov chain.
 * Usage: ChainGenerator {lang} {output file} {download cache directory} [min word occurrences, defaults per language]
 */
public final class ChainGenerator {

    private ChainGenerator() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            throw new IllegalArgumentException("Usage: ChainGenerator {lang} {output file} {cache dir} [min word occurrences]");
        }
        final Language language = Language.of(args[0]);
        final Path output = Path.of(args[1]);
        final Path cache = Path.of(args[2]);
        final int minOccurrences = args.length > 3 ? Integer.parseInt(args[3]) : language.minWordOccurrences;

        final List<String> words = new ArrayList<>();
        for (String source : language.sources) {
            final String book = language.boilerplateStripper.apply(download(source, cache));
            words.addAll(TextCleaner.toWords(book, language.alphabet, language.locale()));
        }
        Files.createDirectories(output.getParent());
        Files.writeString(output, new ChainBuilder(minOccurrences).build(words), StandardCharsets.UTF_8);
        System.out.println("Words: " + words.size() + ", written to " + output);
    }

    private static String download(String url, Path cache) throws IOException {
        final Path cached = cache.resolve(url.replaceAll("[^a-zA-Z0-9.]", "_"));
        if (!Files.exists(cached)) {
            Files.createDirectories(cache);
            Files.writeString(cached, fetch(url), StandardCharsets.UTF_8);
        }
        return Files.readString(cached, StandardCharsets.UTF_8);
    }

    private static String fetch(String url) {
        try {
            final HttpResponse<String> response = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build()
                    .send(HttpRequest.newBuilder(URI.create(url)).build(),
                            HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) {
                throw new IllegalStateException("Cannot download " + url + ", status " + response.statusCode());
            }
            return response.body();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }
}
