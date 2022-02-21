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

import org.jeasy.random.randomizers.EmailRandomizer;
import org.jeasy.random.randomizers.FirstNameRandomizer;
import org.jeasy.random.randomizers.LastNameRandomizer;
import org.jeasy.random.randomizers.RegularExpressionRandomizer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * To arrange test data using pseudo random values.
 */
public class Arranger {
    private static EnhancedRandom random = ArrangersConfigurer.instance().defaultRandom();
    private static EnhancedRandom simplifiedRandom = ArrangersConfigurer.instance().simplifiedRandom();
    private static final EmailRandomizer emailRandomizer = new EmailRandomizer();
    private static final MarkovStringRandomizer stringRandomizer = new MarkovStringRandomizer(ArrangersConfigurer.STRING_MIN_LENGTH, ArrangersConfigurer.STRING_MAX_LENGTH);
    private static final FirstNameRandomizer firstNameRandomizer = new FirstNameRandomizer();
    private static final LastNameRandomizer lastNameRandomizer = new LastNameRandomizer();

    /**
     * @see org.jeasy.random.EasyRandom#nextObject
     */
    public static <T> T some(final Class<T> type, final String... excludedFields) {
        return random.nextObject(type, excludedFields);
    }

    /**
     * @see org.jeasy.random.EasyRandom#nextObject
     */
    public static <T> T someSimplified(final Class<T> type, final String... excludedFields) {
        return simplifiedRandom.nextObject(type, excludedFields);
    }

    /**
     * @see org.jeasy.random.EasyRandom#objects(Class, int)
     */
    public static <T> Stream<T> someObjects(final Class<T> type, final int amount, final String... excludedFields) {
        return random.objects(type, amount, excludedFields);
    }

    /**
     * @return whatever T that satisfies predicate
     */
    public static <T> T someMatching(Class<T> type, Predicate<T> predicate, String... excludedFields) {
        T whatever;
        int noTries = 0;
        do {
            whatever = some(type, excludedFields);
            if (noTries++ > 250) {
                throw new CannotSatisfyPredicateException(type.getName());
            }
        } while (!predicate.test(whatever));
        return whatever;
    }

    /**
     * @return whatever array member that satisfies predicate or null if no member satisfies it
     */
    public static <T> T someMatching(T[] array, Predicate<T> predicate) {
        final List<T> list = Arrays.asList(array);
        Collections.shuffle(list);

        return list.stream()
                .filter(predicate)
                .findFirst()
                .orElse(null);
    }

    /**
     * @return whatever (pseudo random) email
     */
    public static String someEmail() {
        return emailRandomizer.getRandomValue();
    }

    /**
     * @return whatever (pseudo-random) text, that looks a bit like a sequence of words; there is a Markov chain trained on English text underneath that generates a sequence of characters
     */
    public static String someText() {
        return stringRandomizer.getRandomValue();
    }

    /**
     * @return whatever (pseudo-random) text, that looks a bit like a sequence of words; there is a Markov chain trained on English text underneath that generates a sequence of characters
     */
    public static String someText(int maxLength) {
        MarkovStringRandomizer randomizer = new MarkovStringRandomizer(1, maxLength);
        return randomizer.getRandomValue();
    }

    /**
     * @return whatever (pseudo-random) text, that looks a bit like a sequence of words; there is a Markov chain trained on English text underneath that generates a sequence of characters
     */
    public static String someText(int minLength, int maxLength) {
        MarkovStringRandomizer randomizer = new MarkovStringRandomizer(minLength, maxLength);
        return randomizer.getRandomValue();
    }

    /**
     * @return whatever string i.e. some(String.class)
     */
    public static String someString() {
        return some(String.class);
    }

    /**
     * @return whatever string i.e. some(String.class), but with length less or equal the maxLength
     */
    public static String someString(int maxLength) {
        return someString(1, maxLength);
    }

    /**
     * @return whatever string i.e. some(String.class), but with length greater or equal the minLength and less or equal the maxLength
     */
    public static String someString(int minLength, int maxLength) {
        int length = someInteger(minLength, maxLength);
        StringBuilder result = new StringBuilder();
        do {
            result.append(some(String.class));
        } while (result.length() < length);
        return result.substring(0, length);
    }

    /**
     * @return whatever positive BigDecimal with 2 decimal places, i.e. like a price
     */
    public static BigDecimal somePriceLikeBigDecimal() {
        return new BigDecimal(somePositiveInt(10_000)).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    public static long someLong() {
        return random.nextObject(Long.class);
    }

    public static int someInteger() {
        return random.nextObject(Integer.class);
    }

    /**
     * @param minValue inclusive
     * @param maxValue inclusive
     * @return whatever integer from range [minValue,maxValue]
     */
    public static int someInteger(int minValue, int maxValue) {
        Long rangeSize = 1L + maxValue - minValue;
        long randomWithinSize = Math.abs(someLong()) % rangeSize;
        return (int) randomWithinSize + minValue;
    }

    /**
     * @param boundIncl non null positive value
     * @return whatever integer from range [1,boundIncl]
     */
    public static int somePositiveInt(Integer boundIncl) {
        if (boundIncl <= 1) {
            return 1;
        }
        return 1 + random.easyRandom.nextInt(boundIncl - 1);
    }

    private static int someNonNegativeInt() {
        return Arranger.somePositiveInt(100) - 1;
    }

    /**
     * @param boundIncl non null positive value
     * @return whatever long from range [1,boundIncl]
     */
    public static long somePositiveLong(Long boundIncl) {
        if (boundIncl <= 0) {
            throw new IllegalArgumentException("bound must be positive");
        }
        return 1 + Math.abs(someLong() - 2) % boundIncl;
    }

    public static boolean someBoolean() {
        return random.easyRandom.nextBoolean();
    }

    public static <T> T someFrom(Collection<T> source) {
        if (source.isEmpty()) {
            throw new IllegalArgumentException("Cannot return element from empty collection.");
        }
        if (source instanceof List) {
            return ((List<T>) source).get(new Random().nextInt(source.size()));
        } else {
            return source.stream().skip(new Random().nextInt(source.size() - 1)).findFirst().get();
        }
    }

    public static LocalDate someGivenOrLater(LocalDate given) {
        return given.plusDays(someNonNegativeInt());
    }

    public static LocalDate someGivenOrEarlier(LocalDate given) {
        return given.minusDays(someNonNegativeInt());
    }

    public static Instant someInstant() {
        return Instant.now();
    }

    public static BigDecimal somePriceLikeBigDecimal(BigDecimal min, BigDecimal max) {
        if (min.compareTo(max) >= 0) {
            return min;
        }
        final BigDecimal centsRatio = BigDecimal.valueOf(100);
        Integer valueInCents = somePositiveInt(max.add(new BigDecimal("0.01")).subtract(min).multiply(centsRatio).intValue());
        return new BigDecimal(valueInCents).divide(centsRatio).add(min);
    }

    public static String someFirstName() {
        return firstNameRandomizer.getRandomValue();
    }

    public static String someLastName() {
        return lastNameRandomizer.getRandomValue();
    }

    public static String someStringLike(String regex) {
        return new RegularExpressionRandomizer(regex).getRandomValue();
    }

    static class CannotSatisfyPredicateException extends RuntimeException {
        public CannotSatisfyPredicateException(String type) {
            super("Cannot satisfy provided predicate when generating data of type " + type);
        }
    }
}
