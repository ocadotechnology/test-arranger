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

import org.jeasy.random.ObjectCreationException;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.ocadotechnology.gembus.test.Arranger.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;

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
    void should_throwException_when_supplyingWrongTypeInOverrides() {
        //given
        Long override = someLong();
        overrides.put("text", () -> override);

        //when
        Throwable actual = catchThrowable(() -> some(ToOverride.class, overrides));

        //then
        assertThat(actual).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Can not set java.lang.String field com.ocadotechnology.gembus.test.ArrangerTestOverrides$ToOverride.text to java.lang.Long");
    }

    @Test
    void should_throwException_when_supplyingOverrideForNonExistingField() {
        //given
        overrides.put("nonexisting", () -> someInteger());

        //when
        Throwable actual = catchThrowable(() -> some(ToOverride.class, overrides));

        //then
        assertThat(actual).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Failed to override field nonexisting in class com.ocadotechnology.gembus.test.ArrangerTestOverrides$ToOverride");
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

    @Test
    void should_useValueFromOverrides_when_generatingRecord() {
        //given
        Integer override = someInteger();
        overrides.put("number", () -> override);

        //when
        RecordToOverride actual = some(RecordToOverride.class, overrides);

        //then
        assertThat(actual.text).isNotNull();
        assertThat(actual.number).isEqualTo(override);
    }

    @Test
    void should_useValueFromOverrides_when_generatingStreamOfRecord() {
        //given
        String override = someText();
        overrides.put("text", () -> override);
        int size = somePositiveInt(4);

        //when
        Stream<RecordToOverride> actual = someObjects(RecordToOverride.class, size, overrides);

        //then
        assertThat(actual).allSatisfy(r -> {
            assertThat(r.text()).isEqualTo(override);
            assertThat(r.number()).isNotEqualTo(0);
        }).hasSize(size);
    }

    @Test
    void should_throwException_when_supplyingOverrideForNonExistingRecordField() {
        //given
        overrides.put("nonexisting", () -> someInteger());

        //when
        Throwable actual = catchThrowable(() -> some(RecordToOverride.class, overrides));

        //then
        assertThat(actual).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Failed to override field nonexisting in class com.ocadotechnology.gembus.test.ArrangerTestOverrides$RecordToOverride");
    }

    @Test
    void should_throwException_when_supplyingWrongTypeInOverridesForRecord() {
        //given
        Long override = someLong();
        overrides.put("text", () -> override);

        //when
        Throwable actual = catchThrowable(() -> some(RecordToOverride.class, overrides));

        //then
        assertThat(actual).isInstanceOf(ObjectCreationException.class);
    }

    static class ToOverride {
        String text;
        long primitiveNumber;
        Integer number;
    }

    record RecordToOverride(String text, Integer number) {
    }
}
