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
        final EnhancedRandomBuilder enhancedRandomBuilder = getEnhancedRandomBuilder();
        defaultSubBuilder.configureEnhancedRandomBuilder(enhancedRandomBuilder, target, this::buildArranger);
        return enhancedRandomBuilder.build();
    }

    static EnhancedRandomBuilder getEnhancedRandomBuilder() {
        return EnhancedRandomBuilder.aNewEnhancedRandomBuilder()
                .collectionSizeRange(1, 3)
                .randomizationDepth(15)
                .stringLengthRange(STRING_MIN_LENGTH, STRING_MAX_LENGTH);
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

