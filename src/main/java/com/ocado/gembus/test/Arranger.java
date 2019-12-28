package com.ocado.gembus.test;

import io.github.benas.randombeans.api.EnhancedRandom;
import io.github.benas.randombeans.randomizers.EmailRandomizer;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * To arrange test data using pseudo random values.
 */
public class Arranger {
    private static EnhancedRandom whatever = ArrangerBuilder.instance().buildArranger(Optional.empty());
    private static EnhancedRandom flatWhatever = ArrangerBuilder.instance().buildFlatArranger(Optional.empty());
    private static final EmailRandomizer emailRandomizer = new EmailRandomizer();
    private static final MarkovStringRandomizer stringRandomizer = new MarkovStringRandomizer(ArrangerBuilder.STRING_MIN_LENGTH, ArrangerBuilder.STRING_MAX_LENGTH);

    /**
     * @see io.github.benas.randombeans.api.EnhancedRandom#nextObject
     */
    public static <T> T some(final Class<T> type, final String... excludedFields) {
        return whatever.nextObject(type, excludedFields);
    }

    /**
     * @see io.github.benas.randombeans.api.EnhancedRandom#nextObject
     */
    public static <T> T someSimplified(final Class<T> type, final String... excludedFields) {
        return flatWhatever.nextObject(type, excludedFields);
    }

    /**
     * @see io.github.benas.randombeans.api.EnhancedRandom#objects(Class, int, String...)
     */
    public static <T> Stream<T> someObjects(final Class<T> type, final int amount, final String... excludedFields) {
        return whatever.objects(type, amount, excludedFields);
    }

    /**
     * @return whatever T that satisfies predicate
     */
    public static <T> T someMatching(Class<T> type, Predicate<T> predicate, String... excludedFields) {
        T whatever;
        int noTries = 0;
        do {
            whatever = Arranger.whatever.nextObject(type, excludedFields);
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
     * @return whatever (pseudo random) text, that looks like a sequence of words
     */
    public static String someText() {
        return stringRandomizer.getRandomValue();
    }

    /**
     * @return whatever positive BigDecimal with 2 decimal places, i.e. like a price
     */
    public static BigDecimal somePriceLikeBigDecimal() {
        return new BigDecimal(somePositiveInt(10_000)).divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
    }
    public static Long someLong() {
        return whatever.nextLong();
    }

    public static Integer someInteger() {
        return whatever.nextInt();
    }

    /**
     * @param boundIncl non null positive value
     * @return whatever integer from range [1,boundIncl]
     */
    public static Integer somePositiveInt(Integer boundIncl) {
        return 1 + whatever.nextInt(boundIncl - 1);
    }

    /**
     * @param boundIncl non null positive value
     * @return whatever long from range [1,boundIncl]
     */
    public static Long somePositiveLong(Long boundIncl) {
        if (boundIncl <= 0) {
            throw new IllegalArgumentException("bound must be positive");
        }
        return 1 + Math.abs(whatever.nextLong() - 2) % boundIncl;
    }

    public static Boolean someBoolean() {
        return whatever.nextBoolean();
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

    private static int someNonNegativeInt() {
        return Arranger.somePositiveInt(100) - 1;
    }

    static class CannotSatisfyPredicateException extends RuntimeException {

        public CannotSatisfyPredicateException(String type) {
            super("Cannot satisfy provided predicate when generating data of type " + type);
        }
    }
}
