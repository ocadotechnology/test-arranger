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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class ArrangerTest {

    @Test
    public void matching() {
        //given
        final Predicate<LocalDate> predicate = (date) -> date.isAfter(LocalDate.now());

        //when
        final LocalDate whatever = Arranger.someMatching(LocalDate.class, predicate);

        //then
        assertTrue(predicate.test(whatever));
    }

    @Test
    void email() {
        //when
        final String actual = Arranger.someEmail();

        //then
        final String[] actualEmail = actual.split("@");
        assertEquals(2, actualEmail.length);
    }

    @Test
    void priceLike() {
        //when
        final BigDecimal price = Arranger.somePriceLikeBigDecimal();

        //then
        assertTrue(price.compareTo(BigDecimal.ZERO) >= 0);
        assertEquals(2, price.scale());
    }

    @Test
    void priceLikeFromGivenRange() {
        //given
        final BigDecimal min = new BigDecimal("100.01");
        final BigDecimal max = new BigDecimal("100.05");

        //when
        final BigDecimal actual = Arranger.somePriceLikeBigDecimal(min, max);

        //then
        assertThat(actual).isGreaterThanOrEqualTo(min);
        assertThat(actual).isLessThanOrEqualTo(max);
    }

    @Test
    void priceLikeFromGivenRange_emptyRange() {
        //given
        final BigDecimal min = new BigDecimal("100.01");
        final BigDecimal max = new BigDecimal("100.01");

        //when
        final BigDecimal actual = Arranger.somePriceLikeBigDecimal(min, max);

        //then
        assertThat(actual).isGreaterThanOrEqualTo(min);
        assertThat(actual).isLessThanOrEqualTo(max);
    }

    @Test
    void someString() {
        //when
        final String actual = Arranger.some(String.class);

        //then
        assertThat(actual.length())
                .isGreaterThanOrEqualTo(9)
                .isLessThanOrEqualTo(16);
    }

    @Test
    void someText() {
        //when
        final String actual = Arranger.someText();

        //then
        assertThat(actual.length())
                .isGreaterThanOrEqualTo(9)
                .isLessThanOrEqualTo(16);
    }

    @Test
    void someTextWhenRequestingExplicitLength() {
        //given
        int minLength = 900;
        int maxLength = 1000;

        //when
        final String actual = Arranger.someText(minLength, maxLength);

        //then
        assertThat(actual.length())
                .isGreaterThanOrEqualTo(minLength)
                .isLessThanOrEqualTo(maxLength);
    }

    @Test
    void someFromEmptyList() {
        assertThrows(IllegalArgumentException.class, () -> Arranger.someFrom(new ArrayList<>()));
    }

    @Test
    void someFromList() {
        //given
        final List<String> someStrings = Arrays.asList(Arranger.someText(), Arranger.someText(), Arranger.someText());

        for (int i = 0; i < 100; i++) {
            //when
            String actual = Arranger.someFrom(someStrings);

            //then
            assertThat(someStrings).contains(actual);
        }
    }

    @Test
    void someFromCollection() {
        //given
        final Set<Integer> someNumbers = new HashSet<>(Arrays.asList(Arranger.someInteger(), Arranger.someInteger(), Arranger.someInteger()));

        for (int i = 0; i < 100; i++) {
            //when
            Integer actual = Arranger.someFrom(someNumbers);

            //then
            assertThat(someNumbers).contains(actual);
        }
    }

    @Test
    void someShouldRespectExclusionForField() {
        //when
        final SomeClass actual = Arranger.some(SomeClass.class, "text");

        //then
        assertThat(actual.text).isNull();
    }

    @Test
    void someSimplifiedShouldRespectExclusionForField() {
        //when
        final SomeClass actual = Arranger.someSimplified(SomeClass.class, "text");

        //then
        assertThat(actual.text).isNull();
        assertThat(actual.lorem.size()).isLessThanOrEqualTo(1);
    }

    @Test
    void someShouldRespectExclusionForFieldWhenRequestingCollection() {
        //when
        final List<SomeClass> actual = Arranger.someObjects(SomeClass.class, Arranger.somePositiveInt(20), "text").collect(Collectors.toList());

        //then
        assertThat(actual).allMatch(it -> it.text == null);
    }

    @Test
    public void someSimplifiedShouldGenerateSmallCollections() {
        Set<Integer> sizes = new HashSet<>();

        //when
        for (int i = 0; i < 100; i++) {
            sizes.add(Arranger.someSimplified(SomeClass.class).lorem.size());
        }

        //then
        assertThat(sizes)
                .hasSize(2)
                .contains(1)
                .contains(0);
    }

    @Test
    public void sequenceOfGeneratedLongsShouldNotBeRepeated() {
        //given
        Set<Long> longs = new HashSet<>();
        Set<Long> objects = new HashSet<>();
        int N = 100;

        //when
        for (int i = 0; i < N; i++) {
            longs.add(Arranger.someLong());
            objects.add(Arranger.some(SomeClass.class).number);
        }

        //then
        assertThat(longs).hasSize(N);
        longs.addAll(objects);
        assertThat(longs).hasSize(2 * N);
    }

    @Test
    public void sequenceOfGeneratedLongsShouldNotBeRepeatedInCustomArrangers() {
        //given
        Set<Long> withLong = new HashSet<>();
        Set<Long> anotherWithLong = new HashSet<>();
        int N = 100;

        //when
        for (int i = 0; i < N; i++) {
            withLong.add(Arranger.some(ClassWithLong.class).number);
            anotherWithLong.add(Arranger.some(AnotherClassWithLong.class).number);
        }

        //then
        assertThat(withLong).hasSize(N);
        withLong.addAll(anotherWithLong);
        assertThat(withLong).hasSize(2 * N);
    }

    @Test
    public void respectExcludedFieldsInCustomArrangers() {
        //when
        ClassWithLong number = Arranger.some(ClassWithLong.class, "number");

        //then
        assertThat(number.number).isEqualTo(0);
    }

    @Test
    public void respectExcludedFieldsInCustomArrangersInSimplifiedMode() {
        //when
        ClassWithLong number = Arranger.someSimplified(ClassWithLong.class, "number");

        //then
        assertThat(number.number).isEqualTo(0);
    }
}

class SomeClass {
    long number;
    String text;
    List<String> lorem;
}

class ClassWithLong {
    long number;
}

class ClassWithLongArranger extends CustomArranger<ClassWithLong> {
    protected ClassWithLong instance() {
        return enhancedRandom.nextObject(type);
    }
}

class AnotherClassWithLong {
    long number;
}

class AnotherClassWithLongArranger extends CustomArranger<AnotherClassWithLong> {
    protected AnotherClassWithLong instance() {
        return enhancedRandom.nextObject(type);
    }
}