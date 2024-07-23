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

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.function.Function.identity;

/**
 * Class for random object generator.
 */
public class EnhancedRandom extends Random {

    final EasyRandom easyRandom;
    private final Map<Class<?>, CustomArranger<?>> arrangers;
    private final Map<Set<String>, EasyRandom> cache = new ConcurrentHashMap<>();
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
     * @param excludedFields the names of the fields that will be ignored during instance initialization, this param is not automatically propagated to custom arrangers
     * @param <T>            the actual type of the target object
     * @return a random instance of the given type
     */
    public <T> T nextObject(final Class<T> type, final String... excludedFields) {
        final EasyRandom selectedEasyRandom = selectEasyRandomWithRespectToExclusion(type, excludedFields);
        return NestingSafeExecutor.execute(type, () -> selectedEasyRandom.nextObject(type));
    }

    /**
     * Generate a stream of random instances of the given type.
     *
     * @param type           the type for which instances will be generated
     * @param amount         the number of instances to generate
     * @param excludedFields the names of the fields that will be ignored during instance initialization, this param is not automatically propagated to custom arrangers
     * @param <T>            the actual type of the target objects
     * @return a stream of random instances of the given type
     */
    public <T> Stream<T> objects(final Class<T> type, final int amount, final String... excludedFields) {
        final EasyRandom selectedEasyRandom = selectEasyRandomWithRespectToExclusion(type, excludedFields);
        return selectedEasyRandom.objects(type, amount);
    }

    private <T> EasyRandom selectEasyRandomWithRespectToExclusion(Class<T> type, String[] excludedFields) {
        if (newEasyRandomWithCustomRandomizersOrFieldExclusionConfigIsRequired(type, excludedFields)) {
            return createEasyRandomWithCustomRandomizersAndExclusions(type, excludedFields);
        }
        return easyRandom;
    }

    private <T> boolean newEasyRandomWithCustomRandomizersOrFieldExclusionConfigIsRequired(Class<T> type, String[] excludedFields) {
        /* There is a logical inconsistency in using a custom arranger and field exclusion for the same type - the
         * exclusion can be configured in the custom arranger. Technically, creating an arranger with exclusion disables
         * the custom arranger for the type that is being instantiated. */
        return !arrangers.containsKey(type) && (excludedFields.length != 0 || isSealedInterface(type) || !sealedInterfaceFields(type).isEmpty());
    }

    private <T> EasyRandom createEasyRandomWithCustomRandomizersAndExclusions(Class<T> type, String[] excludedFields) {
        Set<String> fields = new HashSet<>(Arrays.asList(excludedFields));
        var forSealedInterfaces = createCustomArrangersForSealedInterfaces(type, fields);
        Set<String> cacheKey = getCacheKey(fields, forSealedInterfaces.keySet());
        cache.computeIfAbsent(cacheKey, key -> {
            HashMap<Class<?>, CustomArranger<?>> enhancedArrangers = new HashMap<>(arrangers);
            enhancedArrangers.putAll(forSealedInterfaces);
            EnhancedRandom er = ArrangersConfigurer.instance()
                    .randomForGivenConfiguration(type, !forSealedInterfaces.containsKey(type), enhancedArrangers, () -> addExclusionToParameters(fields));
            return er.easyRandom;
        });
        return cache.get(cacheKey);
    }

    private Set<String> getCacheKey(Set<String> fields, Set<Class<?>> sealedInterfaces) {
        Set<String> cacheKey = new HashSet<>(fields);
        cacheKey.addAll(sealedInterfaces.stream().map(Class::getName).toList());
        return cacheKey;
    }

    private <T> Map<Class<?>, CustomArranger<?>> createCustomArrangersForSealedInterfaces(Class<T> type,
                                                                                          Set<String> excludedFields) {
        HashMap<Class<?>, CustomArranger<?>> sealedInterfaceArrangers = new HashMap<>();
        if (isSealedInterface(type)) {
            sealedInterfaceArrangers.put(type, new SealedInterfaceArranger<T>(type));
        }
        sealedInterfaceArrangers.putAll(sealedInterfaceFields(type)
                .entrySet()
                .stream()
                .filter(entry -> !excludedFields.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toMap(identity(), SealedInterfaceArranger::new)));
        return sealedInterfaceArrangers;
    }

    private <T> Map<String, Class<?>> sealedInterfaceFields(Class<T> type) {
        return Arrays.stream(type.getDeclaredFields())
                .filter(field -> isSealedInterface(field.getType()))
                .collect(Collectors.toMap(Field::getName, Field::getType));
    }

    private static boolean isSealedInterface(Class<?> type) {
        return type.isSealed() && type.isInterface();
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
