package com.ocado.gembus.test;


import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

public class MarkovStringRandomizerTest {

    final int min = 15;
    final int max = 25;

    static MarkovChain charMarkov = new MarkovChain("enMarkovChain");
    static MarkovChain wordMarkov = new MarkovChain("wordMarkovChain");

    @Test
    public void getRandomValueOfRequestedSize() {
        //given
        final MarkovStringRandomizer markovStringRandomizer = new MarkovStringRandomizer(min, max, charMarkov);

        //when
        final List<String> actual = IntStream.range(0, 100).boxed()
                .map(i -> markovStringRandomizer.getRandomValue())
                .collect(Collectors.toList());

        //then
        assertThat(actual)
                .allMatch(a -> a.length() >= min)
                .allMatch(a -> a.length() <= max);
    }

    @Test
    public void getTrimmedRandomValue() {
        //given
        final MarkovStringRandomizer markovStringRandomizer = new MarkovStringRandomizer(min, max, charMarkov);

        //when
        final List<String> actual = IntStream.range(0, 100).boxed()
                .map(i -> markovStringRandomizer.getRandomValue())
                .collect(Collectors.toList());

        //then
        assertThat(actual)
                .allMatch(a -> a.length() == a.trim().length());
    }

    @Test
    public void getRandomValueOfRequestedSizeWhenWorkingWithWords() {
        final MarkovStringRandomizer markovStringRandomizer = new MarkovStringRandomizer(min, max, wordMarkov);

        //when
        final List<String> actual = IntStream.range(0, 100).boxed()
                .map(i -> markovStringRandomizer.getRandomValue())
                .collect(Collectors.toList());

        System.out.println(actual);
        //then
        assertThat(actual)
                .allMatch(a -> a.length() >= min)
                .allMatch(a -> a.length() <= max);
    }

    @Test
    public void getRandomValueWithSpacesWhenWorkingWithWords() {
        final MarkovStringRandomizer markovStringRandomizer = new MarkovStringRandomizer(min, max, wordMarkov);

        //when
        final List<String> actual = IntStream.range(0, 100).boxed()
                .map(i -> markovStringRandomizer.getRandomValue())
                .collect(Collectors.toList());

        System.out.println(actual);
        //then
        assertThat(actual)
                .allMatch(a -> a.contains(" "));
    }
}
