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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.ocadotechnology.gembus.test.Arranger.some;
import static com.ocadotechnology.gembus.test.Arranger.someObjects;
import static org.assertj.core.api.Assertions.assertThat;

public class ArrangerEnumSetTest {

    @Test
    @DisplayName("SHOULD arrange a non empty EnumSet WHEN a field of such a type is present")
    void arrangeEnumSet() {
        //when
        ClassWithEnumSetField actual = some(ClassWithEnumSetField.class);

        //then
        assertThat(actual.txt).isNotBlank();
        assertThat(actual.enumeration)
                .isInstanceOf(EnumSet.class)
                .isNotEmpty()
                .isSubsetOf(SomeEnum.values());
    }

    @Test
    @DisplayName("SHOULD arrange a non empty EnumSet WHEN it is a component of a record")
    void arrangeEnumSetInRecord() {
        //when
        RecordWithEnumSetComponent actual = some(RecordWithEnumSetComponent.class);

        //then
        assertThat(actual.enumeration())
                .isInstanceOf(EnumSet.class)
                .isNotEmpty()
                .isSubsetOf(SomeEnum.values());
    }

    @Test
    @DisplayName("SHOULD arrange a non empty EnumSet WHEN the field is nested in another object")
    void arrangeNestedEnumSet() {
        //when
        ClassWithNestedEnumSetField actual = some(ClassWithNestedEnumSetField.class);

        //then
        assertThat(actual.nested.enumeration)
                .isInstanceOf(EnumSet.class)
                .isNotEmpty()
                .isSubsetOf(SomeEnum.values());
    }

    @Test
    @DisplayName("SHOULD arrange EnumSets of various content WHEN several instances are requested")
    void arrangeVariousEnumSets() {
        //when
        List<ClassWithEnumSetField> actual = someObjects(ClassWithEnumSetField.class, 30)
                .collect(Collectors.toList());

        //then
        assertThat(actual).allSatisfy(it -> assertThat(it.enumeration).isNotEmpty().isSubsetOf(SomeEnum.values()));
        Set<EnumSet<SomeEnum>> distinct = actual.stream().map(it -> it.enumeration).collect(Collectors.toSet());
        assertThat(distinct).hasSizeGreaterThan(1);
    }

    @Test
    @DisplayName("SHOULD arrange an EnumSet of all the constants WHEN the enum has less constants than the collection size")
    void arrangeEnumSetOfTinyEnum() {
        //when
        ClassWithTinyEnumSetField actual = some(ClassWithTinyEnumSetField.class);

        //then
        assertThat(actual.enumeration).containsExactly(TinyEnum.ONLY_ONE);
    }

    @Test
    @DisplayName("SHOULD not try to initialize file WHEN an override is delivered")
    void skipEnumSet() {
        //given
        Map<String, Supplier<?>> overrides = new HashMap<String, Supplier<?>>(){{
            put("enumeration", () -> null);
        }};

        //when
        ClassWithEnumSetField some = some(ClassWithEnumSetField.class, overrides);

        //then
        assertThat(some.txt).isNotBlank();
        assertThat(some.enumeration).isNull();
    }

    @Test
    @DisplayName("SHOULD not try to initialize file WHEN an override is delivered and using objects")
    void skipEnumSetInObjects() {
        //given
        Map<String, Supplier<?>> overrides = new HashMap<String, Supplier<?>>(){{
            put("enumeration", () -> null);
        }};

        //when
        List<ClassWithEnumSetField> some = someObjects(ClassWithEnumSetField.class, 2, overrides)
                .collect(Collectors.toList());

        //then
        assertThat(some).allSatisfy(s -> {
            assertThat(s.txt).isNotBlank();
            assertThat(s.enumeration).isNull();
        });
    }
}

class ClassWithEnumSetField {
    String txt;
    EnumSet<SomeEnum> enumeration;
}

class ClassWithNestedEnumSetField {
    ClassWithEnumSetField nested;
}

class ClassWithTinyEnumSetField {
    EnumSet<TinyEnum> enumeration;
}

record RecordWithEnumSetComponent(String txt, EnumSet<SomeEnum> enumeration) {
}

enum SomeEnum {
    ONE,
    TWO,
    THREE,
    FOUR,
    FIVE,
    SIX
}

enum TinyEnum {
    ONLY_ONE
}