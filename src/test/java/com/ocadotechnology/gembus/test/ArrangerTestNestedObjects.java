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

import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class ArrangerTestNestedObjects {
    @Test
    public void avoidNullsInNestedObjectOnDeepestLevel() {
        //when
        RecursiveObject actual = Arranger.some(RecursiveObject.class);

        //then
        assertThat(actual).isNotNull();
        assertThat(actual.flatten())
                .isNotEmpty()
                .allMatch(it -> it.collection != null, "Collections should avoid nulls")
                .allMatch(it -> it.array != null, "Arrays should avoid null")
                .allMatch(it -> it.map != null, "Maps should avoid nulls")
                .allMatch(it -> it.field != null, "Other fields should avoid nulls")
                .anyMatch(it -> it.collection.isEmpty())
                .anyMatch(it -> it.array.length == 0)
                .anyMatch(it -> it.map.isEmpty());
    }

    @Test
    public void avoidNullsInOptionalsOnDeepestNestingLevel() {
        //when
        RecursiveThroughOptional actual = Arranger.some(RecursiveThroughOptional.class);

        //then
        assertThat(actual).isNotNull();
        do {
            assertThat(actual.optional).isNotNull();
            assertThat(actual.field).isNotNull();
            actual = actual.optional.get();
        } while(actual.optional.isPresent());
    }
}

class RecursiveObject {
    String field;
    List<RecursiveObject> collection;
    RecursiveObject[] array;
    Map<String, RecursiveObject> map;

    RecursiveObject(String field, ArrayList<RecursiveObject> collection, RecursiveObject[] array, Map<String, RecursiveObject> map) {
        this.field = field == null ? "" : field;
        this.collection = collection == null ? new ArrayList<>() : collection;
        this.array = array == null ? new RecursiveObject[0] : array;
        this.map = map == null ? new HashMap<>() : map;
    }

    List<RecursiveObject> flatten() {
        ArrayList<RecursiveObject> result = new ArrayList<>();
        result.add(this);
        if (collection != null) {
            result.addAll(flattenRecursively(collection.stream()));
        }
        if (array != null) {
            result.addAll(flattenRecursively(Arrays.stream(array)));
        }
        if (map != null) {
            result.addAll(flattenRecursively(map.values().stream()));
        }
        return result;
    }

    private List<RecursiveObject> flattenRecursively(Stream<RecursiveObject> stream) {
        return stream.flatMap(it -> it.flatten().stream())
                .collect(Collectors.toList());
    }
}

class RecursiveThroughOptional {
    String field;
    Optional<RecursiveThroughOptional> optional;
}