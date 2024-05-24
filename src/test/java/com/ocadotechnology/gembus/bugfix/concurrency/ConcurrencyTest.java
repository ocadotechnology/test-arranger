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
package com.ocadotechnology.gembus.bugfix.concurrency;

import com.ocadotechnology.gembus.test.Arranger;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class ConcurrencyTest {

    @Test
    void concurrentSomeWithOverridesDoesNotThrow() {
        assertDoesNotThrow(() -> {
            IntStream.range(0, 10).parallel().mapToObj(i -> createCustomStruct()).toList();
        });
    }

    private CustomStruct createCustomStruct() {
        return Arranger.some(CustomStruct.class, Map.of("id", () -> "id", "uuid", UUID::randomUUID));
    }
}

