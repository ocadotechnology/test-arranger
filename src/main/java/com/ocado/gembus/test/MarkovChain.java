package com.ocado.gembus.test;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class MarkovChain {
    final Map<String, Transition[]> transitions = new HashMap<>();
    final MarkovChainParser parser = new MarkovChainParser();

    public MarkovChain(String transitionsFile) {
        try (InputStream in = getClass().getResourceAsStream("/" + transitionsFile);
             BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Pair<String, Transition[]> parsed = parser.parseLine(line);
                transitions.put(parsed.getKey(), parsed.getValue());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String fireTransition(String from) {
        final double random = Math.random();
        final Transition transition = Arrays.stream(this.transitions.get(from))
                .filter(t -> random <= t.cumulativeProbability)
                .findFirst()
                .get();
        return transition.target;
    }
}

final class Transition {
    final String target;
    final double cumulativeProbability;

    Transition(String target, double cumulativeProbability) {
        this.cumulativeProbability = cumulativeProbability;
        this.target = target;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transition that = (Transition) o;
        return new EqualsBuilder()
                .append(cumulativeProbability, that.cumulativeProbability)
                .append(target, that.target)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(target)
                .append(cumulativeProbability)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("target", target)
                .append("cumulativeProbability", cumulativeProbability)
                .toString();
    }
}

class MarkovChainParser {

    static final String TRANSITION_OPERATOR = "-->>";
    static final String SEPARATOR = "##";
    final Pattern keyPattern = Pattern.compile(SEPARATOR + "(.*)" + SEPARATOR);

    Pair<String, Transition[]> parseLine(String line) {
        final String[] split = line.split(TRANSITION_OPERATOR);
        if (split.length != 2) {
            throw new IllegalStateException("Cannot parse line in Markov Chain");
        }
        return new ImmutablePair<>(parseKey(split[0]), parseTransitions(split[1]));
    }

    private String parseKey(String text) {
        final Matcher matcher = keyPattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            throw new IllegalStateException("Cannot parse key value in Markov Chain");
        }
    }

    private Transition[] parseTransitions(String text) {
        final String[] transitionsSplit = text.split(SEPARATOR);
        final List<Transition> transitions = new ArrayList<>();
        double consumedProbability = 0.0;
        for (int i = 0; i < transitionsSplit.length; i += 2) {
            double probability = Double.parseDouble(transitionsSplit[i]);
            transitions.add(new Transition(transitionsSplit[i + 1], consumedProbability += probability));
        }
        final Transition[] result = transitions.toArray(new Transition[transitions.size()]);
        result[result.length - 1] = new Transition(result[result.length - 1].target, 1.0);
        return result;
    }
}