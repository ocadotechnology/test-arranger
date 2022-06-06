package com.ocadotechnology.gembus.test;

import org.jeasy.random.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.function.Supplier;

public class OverridesHelper {
    static <T> void applyOverrides(T result, Map<String, Supplier<?>> overrides) {
        Class type = result.getClass();
        overrides.forEach((key, value) -> {
            try {
                Field field = result.getClass().getDeclaredField(key);
                ReflectionUtils.setFieldValue(result, field, value.get());
            } catch (NoSuchFieldException e) {
                System.err.println("Field selected for an override i.e. " + key + " was not found in class " + type.getName());
            } catch (IllegalArgumentException | IllegalAccessException e) {
                System.err.println("Access rejected when trying to override " + key + " field in class " + type.getName() + ": " + e.getMessage());
            }
        });
    }
}
