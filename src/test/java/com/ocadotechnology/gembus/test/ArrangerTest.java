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

import com.ocadotechnology.gembus.ToTestNonPublic;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

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
        for (int i = 0; i < 999; i++) {
            //given
            final BigDecimal min = new BigDecimal("64");
            final BigDecimal max = new BigDecimal("65");

            //when
            final BigDecimal actual = Arranger.somePriceLikeBigDecimal(min, max);

            //then
            assertThat(actual).isGreaterThanOrEqualTo(min);
            assertThat(actual).isLessThanOrEqualTo(max);
            assertEquals(2, actual.scale());
        }
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
    void someTextWhenRequestingMaxLength() {
        //given
        int maxLen = 1;

        //when
        final String actual = Arranger.someText(maxLen);

        //then
        assertThat(actual.length()).isEqualTo(maxLen);
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
    void someFromSetOfSizeOne() {
        //given
        final Set<Integer> listOfSizeOne = Collections.singleton(Arranger.someInteger());

        //when
        Integer selected = Arranger.someFrom(listOfSizeOne);

        //then
        assertThat(selected).isEqualTo(listOfSizeOne.iterator().next());
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
    public void shouldSupportPackagePrivateArrangers() {
        //when
        ToTestNonPublic actual = Arranger.some(ToTestNonPublic.class);

        //then
        assertThat(actual.someText).isEqualTo(ToTestNonPublic.ARRANGER_TEXT);
        assertThat(actual.someNumber).isNotNull();
    }

    @Test
    public void shouldGenerateStringOfGivenMaxLength() {
        for (int i = 0; i < 100; i++) {
            //given
            int maxLen = Arranger.somePositiveInt(50);

            //when
            String actual = Arranger.someString(maxLen);

            //then
            assertThat(actual.length()).isLessThanOrEqualTo(maxLen);
        }
    }

    @Test
    public void shouldGenerateStringOfLengthWithinGivenBoundaries() {
        //given
        int minLen = 995;
        int maxLen = 1_000;

        //when
        String actual = Arranger.someString(minLen, maxLen);

        //then
        assertThat(actual.length())
                .isLessThanOrEqualTo(maxLen)
                .isGreaterThanOrEqualTo(minLen);
    }

    @Test
    public void shouldGenerateIntFromGivenRange() {
        for (int i = 0; i < 100; i++) {
            //given
            int min = Arranger.somePositiveInt(500);
            int max = min + 10;

            //when
            int actual = Arranger.someInteger(min, max);

            //then
            assertThat(actual)
                    .isLessThanOrEqualTo(max)
                    .isGreaterThanOrEqualTo(min);
        }
    }

    @Test
    public void shouldGenerateIntFromGivenRangeIncludingBoundaries() {
        //given
        int boundaries = Arranger.somePositiveInt(100);

        //when
        int actual = Arranger.someInteger(boundaries, boundaries);

        //then
        assertThat(actual).isEqualTo(boundaries);
    }

    @Test
    public void shouldGenerateIntFromRangeOverNegativeNumbers() {
        for (int i = 0; i < 100; i++) {
            //given
            int min = Integer.MIN_VALUE;
            int max = Arranger.somePositiveInt(100);

            //when
            int actual = Arranger.someInteger(min, max);

            //then
            assertThat(actual)
                    .isLessThanOrEqualTo(max)
                    .isGreaterThanOrEqualTo(min);
        }
    }

    @Test
    public void should_generateFirstName() {
        //when
        String actual = Arranger.someFirstName();

        //then
        assertThat(actual.length()).isGreaterThan(1);
        assertThat(actual.charAt(0)).isUpperCase();
        assertThat(actual.substring(1)).isEqualTo(actual.substring(1).toLowerCase(Locale.ROOT));
    }

    @Test
    public void should_generateLastName() {
        //when
        String actual = Arranger.someLastName();

        //then
        assertThat(actual.length()).isGreaterThan(1);
        assertThat(actual.charAt(0)).isUpperCase();
    }

    @Test
    void should_generateFloatFromTheRangeDefinedByMinAndMax() {
        //given
        float min = -999.99f;
        float max = 999.99f;

        //when
        float actualMin = 0.0f;
        float actualMax = 0.0f;
        for (int i = 0; i < 999; i++) {
            double actual = Arranger.someFloat(min, max);
            actualMin = (float) Math.min(actualMin, actual);
            actualMax = (float) Math.max(actualMax, actual);
        }

        //then
        assertThat(actualMin).isGreaterThanOrEqualTo(min);
        assertThat(actualMin).isLessThan(-1.0f);
        assertThat(actualMax).isLessThan(max);
        assertThat(actualMax).isGreaterThan(1.0f);
    }

    @Test
    void should_generateDoubleFromTheRangeDefinedByMinAndMax() {
        //given
        double min = -999.99;
        double max = 999.99;

        //when
        double actualMin = 0.0;
        double actualMax = 0.0;
        for (int i = 0; i < 999; i++) {
            double actual = Arranger.someDouble(min, max);
            actualMin = Math.min(actualMin, actual);
            actualMax = Math.max(actualMax, actual);
        }

        //then
        assertThat(actualMin).isGreaterThanOrEqualTo(min);
        assertThat(actualMin).isLessThan(-1.0);
        assertThat(actualMax).isLessThan(max);
        assertThat(actualMax).isGreaterThan(1.0);
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
