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

import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.jeasy.random.api.Randomizer;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Class for random object generator.
 */
public class EnhancedRandom extends Random {

    final EasyRandom easyRandom;
    private final Map<Class<?>, CustomArranger<?>> arrangers;
    private final HashMap<Set<String>, EasyRandom> cache = new HashMap<>();
    private final Supplier<EasyRandomParameters> parametersSupplier;

    static class Builder {
        private final Supplier<EasyRandomParameters> parametersSupplier;

        Builder(Supplier<EasyRandomParameters> parametersSupplier) {
            this.parametersSupplier = parametersSupplier;
        }

        EnhancedRandom build(Map<Class<?>, CustomArranger<?>> customArrangersByTargetType, Long seed) {
            return new EnhancedRandom(customArrangersByTargetType, parametersSupplier, seed);
        }
    }

    private EnhancedRandom(Map<Class<?>, CustomArranger<?>> arrangers, Supplier<EasyRandomParameters> parametersSupplier, long seed) {
        this.arrangers = arrangers;
        this.parametersSupplier = parametersSupplier;
        EasyRandomParameters parameters = parametersSupplier.get();
        parameters.seed(seed);
        addRandomizersToParameters(Optional.empty(), parameters, arrangers);
        this.easyRandom = new EasyRandom(parameters);
    }

    /**
     * Generate a random instance of the given type.
     *
     * @param type           the type for which an instance will be generated
     * @param excludedFields the name of fields to exclude
     * @param <T>            the actual type of the target object
     * @return a random instance of the given type
     */
    public <T> T nextObject(final Class<T> type, final String... excludedFields) {
        if (excludedFields.length == 0) {
            return easyRandom.nextObject(type);
        } else {
            return createEasyRandomWithExclusions(excludedFields, type).nextObject(type);
        }
    }

    /**
     * Generate a stream of random instances of the given type.
     *
     * @param type           the type for which instances will be generated
     * @param amount         the number of instances to generate
     * @param excludedFields the name of fields to exclude
     * @param <T>            the actual type of the target objects
     * @return a stream of random instances of the given type
     */
    public <T> Stream<T> objects(final Class<T> type, final int amount, final String... excludedFields) {
        if (excludedFields.length == 0) {
            return easyRandom.objects(type, amount);
        } else {
            return createEasyRandomWithExclusions(excludedFields, type).objects(type, amount);
        }
    }

    private EasyRandom createEasyRandomWithExclusions(String[] excludedFields, Class type) {
        Set<String> fields = new HashSet<>(Arrays.asList(excludedFields));
        cache.computeIfAbsent(fields, key -> {
            EnhancedRandom enhancedRandom = ArrangersConfigurer.instance().randomForGivenConfiguration(type, arrangers, () -> addExclusionToParameters(fields));
            return enhancedRandom.easyRandom;
        });
        return cache.get(fields);
    }

    private EasyRandomParameters addExclusionToParameters(Set<String> fields) {
        final EasyRandomParameters parameters = parametersSupplier.get();
        for (String fieldName : fields) {
            parameters.excludeField(field -> field.getName().equals(fieldName));
        }
        return parameters;
    }

    private void addRandomizersToParameters(Optional<Class> typeToSkip, EasyRandomParameters parameters, Map<Class<?>, CustomArranger<?>> customArrangers) {
        for (Map.Entry<Class<?>, CustomArranger<?>> entry : customArrangers.entrySet()) {
            if (entry.getKey() != typeToSkip.orElse(null)) {
                final Class key = entry.getKey();
                final Randomizer randomizer = customArrangerToRandomizer(entry.getValue());
                parameters.randomize(key, randomizer);
            }
        }
        long newSeed = parameters.getSeed() + SeedHelper.customArrangerTypeSpecificSeedRespectingRandomSeedSetting(typeToSkip.orElse(CustomArranger.class));
        parameters.randomizerRegistry(new CustomArrangerRandomizerRegistry(newSeed));
    }

    private Randomizer<?> customArrangerToRandomizer(CustomArranger arranger) {
        return arranger::instance;
    }


}
