package com.ocadotechnology.gembus.test;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.ocadotechnology.gembus.test.Arranger.*;
import static org.assertj.core.api.Assertions.assertThat;

public class ArrangerTestOverrides {
    HashMap<String, Supplier<?>> overrides = new HashMap<>();

    @Test
    void should_useValuesFromOverrides_when_anOverrideIsSuppliedForExistingField() {
        //given
        Integer override = someInteger();
        overrides.put("number", () -> override);

        //when
        ToOverride actual = some(ToOverride.class, overrides);

        //then
        assertThat(actual.text).isNotNull();
        assertThat(actual.primitiveNumber).isNotEqualTo(0);
        assertThat(actual.number).isEqualTo(override);
    }

    @Test
    void should_createInitializedObject_when_requestingOverrideOfNonExistingField() {
        //given
        overrides.put("nonexisting", () -> someInteger());

        //when
        ToOverride actual = some(ToOverride.class, overrides);

        //then
        assertThat(actual.text).isNotNull();
        assertThat(actual.primitiveNumber).isNotEqualTo(0);
        assertThat(actual.number).isNotEqualTo(0);
    }

    @Test
    void should_overridePrimitives() {
        //given
        Long override = someLong();
        overrides.put("primitiveNumber", () -> override);

        //when
        ToOverride actual = some(ToOverride.class, overrides);

        //then
        assertThat(actual.text).isNotNull();
        assertThat(actual.primitiveNumber).isEqualTo(override);
        assertThat(actual.number).isNotEqualTo(0);
    }

    @Test
    void should_overrideManyFields() {
        //given
        String overrideText = someText();
        Long overrideLong = someLong();
        Integer overrideInt = someInteger();
        overrides.put("text", () -> overrideText);
        overrides.put("primitiveNumber", () -> overrideLong);
        overrides.put("number", () -> overrideInt);

        //when
        ToOverride actual = some(ToOverride.class, overrides);

        //then
        assertThat(actual.text).isNotNull();
        assertThat(actual.primitiveNumber).isEqualTo(overrideLong);
        assertThat(actual.number).isNotEqualTo(0);
    }

    @Test
    void should_createInitializedObject_when_supplyingWrongTypeInOverrides() {
        //given
        Long override = someLong();
        overrides.put("text", () -> override);

        //when
        ToOverride actual = some(ToOverride.class, overrides);

        //then
        assertThat(actual.text).isNotNull();
        assertThat(actual.primitiveNumber).isNotEqualTo(0);
        assertThat(actual.number).isNotEqualTo(0);
    }

    @Test
    void should_useValuesFromOverrides_when_creatingSeveralObjects() {
        //given
        Integer override = someInteger();
        Integer noOfObjects = somePositiveInt(10);
        overrides.put("number", () -> override);

        //when
        Stream<ToOverride> actual = someObjects(ToOverride.class, noOfObjects, overrides);

        //then
        assertThat(actual).allSatisfy(a -> {
            assertThat(a.number).isEqualTo(override);
        });
    }

    static class ToOverride {
        String text;
        long primitiveNumber;
        Integer number;
    }
}
