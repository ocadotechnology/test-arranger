package com.ocadotechnology.gembus.test;

import org.jeasy.random.EasyRandomParameters;
import org.jeasy.random.annotation.Priority;
import org.jeasy.random.api.Randomizer;
import org.jeasy.random.api.RandomizerRegistry;
import org.jeasy.random.randomizers.misc.BooleanRandomizer;
import org.jeasy.random.randomizers.number.*;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * The priority should be lower than the BeanValidationRandomizerRegistry one but greater than the InternalRandomizerRegistry one
 */
@Priority(-3)
public class CustomArrangerRandomizerRegistry implements RandomizerRegistry {
    private final long seed;
    private final Map<Class<?>, Randomizer<?>> randomizers = new HashMap<>();

    public CustomArrangerRandomizerRegistry(long seed) {
        this.seed = seed + 1;
    }

    @Override
    public void init(EasyRandomParameters parameters) {
        randomizers.put(Boolean.class, new BooleanRandomizer(seed));
        randomizers.put(Byte.class, new ByteRandomizer(seed));
        randomizers.put(Short.class, new ShortRandomizer(seed));
        randomizers.put(Integer.class, new IntegerRandomizer(seed));
        randomizers.put(Long.class, new LongRandomizer(seed));
        randomizers.put(Double.class, new DoubleRandomizer(seed));
        randomizers.put(Float.class, new FloatRandomizer(seed));
    }

    @Override
    public Randomizer<?> getRandomizer(final Field field) {
        return getRandomizer(field.getType());
    }

    @Override
    public Randomizer<?> getRandomizer(Class<?> type) {
        return randomizers.get(type);
    }
}
