package com.ocado.gembus.test;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


public class MarkovChainTest {

    @Test
    public void loadMarkovChain() {
        //given
        final MarkovChain markov = new MarkovChain("enMarkovChain");

        //then
        assertThat(markov.transitions.get(" "))
                .contains(new Transition("a", 0.5))
                .contains(new Transition("b", 1.0));
        assertThat(markov.transitions.get("a"))
                .contains(new Transition("b", 0.8))
                .contains(new Transition(" ", 1.0));
        assertThat(markov.transitions.get("b"))
                .contains(new Transition("a", 0.8))
                .contains(new Transition(" ", 1.0));
    }

    @Test
    public void fireTransition() {
        //given
        final MarkovChain markov = new MarkovChain("enMarkovChain");

        //when
        String prev = " ";
        String actual = "";
        for (int i = 0; i < 1000; i++) {
            prev = markov.fireTransition(prev);
            actual += prev;
        }

        //then
        assertThat(actual).matches("[ab ]+");
        assertThat(actual).contains("a");
        assertThat(actual).contains("b");
    }

    @Test
    public void generateChain() throws URISyntaxException, IOException {
        Map<Character, Map<Character, Integer>> charOccurences = loadCharOccurences("file");
        saveAsMarkovChain(charOccurences);
    }

    private void saveAsMarkovChain(Map<Character, Map<Character, Integer>> charOccurences) {
        charOccurences.entrySet().forEach(entry -> {
            final double sumOfOccurences = entry.getValue().values().stream().reduce((a, b) -> a + b).get();
            final String lineKey = MarkovChainParser.SEPARATOR + entry.getKey() + MarkovChainParser.SEPARATOR + MarkovChainParser.TRANSITION_OPERATOR;
            final String lineValue = entry.getValue().entrySet().stream()
                    .map(transition -> "" + (transition.getValue() / sumOfOccurences) + (MarkovChainParser.SEPARATOR + transition.getKey() + MarkovChainParser.SEPARATOR))
                    .reduce("", (a, b) -> a + b);
            System.out.println(lineKey + lineValue);
        });
    }

    private Map<Character, Map<Character, Integer>> loadCharOccurences(String fileName) throws URISyntaxException, IOException {
        Map<Character, Map<Character, Integer>> charOccurences = new HashMap<>();
        Path path = Paths.get(getClass().getClassLoader().getResource(fileName).toURI());
        Character previous = ' ';
        for (String line : Files.readAllLines(path)) {
            for (int i = 0; i < line.length(); i++) {
                Map<Character, Integer> previousOccurences = charOccurences.get(previous);
                if (previousOccurences == null) {
                    previousOccurences = new HashMap<>();
                    charOccurences.put(previous, previousOccurences);
                }
                final char newChar = line.charAt(i);
                Integer pastOccurences = previousOccurences.get(newChar);
                if (pastOccurences == null) {
                    pastOccurences = 0;
                }
                previousOccurences.put(newChar, pastOccurences + 1);
                previous = line.charAt(i);
            }
        }
        return charOccurences;
    }
}
