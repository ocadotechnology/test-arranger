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
                .collect(Collectors.toMap(customArranger -> customArranger.type, customArranger -> customArranger));
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
            e.printStackTrace();
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
        return () -> instance.some();
    }

}
