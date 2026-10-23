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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class ReflectionHelperTest {

    @ParameterizedTest(name = "{3}")
    @MethodSource("allCases")
    void should_detectPrimitiveField(Class<?> clazz, String fieldName, boolean expected, String description) {
        //when
        boolean actual = ReflectionHelper.isPrimitiveField(clazz, fieldName);

        //then
        assertThat(actual).isEqualTo(expected);
    }

    static Stream<Arguments> allCases() {
        return Stream.of(
                Arguments.of(PlainClass.class, "primitive", true, "SHOULD detect primitive WHEN field is primitive in plain class"),
                Arguments.of(PlainClass.class, "boxed", false, "SHOULD detect non-primitive WHEN field is boxed in plain class"),
                Arguments.of(PlainRecord.class, "primitive", true, "SHOULD detect primitive WHEN field is primitive in record"),
                Arguments.of(PlainRecord.class, "boxed", false, "SHOULD detect non-primitive WHEN field is boxed in record"),
                Arguments.of(ChildWithPrimitiveParent.class, "primitive", true, "SHOULD detect primitive WHEN field is inherited from parent"),
                Arguments.of(ChildWithBoxedParent.class, "boxed", false, "SHOULD detect non-primitive WHEN field is inherited from parent"),
                Arguments.of(ChildHidesWithBoxed.class, "value", false, "SHOULD detect non-primitive WHEN child hides parent field"),
                Arguments.of(ChildHidesWithPrimitive.class, "value", true, "SHOULD detect primitive WHEN child hides parent field"),
                Arguments.of(PlainClass.class, "missing", false, "SHOULD return false WHEN field does not exist"),
                Arguments.of(PlainClass.class, "", false, "SHOULD return false WHEN field name is empty"),
                Arguments.of(PlainClass.class, null, false, "SHOULD return false WHEN field name is null")
        );
    }

    static class PlainClass {
        int primitive;
        Integer boxed;
    }

    record PlainRecord(int primitive, Integer boxed) {
    }

    static class ParentWithPrimitive {
        int primitive;
    }

    static class ChildWithPrimitiveParent extends ParentWithPrimitive {
    }

    static class ParentWithBoxed {
        Integer boxed;
    }

    static class ChildWithBoxedParent extends ParentWithBoxed {
    }

    static class ParentPrimitiveValue {
        int value;
    }

    static class ChildHidesWithBoxed extends ParentPrimitiveValue {
        String value;
    }

    static class ParentBoxedValue {
        String value;
    }

    static class ChildHidesWithPrimitive extends ParentBoxedValue {
        int value;
    }
}