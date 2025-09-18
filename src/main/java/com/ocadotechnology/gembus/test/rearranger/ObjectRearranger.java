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
package com.ocadotechnology.gembus.test.rearranger;

import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ObjectRearranger {
    private static final Objenesis OBJENESIS = new ObjenesisStd();

    static <T> T copyObject(T objectToCopy, final Map<String, Supplier<?>> overrides) {
        @SuppressWarnings("unchecked")
        Class<T> type = (Class<T>) objectToCopy.getClass();
        T clone = instantiateEmptyObject(type);
        List<Field> fields = allInstanceFields(type);

        assureOverridesAreValid(overrides, fields, type);

        for (Field f : fields) {
            if (Modifier.isStatic(f.getModifiers()) || f.isSynthetic()) {
                continue;
            }
            //TODO t: refactor get/set through reflection into single place
            f.setAccessible(true);
            try {
                Object fieldDesiredValue = getFieldDesiredValue(objectToCopy, overrides, f);
                f.set(clone, fieldDesiredValue);
            } catch (IllegalAccessException e) {
                //TODO t: refactor exception handling into single place
                throw new RuntimeException(e);
            }
        }
        return clone;
    }

    //TODO t: refactor, similar solution for records
    private static <T> void assureOverridesAreValid(Map<String, Supplier<?>> overrides, List<Field> fields, Class<T> type) {
        overrides.keySet().forEach(k -> {
            if (fields.stream().noneMatch(f -> f.getName().equals(k))) {
                throw new IllegalArgumentException("Failed to override field " + k + " in class " + type.getName() + ". Field not found.");
            }
        });
    }

    private static <T> Object getFieldDesiredValue(T objectToCopy, Map<String, Supplier<?>> overrides, Field field) throws IllegalAccessException {
        if (overrides.containsKey(field.getName())) {
            return overrides.get(field.getName()).get();
        } else {
            return field.get(objectToCopy);
        }
    }

    private static <T> T instantiateEmptyObject(Class<T> type) {
        try {
            Constructor<T> noArg = type.getDeclaredConstructor();
            noArg.setAccessible(true);
            return noArg.newInstance();
        } catch (Exception ignored) {
            return OBJENESIS.newInstance(type);
        }
    }

    //TODO t: ReflectionUtils candidate
    private static List<Field> allInstanceFields(Class<?> type) {
        List<Field> result = new ArrayList<>();
        Class<?> current = type;
        while (current != null && current != Object.class) {
            for (Field f : current.getDeclaredFields()) {
                result.add(f);
            }
            current = current.getSuperclass();
        }
        return result;
    }
}
