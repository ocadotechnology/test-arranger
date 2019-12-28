package com.ocado.gembus.test;

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
    void someFromEmptyList() {
        assertThrows(IllegalArgumentException.class, () -> Arranger.someFrom(new ArrayList<>()));
    }

    @Test
    void someFromList() {
        //given
        final List<String> someStrings = Arrays.asList(Arranger.someText(), Arranger.someText(), Arranger.someText());

        for(int i=0; i<100; i++) {
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

        for(int i=0; i<100; i++) {
            //when
            Integer actual = Arranger.someFrom(someNumbers);

            //then
            assertThat(someNumbers).contains(actual);
        }
    }
}
