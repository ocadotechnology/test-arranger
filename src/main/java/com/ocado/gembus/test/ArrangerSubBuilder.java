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
import io.github.benas.randombeans.api.Randomizer;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

class ArrangerSubBuilder {
    private Map<Class<?>, CustomArranger<?>> customArrangers;
    private boolean initializing = false;

    void createAllCustomArrangers(List<Constructor<?>> arrangerConstructors) {
        customArrangers = arrangerConstructors.stream()
                .map(constructor -> createCustomArranger(constructor))
                .filter(customArranger -> customArranger != null)
                .collect(Collectors.toMap(customArranger -> customArranger.type, customArranger -> customArranger,
                        (arrangerA, arrangerB) -> {
                            throw new IllegalArgumentException("There are two arrangers registered for " + arrangerA.type.getName()
                                    + ", those are " + arrangerA.getClass().getName() + " and " + arrangerB.getClass().getName());
                        }));
    }

    void configureEnhancedRandomBuilder(EnhancedRandomBuilder enhancedRandomBuilder, Optional<Class> target, Function<Optional<Class>, EnhancedRandom> nestedArrangerBuilder) {
        configureCustomArranger(target, enhancedRandomBuilder, customArrangers);
        if (!initializing) {
            initializing = true;
            customArrangers.forEach((clazz, customArranger) -> customArranger.setEnhancedRandom(nestedArrangerBuilder.apply(Optional.of(clazz))));
        }
    }

    private CustomArranger<?> createCustomArranger(Constructor<?> constructor) {
        try {
            return (CustomArranger) constructor.newInstance();
        } catch (Exception e) {
            System.err.println("Cannot create arranger for " + constructor.getName());;
            return null;
        }
    }

    private void configureCustomArranger(Optional<Class> target, EnhancedRandomBuilder enhancedRandomBuilder, Map<Class<?>, CustomArranger<?>> customArrangers) {
        for (Map.Entry<Class<?>, CustomArranger<?>> entry : customArrangers.entrySet()) {
            if (entry.getKey() != target.orElse(null)) {
                enhancedRandomBuilder.randomize(entry.getKey(), customArrangerToRandomizer(entry.getValue()));
            }
        }
    }

    private Randomizer<?> customArrangerToRandomizer(CustomArranger instance) {
        return () -> instance.instance();
    }

}
