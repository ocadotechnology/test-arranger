package com.ocado.gembus.test;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        String prev = null;
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
    @Disabled
    public void generateChainOfChars() throws URISyntaxException, IOException {
        Map<String, Map<String, Integer>> charOccurences = loadOccurences("file", text -> {
            List<String> listOf = new ArrayList<>(text.length());
            for (char c : text.toCharArray()) {
                listOf.add(String.valueOf(c));
            }
            return listOf;
        });
        saveAsMarkovChain(charOccurences);
    }

    @Test
    @Disabled
    public void generateChainOfWords() throws URISyntaxException, IOException {
        Map<String, Map<String, Integer>> charOccurences = loadOccurences("file", text -> Arrays.stream(text.split("\\s+"))
                .map(word -> word.trim().replaceAll("[^a-zA-Z\\'\\-]", ""))
                .filter(word -> word.length() > 0)
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1, word.length()))
                .collect(Collectors.toList()));
        saveAsMarkovChain(charOccurences);
    }

    private Map<String, Map<String, Integer>> loadOccurences(String fileName, Function<String, List<String>> textToListOfStringsConverter) throws URISyntaxException, IOException {
        Map<String, Map<String, Integer>> occurences = new HashMap<>();
        Path path = Paths.get(getClass().getClassLoader().getResource(fileName).toURI());

        String text = Files.readAllLines(path).stream()
                .map(line -> line.toLowerCase())
                .collect(Collectors.joining(" "));

        List<String> listOf = textToListOfStringsConverter.apply(text);

        String previous = listOf.get(0);
        for (int i = 1; i < listOf.size(); i++) {
            Map<String, Integer> previousOccurences = occurences.get(previous);
            if (previousOccurences == null) {
                previousOccurences = new HashMap<>();
                occurences.put(previous, previousOccurences);
            }

            Integer pastOccurences = previousOccurences.get(listOf.get(i));
            if (pastOccurences == null) {
                pastOccurences = 0;
            }
            previousOccurences.put(listOf.get(i), pastOccurences + 1);
            previous = listOf.get(i);
        }
        return occurences;
    }

    private void saveAsMarkovChain(Map<String, Map<String, Integer>> occurences) {
        occurences.entrySet().forEach(entry -> {
            final double sumOfOccurences = entry.getValue().values().stream().reduce((a, b) -> a + b).get();
            final String lineKey = MarkovChainParser.SEPARATOR + entry.getKey() + MarkovChainParser.SEPARATOR + MarkovChainParser.TRANSITION_OPERATOR;
            final String lineValue = entry.getValue().entrySet().stream()
                    .map(transition -> "" + (transition.getValue() / sumOfOccurences) + (MarkovChainParser.SEPARATOR + transition.getKey() + MarkovChainParser.SEPARATOR))
                    .reduce("", (a, b) -> a + b);
            System.out.println(lineKey + lineValue);
        });
    }
}
