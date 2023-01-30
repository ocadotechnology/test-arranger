package com.ocadotechnology.gembus.test;

import org.jeasy.random.EasyRandomParameters;
import org.jeasy.random.ObjenesisObjectFactory;
import org.jeasy.random.api.RandomizerContext;
import org.jeasy.random.util.ReflectionUtils;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;

public class InstanceProducerHelper {

    private static final ObjenesisObjectFactory dummyFactory = new ObjenesisObjectFactory();
    private static final RandomizerContext dummyRandomizerContext = new RandomizerContext() {
        @Override
        public Class<?> getTargetType() {
            return null;
        }

        @Override
        public Object getRootObject() {
            return null;
        }

        @Override
        public Object getCurrentObject() {
            return null;
        }

        @Override
        public String getCurrentField() {
            return null;
        }

        @Override
        public int getCurrentRandomizationDepth() {
            return 0;
        }

        @Override
        public EasyRandomParameters getParameters() {
            return new EasyRandomParameters();
        }
    };

    public static <T> T createEmptyInstance(ObjenesisObjectFactory factory, Class<T> type, RandomizerContext context) {
        return factory.createInstance(type, context);
    }

    public static <T> T createEmptyInstance(Class<T> type) {
        return dummyFactory.createInstance(type, dummyRandomizerContext);
    }

    /** It's a leaf in a nesting structure, i.e. initialized with nulls and empty collections. */
    public static <T> void initializeLeafInstance(Class<T> type, T result) {
        ReflectionUtils.getDeclaredFields(result).stream()
                .filter(field -> !field.isSynthetic())
                .forEach(field -> {
                    try {
                        Object emptyOne = produceEmptyValueForField(field.getType());
                        if (emptyOne != null) {
                            ReflectionUtils.setProperty(result, field, emptyOne);
                        }
                    } catch (Exception e) {
                        System.err.println("Unable to set " + type.getName() + "." + field.getName() + ". " + e.getMessage());
                    }
                });
    }

    @Nullable
    private static Object produceEmptyValueForField(Class<?> fieldType) {
        if (ReflectionUtils.isArrayType(fieldType)) {
            return Array.newInstance(fieldType.getComponentType(), 0);
        }
        if (ReflectionUtils.isCollectionType(fieldType)) {
            return ReflectionUtils.getEmptyImplementationForCollectionInterface(fieldType);
        }
        if (ReflectionUtils.isMapType(fieldType)) {
            return ReflectionUtils.getEmptyImplementationForMapInterface(fieldType);
        }
        return null;
    }
}
