package com.ocado.gembus.test;

import io.github.benas.randombeans.api.Randomizer;

import java.util.stream.Stream;

public class MarkovStringRandomizer implements Randomizer<String> {

    private final int minLength;
    private final int maxLength;
    private static MarkovChain markov = new MarkovChain("enMarkovChain");

    public MarkovStringRandomizer(int minLength, int maxLength) {
        this.minLength = minLength;
        this.maxLength = maxLength;
    }

    MarkovStringRandomizer(int minLength, int maxLength, MarkovChain markov) {
        MarkovStringRandomizer.markov = markov;
        this.minLength = minLength;
        this.maxLength = maxLength;
    }

    @Override
    public String getRandomValue() {
        final int size = (int) (Math.random() * (1 + maxLength - minLength) + minLength);
        String prev = getTrimmed(null);
        StringBuilder actual = new StringBuilder(prev);
        while (actual.length() < size - 1) {
            addWordSeparator(actual);
            prev = markov.fireTransition(prev);
            actual.append(prev);
        }
        addWordSeparator(actual);
        actual.append(getTrimmed(prev));
        return toStringRespectingMaxLen(actual);
    }

    private String toStringRespectingMaxLen(StringBuilder actual) {
        if (actual.length() > maxLength) {
            return actual.substring(0, maxLength).trim();
        }
        return actual.toString();
    }

    private void addWordSeparator(StringBuilder actual) {
        if (markov.generateWords()) {
            actual.append(" ");
        }
    }

    private String getTrimmed(String from) {
        return Stream.iterate("", i -> markov.fireTransition(from))
                .filter(txt -> txt.length() > 0 && txt.trim().length() == txt.length())
                .findFirst()
                .get();
    }
}
