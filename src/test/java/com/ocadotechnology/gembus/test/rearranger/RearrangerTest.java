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
package com.ocadotechnology.gembus.test.rearranger;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static com.ocadotechnology.gembus.test.Arranger.*;
import static org.assertj.core.api.Assertions.assertThat;

public class RearrangerTest {

    @Test
    public void SHOULD_copyInstancesWithOverrides_WHEN_itIsRecord() {
        // given
        DataClass original = some(DataClass.class);
        var otherBefore = original.other();
        assertThat(otherBefore).isNotNull();
        String overrideString = someString();
        int overrideNumber = somePositiveInt(100);

        // when
        DataClass copy = Rearranger.copy(original, Map.of(
                "name", () -> overrideString,
                "number", () -> overrideNumber
        ));

        // then
        assertThat(copy.name()).isEqualTo(overrideString);
        assertThat(copy.number()).isEqualTo(overrideNumber);
        assertThat(copy.other()).isEqualTo(otherBefore);
    }

    @Test
    public void SHOULD_copyInstancesWithOverrides_WHEN_instanceHasPrivateFields() {
        //given
        PojoClass original = some(PojoClass.class);
        var otherBefore = original.other;
        assertThat(otherBefore).isNotNull();
        String overrideString = someString();
        int overrideNumber = somePositiveInt(100);

        //when
        PojoClass copy = Rearranger.copy(original, Map.of(
                "name", () -> overrideString,
                "number", () -> overrideNumber
        ));

        //then
        assertThat(copy.getName()).isEqualTo(overrideString);
        assertThat(copy.getNumber()).isEqualTo(overrideNumber);
        assertThat(copy.other).isEqualTo(otherBefore);
    }

    @Test
    public void SHOULD_copyInstancesWithOverrides_WHEN_instanceImmutable() {
        //given
        Immutable original = some(Immutable.class);
        var otherBefore = original.other;
        assertThat(otherBefore).isNotNull();
        String overrideString = someString();
        int overrideNumber = somePositiveInt(100);

        //when
        Immutable copy = Rearranger.copy(original, Map.of(
                "name", () -> overrideString,
                "number", () -> overrideNumber
        ));

        //then
        assertThat(copy.name).isEqualTo(overrideString);
        assertThat(copy.number).isEqualTo(overrideNumber);
        assertThat(copy.other).isEqualTo(otherBefore);
    }

    @Test
    public void SHOULD_copyInstancesWithOverrides_WHEN_instanceIsMutableWithNoConstructor() {
        //given
        NoConstructorMutable original = some(NoConstructorMutable.class);
        var otherBefore = original.other;
        assertThat(otherBefore).isNotNull();

        String overrideString = someString();
        int overrideNumber = somePositiveInt(100);

        //when
        NoConstructorMutable copy = Rearranger.copy(original, Map.of(
                "name", () -> overrideString,
                "number", () -> overrideNumber
        ));

        //then
        assertThat(copy.name).isEqualTo(overrideString);
        assertThat(copy.number).isEqualTo(overrideNumber);
        assertThat(copy.other).isEqualTo(otherBefore);
    }

    @Test
    public void SHOULD_copyInstancesWithOverrides_WHEN_workingWithCollection() {
        //given
        ClassWithCollections original = some(ClassWithCollections.class);
        var listPropertyBefore = original.listProperty;
        assertThat(listPropertyBefore).isNotEmpty();
        var mapPropertyBefore = original.mapProperty;
        assertThat(mapPropertyBefore).isNotEmpty();

        //when
        List<String> expectedList = someObjects(String.class, 3).toList();
        ClassWithCollections copy = Rearranger.copy(original, Map.of(
                "listProperty", () -> expectedList
        ));

        //then
        assertThat(copy.listProperty).isEqualTo(expectedList);
        assertThat(copy.mapProperty).isEqualTo(mapPropertyBefore);
    }

    @Test
    public void SHOULD_copyInstancesWithOverrides_WHEN_workingWithFieldsOverridingParent() {
        //given
        ConcreteClass original = some(ConcreteClass.class);
        var concretePropertyBefore = original.concreteProperty;
        String overrideString = someString();

        //when
        ConcreteClass copy = Rearranger.copy(original, Map.of(
                "abstractProperty", () -> overrideString
        ));

        //then
        assertThat(copy.abstractProperty).isEqualTo(overrideString);
        assertThat(copy.concreteProperty).isEqualTo(concretePropertyBefore);
    }

    @Test
    public void SHOULD_copyInstancesWithOverrides_WHEN_workingWithNotNullFields() {
        //given
        ClassWithNotNullField original = some(ClassWithNotNullField.class);
        String overrideString = someString();

        //when
        ClassWithNotNullField copy = Rearranger.copy(original, Map.of(
                "notNullField", () -> overrideString
        ));

        //then
        assertThat(copy.notNullField).isEqualTo(overrideString);
    }

    @Test
    void SHOULD_copyInstancesWithOverrides_WHEN_workingWithFieldsOfAbstractType() {
        // given
        ClassWithAbstractField original = some(ClassWithAbstractField.class);
        var simpleFieldBefore = original.simpleField;
        var override1 = some(ConcreteClass.class);
        var override2 = some(ConcreteClass.class);

        //when
        ClassWithAbstractField copy = Rearranger.copy(original, Map.of(
                "abstractField", () -> override1,
                "anotherAbstractField", () -> override2
        ));

        //then
        assertThat(copy.abstractField).isEqualTo(override1);
        assertThat(copy.anotherAbstractField).isEqualTo(override2);
        assertThat(copy.simpleField).isEqualTo(simpleFieldBefore);
    }

    record DataClass(String name, int number, String other) {
    }

    class PojoClass {
        private String name;
        private int number;
        String other;

        PojoClass(String name, int number, String other) {
            this.name = name;
            this.number = number;
            this.other = other;
        }

        public String getName() {
            return name;
        }

        public int getNumber() {
            return number;
        }
    }

    class Immutable {
        final String name;
        final Integer number;
        String other;

        Immutable() {
            this.name = null;
            this.number = null;
        }

        Immutable(String name) {
            this.name = name;
            this.number = name.length();
        }
    }

    class NoConstructorMutable {
        String name;
        Integer number;
        String other;
    }

    abstract class AbstractClass {
        abstract String getAbstractProperty();
    }

    class ConcreteClass extends AbstractClass {
        String abstractProperty = "";
        String concreteProperty;

        @Override
        String getAbstractProperty() {
            return abstractProperty;
        }

        void setAbstractProperty(String value) {
            this.abstractProperty = value;
        }
    }

    class ClassWithNotNullField {
        final String notNullField;
        String nullableField;

        ClassWithNotNullField(String notNullField, String nullableField) {
            if (notNullField == null) {
                throw new IllegalArgumentException("notNullField must not be null");
            }
            this.notNullField = notNullField;
            this.nullableField = nullableField;
        }
    }

    class ClassWithAbstractField {
        final AbstractClass anotherAbstractField;
        String simpleField;
        AbstractClass abstractField;

        ClassWithAbstractField(AbstractClass anotherAbstractField, String simpleField) {
            this.anotherAbstractField = anotherAbstractField;
            this.simpleField = simpleField;
        }
    }

    class ClassWithCollections {
        List<String> listProperty;
        Map<String, String> mapProperty;
    }
}