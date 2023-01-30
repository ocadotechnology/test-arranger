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

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

class ArrangersConfigurer {

    static final int STRING_MIN_LENGTH = 9;
    static final int STRING_MAX_LENGTH = 16;
    static final int CACHE_SIZE = 15;
    static final int MAX_RANDOMIZATION_DEPTH = 4;
    private static ArrangersConfigurer instance;

    private final Map<Class<?>, CustomArranger<?>> defaultArrangers;
    private final Map<Class<?>, CustomArranger<?>> simplifiedArrangers;

    private ArrangersConfigurer() {
        final ReflectionHelper reflectionHelper = new ReflectionHelper();

        defaultArrangers = reflectionHelper.createAllCustomArrangers();
        simplifiedArrangers = reflectionHelper.createAllCustomArrangers();
    }

    static ArrangersConfigurer instance() {
        if (instance == null) {
            instance = new ArrangersConfigurer();
        }
        return instance;
    }

    static EasyRandomParameters getEasyRandomDefaultParameters() {
        return sharedParameters()
                .collectionSizeRange(1, 4)
                .randomizationDepth(MAX_RANDOMIZATION_DEPTH)
                .stringLengthRange(STRING_MIN_LENGTH, STRING_MAX_LENGTH);
    }

    static EasyRandomParameters getEasyRandomSimplifiedParameters() {
        return sharedParameters()
                .collectionSizeRange(0, 2)
                .randomizationDepth(2)
                .stringLengthRange(5, 10);
    }

    EnhancedRandom defaultRandom() {
        return randomWithArrangers(defaultArrangers, new EnhancedRandom.Builder(ArrangersConfigurer::getEasyRandomDefaultParameters));
    }

    EnhancedRandom simplifiedRandom() {
        return randomWithArrangers(simplifiedArrangers, new EnhancedRandom.Builder(ArrangersConfigurer::getEasyRandomSimplifiedParameters));
    }

    EnhancedRandom randomForGivenConfiguration(Class<?> type, Map<Class<?>, CustomArranger<?>> arrangers, Supplier<EasyRandomParameters> parametersSupplier) {
        EnhancedRandom.Builder randomBuilder = new EnhancedRandom.Builder(parametersSupplier);
        CustomArranger<?> arrangerToUpdate = arrangers.get(type);
        if (arrangerToUpdate == null) {
            return randomBuilder.build(arrangers, SeedHelper.calculateSeed());
        } else {
            return randomWithoutSelfReferenceThroughArranger(arrangers, randomBuilder, type);
        }
    }

    private static EasyRandomParameters sharedParameters() {
        return new MyEasyRandomParameters()
                .objectPoolSize(calculateObjectPoolSize())
                .objectFactory(new DecoratedObjectFactory(PropertiesWrapper.getCacheEnable()))
                .excludeField(Field::isSynthetic)
                .seed(SeedHelper.calculateSeed())
                .overrideDefaultInitialization(PropertiesWrapper.getOverrideDefaults());
    }

    private static int calculateObjectPoolSize() {
        if (PropertiesWrapper.getCacheEnable()) {
            return CACHE_SIZE;
        } else {
            return -1;
        }
    }

    private EnhancedRandom randomWithArrangers(Map<Class<?>, CustomArranger<?>> arrangers, EnhancedRandom.Builder randomBuilder) {
        arrangers.forEach((clazz, customArranger) -> {
            EnhancedRandom random = randomWithoutSelfReferenceThroughArranger(arrangers, randomBuilder, clazz);
            customArranger.setEnhancedRandom(random);
        });
        return randomBuilder.build(arrangers, SeedHelper.calculateSeed());
    }

    private EnhancedRandom randomWithoutSelfReferenceThroughArranger(Map<Class<?>, CustomArranger<?>> arrangers, EnhancedRandom.Builder enhancedRandomBuilder, Class<?> type) {
        final HashMap<Class<?>, CustomArranger<?>> forCustomArranger = new HashMap<>(arrangers);
        forCustomArranger.remove(type);
        return enhancedRandomBuilder.build(forCustomArranger, SeedHelper.customArrangerTypeSpecificSeedRespectingRandomSeedSetting(type));
    }
}
