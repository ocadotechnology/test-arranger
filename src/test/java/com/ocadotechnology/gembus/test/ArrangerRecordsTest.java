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
import org.junit.jupiter.api.Timeout;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.ocadotechnology.gembus.test.Arranger.some;
import static com.ocadotechnology.gembus.test.Arranger.someSimplified;
import static org.assertj.core.api.Assertions.assertThat;

public class ArrangerRecordsTest {
    @Test
    void should_arrangeRecords() {
        //when
        Data actual = some(Data.class);

        //then
        assertThat(actual.value()).isNotEqualTo(0);
        assertThat(actual.name()).isNotBlank();
    }

    @Test
    void should_arrangeCollectionsInRecords() {
        //when
        Data actual = some(Data.class);

        //then
        assertThat(actual.tags().size()).isGreaterThan(0);
        assertThat(actual.tags().iterator().next().length()).isGreaterThanOrEqualTo(ArrangersConfigurer.STRING_MIN_LENGTH);
        assertThat(actual.classWithCustomArranger().size()).isGreaterThan(0);
        assertThat(actual.classWithCustomArranger().get(0).dummyId).isNull();
        assertThat(actual.classWithCustomArranger().get(0).children.size()).isGreaterThan(0);
    }

    @Test
    void should_arrangeCollectionsInRecordsRespectingSimplified() {
        //given
        Data actual = someSimplified(Data.class);

        //then
        assertThat(actual.name().length()).isLessThan(ArrangersConfigurer.STRING_MIN_LENGTH);
        assertThat(actual.tags()).isEmpty();
        assertThat(actual.classWithCustomArranger()).isEmpty();
    }

    @Test
    void should_arrangeGenericsInRecords() {
        //when
        DataWithVariousTypes actual = some(DataWithVariousTypes.class);

        //then
        assertThat(actual.a()).isNotEmpty();
        assertThat(actual.b()).isNotEmpty();
        assertThat(actual.c()).isNotEmpty();
        assertThat(actual.d()).isNotEmpty();
        assertThat(actual.e()).isNotNull();
    }

    @Test
    @Timeout(3)
    void should_limitTheNestingLevel_when_inRecursiveStructures() {
        //when
        NestedRecord actual = some(NestedRecord.class);

        //then
        assertThat(actual.children()).isNotEmpty();
        assertThat(actual.children().get(0).children().get(0).children()).isEmpty();
    }

    @Test
    void should_respectCustomArrangers_when_inRecords() {
        //when
        Answer actual = some(Answer.class);

        //then
        assertThat(actual.answer().value()).isEqualTo(42L);
    }

    @Test
    @Timeout(3)
    void should_limitTheNestingLevel_when_inDirectlyRecursiveStructures() {
        //when
        DirectlyNested actual = some(DirectlyNested.class);

        //then
        assertThat(actual.value()).isNotNull();
        assertThat(actual.child().child().child()).isEqualTo(new DirectlyNested(null,null));
        assertThat(actual.child().child().value()).isNotNull();
    }
}

record Data(Integer value, String name, Set<String> tags, List<NestedStructure> classWithCustomArranger) {
    Data {
    }

    Data(String name) {
        this(0, name, Set.of(), List.of());
    }
}

record DataWithVariousTypes(Set<String> a, String[] b, Map<String, String> c, Optional<String> d, SomeEnum e) {
}

record NestedRecord(List<NestedRecord> children) {
}

enum SomeEnum {A, B, C, D, E}

class WithList {
    List<String> list;
}

record Always42(long value) {
}

record Answer(Always42 answer) {
}

class Always42Arranger extends CustomArranger<Always42> {
    @Override
    protected Always42 instance() {
        return new Always42(42L);
    }
}

record DirectlyNested(DirectlyNested child, Integer value) {
}