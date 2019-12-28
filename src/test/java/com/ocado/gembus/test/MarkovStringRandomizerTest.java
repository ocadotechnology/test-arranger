package com.ocado.gembus.test;


import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

public class MarkovStringRandomizerTest {

    final int min = 5;
    final int max = 10;

    @Test
    public void getRandomValueOfRequestedSize() {
        //given
        final MarkovStringRandomizer markovStringRandomizer = new MarkovStringRandomizer(min, max);

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
        final int min = 5;
        final int max = 10;
        final MarkovStringRandomizer markovStringRandomizer = new MarkovStringRandomizer(min, max);

        //when
        final List<String> actual = IntStream.range(0, 100).boxed()
                .map(i -> markovStringRandomizer.getRandomValue())
                .collect(Collectors.toList());

        //then
        assertThat(actual)
                .allMatch(a -> a.length() == a.trim().length());
    }
}
