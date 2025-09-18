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

import com.ocadotechnology.gembus.test.CustomArranger;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static com.ocadotechnology.gembus.test.Arranger.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
    void SHOULD_returnUnmodifiedCopy_WHEN_noOverridesGivenForRecord() {
        // given
        DataClass original = some(DataClass.class);

        // when
        DataClass copy = Rearranger.copy(original, Map.of());

        //then
        assertThat(copy).isEqualTo(original);
        assertThat(copy).isNotSameAs(original);
    }

    @Test
    void SHOULD_setNull_WHEN_nullSpecifiedInOverridesForRecord() {
        // given
        DataClass original = some(DataClass.class);
        var otherBefore = original.other();
        assertThat(otherBefore).isNotNull();

        // when
        DataClass copy = Rearranger.copy(original, Map.of(
                "other", () -> null
        ));
        // then
        assertThat(copy.other()).isNull();
        assertThat(original.other()).isEqualTo(otherBefore);

    }

    @Test
    void SHOULD_throwException_WHEN_tryingToOverrideNonExistingFieldInRecord() {
        //given
        DataClass original = some(DataClass.class);
        String nonExistingField = someString();

        //when // then
        assertThatThrownBy(() ->
                Rearranger.copy(original, Map.of(nonExistingField, () -> someString()))
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(nonExistingField);
    }

    @Test
    void SHOULD_returnUnmodifiedCopy_WHEN_noOverridesGivenForPlainClass() {
        // given
        PojoClass original = some(PojoClass.class);

        // when
        PojoClass copy = Rearranger.copy(original, Map.of());

        //then
        assertThat(copy).isEqualTo(original);
        assertThat(copy).isNotSameAs(original);
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

    @Test
    void SHOULD_setNull_WHEN_specifiedInOverrides() {
        //given
        PojoClass original = some(PojoClass.class);
        var nameBefore = original.name;
        assertThat(nameBefore).isNotNull();

        //when
        PojoClass copy = Rearranger.copy(original, Map.of(
                "name", () -> null
        ));

        //then
        assertThat(copy.name).isNull();
        assertThat(original.name).isEqualTo(nameBefore);
    }

    @Test
    void SHOULD_copyInstancesWithOverrides_WHEN_overridingFieldFromParentClass() {
        // given
        ChildClass original = some(ChildClass.class);
        var expectedName = someString();

        // when
        ChildClass copy = Rearranger.copy(original, Map.of(
                "name", () -> expectedName
        ));

        //then
        assertThat(copy.getName()).isEqualTo(expectedName);
    }

    @Test
    void SHOULD_throwException_WHEN_tryingToOverrideNonExistingField() {
        //given
        PojoClass original = some(PojoClass.class);
        String nonExistingField = someString();

        //when // then
        assertThatThrownBy(() ->
                Rearranger.copy(original, Map.of(nonExistingField, () -> someString()))
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(nonExistingField);
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PojoClass pojoClass = (PojoClass) o;
            return number == pojoClass.number && java.util.Objects.equals(name, pojoClass.name) && java.util.Objects.equals(other, pojoClass.other);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(name, number, other);
        }
    }

    class ChildClass extends PojoClass {

        String childField;

        ChildClass(String name, int number, String other) {
            super(name, number, other);
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

class AbstractClassArranger extends CustomArranger<RearrangerTest.AbstractClass> {
    @Override
    protected RearrangerTest.AbstractClass instance() {
        return some(RearrangerTest.ConcreteClass.class);
    }
}