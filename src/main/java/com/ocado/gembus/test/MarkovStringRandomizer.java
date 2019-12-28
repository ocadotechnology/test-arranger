package com.ocado.gembus.test;

import io.github.benas.randombeans.api.Randomizer;

import java.util.stream.Stream;

public class MarkovStringRandomizer implements Randomizer<String> {

    private final int minLength;
    private final int maxLength;
    private static final MarkovChain markov = new MarkovChain("enMarkovChain");

    public MarkovStringRandomizer(int minLength, int maxLength) {
        this.minLength = minLength;
        this.maxLength = maxLength;
    }

    @Override
    public String getRandomValue() {
        final int size = (int) (Math.random() * (1 + maxLength - minLength) + minLength);
        String prev = getTrimmed(" ");
        StringBuilder actual = new StringBuilder(prev);
        for (int i = 1; i < size-1; i++) {
            prev = markov.fireTransition(prev);
            actual.append(prev);
        }
        actual.append(getTrimmed(prev));
        return actual.toString();
    }

    private String getTrimmed(String from) {
        return Stream.iterate("", i -> markov.fireTransition(from))
                    .filter(txt -> txt.length() > 0 && txt.trim().length() == txt.length())
                    .findFirst()
                    .get();
    }
}
