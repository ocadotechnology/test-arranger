package com.ocado.gembus.test;

import io.github.benas.randombeans.EnhancedRandomBuilder;
import io.github.benas.randombeans.api.EnhancedRandom;
import io.github.classgraph.ClassGraph;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

class ArrangerBuilder {

    static final int STRING_MIN_LENGTH = 9;
    static final int STRING_MAX_LENGTH = 16;
    private static ArrangerBuilder instance;
    private ArrangerSubBuilder defaultSubBuilder = new ArrangerSubBuilder();
    private ArrangerSubBuilder flatSubBuilder = new ArrangerSubBuilder();

    private ArrangerBuilder() {
        final List<Constructor<?>> arrangerConstructors = new ClassGraph()
                .whitelistPackages(ReflectionsRoot.getRootPackage())
                .enableAllInfo()
                .scan()
                .getSubclasses(CustomArranger.class.getName())
                .loadClasses(CustomArranger.class, true)
                .stream()
                .filter(clazz -> isNotAbstract(clazz))
                .map(clazz -> extractConstructor(clazz))
                .filter(constructor -> constructor.isPresent())
                .map(constructor -> constructor.get())
                .collect(Collectors.toList());

        defaultSubBuilder.createAllCustomArrangers(arrangerConstructors);
        flatSubBuilder.createAllCustomArrangers(arrangerConstructors);
    }

    static ArrangerBuilder instance() {
        if (instance == null) {
            instance = new ArrangerBuilder();
        }
        return instance;
    }

    EnhancedRandom buildArranger(Optional<Class> target) {
        final EnhancedRandomBuilder enhancedRandomBuilder = EnhancedRandomBuilder.aNewEnhancedRandomBuilder()
                .collectionSizeRange(1, 3)
                .randomizationDepth(15)
                .stringLengthRange(STRING_MIN_LENGTH, STRING_MAX_LENGTH);
        defaultSubBuilder.configureEnhancedRandomBuilder(enhancedRandomBuilder, target, this::buildArranger);
        return enhancedRandomBuilder.build();
    }

    EnhancedRandom buildFlatArranger(Optional<Class> target) {
        final EnhancedRandomBuilder enhancedRandomBuilder = EnhancedRandomBuilder.aNewEnhancedRandomBuilder()
                .collectionSizeRange(1, 1)
                .randomizationDepth(3)
                .stringLengthRange(5, 10);
        flatSubBuilder.configureEnhancedRandomBuilder(enhancedRandomBuilder, target, this::buildFlatArranger);
        return enhancedRandomBuilder.build();
    }

    private boolean isNotAbstract(Class<CustomArranger> clazz) {
        return !Modifier.isAbstract(clazz.getModifiers());
    }

    private Optional<Constructor<?>> extractConstructor(Class<?> clazz) {
        final Optional<Constructor<?>> result = Arrays.stream(clazz.getDeclaredConstructors())
                .filter(constructor -> constructor.getParameterCount() == 0)
                .findAny();
        if (!result.isPresent()) {
            System.err.println(clazz.getCanonicalName() + " has no suitable constructor.");
        }
        return result;
    }
}

