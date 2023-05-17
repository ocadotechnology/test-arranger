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
package com.ocadotechnology.gembus.test.experimental;

import com.ocadotechnology.gembus.test.easyrandom.RecordReflectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * This solution was inspired by the Kotlin data class copy method.
 * <p>Please note that it is currently an experimental implementation, and its API may be subject to substantial changes.</p>
 */
public class Data {
    /**
     * This function creates a shallow copy of a provided record while allowing you to override values for selected fields within the record.
     * Works only with Java Records!
     *
     * @param record    the record that will be copied
     * @param overrides the map contains the fields that should be overridden, where the field name should be used as the key in the map entry.
     *                  The corresponding value should be a supplier that provides the value that should be set on the field.
     * @return a new object that is a shallow copy of the provided record.
     */
    public static <T> T copy(T record, final Map<String, Supplier<?>> overrides) {
        Class<T> recordClass = (Class<T>) record.getClass();
        if (recordClass.isRecord()) {
            Object[] constructorParams = Arrays.stream(recordClass.getRecordComponents())
                    .map(recordParamsCopyWithOverrides(record, overrides))
                    .toArray(Object[]::new);
            return RecordReflectionUtils.instantiateRecord(recordClass, constructorParams);
        } else {
            throw new IllegalArgumentException("Copy is supported only for records");
        }
    }

    private static Function<RecordComponent, Object> recordParamsCopyWithOverrides(Object source, Map<String, Supplier<?>> overrides) {
        return param -> {
            if (overrides.containsKey(param.getName())) {
                return overrides.get(param.getName()).get();
            } else {
                try {
                    return param.getAccessor().invoke(source);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }
}
