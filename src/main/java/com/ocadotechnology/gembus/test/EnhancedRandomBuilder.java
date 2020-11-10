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
package com.ocadotechnology.gembus.test;

import org.jeasy.random.EasyRandomParameters;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;

class EnhancedRandomBuilder {

    static final int STRING_MIN_LENGTH = 9;
    static final int STRING_MAX_LENGTH = 16;
    static final long DEFAULT_SEED = 123L;
    private static EnhancedRandomBuilder instance;

    private final Map<Class<?>, CustomArranger<?>> defaultArrangers;
    private final Map<Class<?>, CustomArranger<?>> simplifiedArrangers;

    private EnhancedRandomBuilder() {
        final ReflectionHelper reflectionHelper = new ReflectionHelper();

        defaultArrangers = reflectionHelper.createAllCustomArrangers();
        simplifiedArrangers = reflectionHelper.createAllCustomArrangers();
    }

    static EnhancedRandomBuilder instance() {
        if (instance == null) {
            instance = new EnhancedRandomBuilder();
        }
        return instance;
    }

    static EasyRandomParameters getEasyRandomDefaultParameters() {
        return new EasyRandomParameters()
                .collectionSizeRange(1, 4)
                .randomizationDepth(15)
                .objectPoolSize(30)
                .stringLengthRange(STRING_MIN_LENGTH, STRING_MAX_LENGTH);
    }

    static EasyRandomParameters getEasyRandomSimplifiedParameters() {
        return new EasyRandomParameters()
                .collectionSizeRange(0, 2)
                .randomizationDepth(3)
                .objectPoolSize(10)
                .stringLengthRange(5, 10);
    }

    EnhancedRandom buildDefaultRandom() {
        return buildRandom(defaultArrangers, (arrangers, seed) -> EnhancedRandom.of(arrangers, EnhancedRandomBuilder::getEasyRandomDefaultParameters, seed));
    }

    EnhancedRandom buildCustomDefaultRandom(Supplier<EasyRandomParameters> parametersSupplier) {
        return buildRandom(defaultArrangers, (arrangers, seed) -> EnhancedRandom.of(arrangers, parametersSupplier, seed));
    }

    EnhancedRandom buildSimplifiedRandom() {
        return buildRandom(simplifiedArrangers, (arrangers, seed) -> EnhancedRandom.of(arrangers, EnhancedRandomBuilder::getEasyRandomSimplifiedParameters, seed));
    }

    private EnhancedRandom buildRandom(Map<Class<?>, CustomArranger<?>> arrangers, BiFunction<Map<Class<?>, CustomArranger<?>>, Long, EnhancedRandom> enhancedRandomProducer) {
        arrangers.forEach((clazz, customArranger) -> {
            final HashMap<Class<?>, CustomArranger<?>> forCustomArranger = new HashMap<>(arrangers);
            forCustomArranger.remove(clazz);
            customArranger.setEnhancedRandom(enhancedRandomProducer.apply(forCustomArranger, (long) clazz.getName().hashCode()));
        });
        return enhancedRandomProducer.apply(arrangers, DEFAULT_SEED);
    }
}
