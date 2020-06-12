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
import java.util.*;
import java.util.function.Predicate;

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
}
