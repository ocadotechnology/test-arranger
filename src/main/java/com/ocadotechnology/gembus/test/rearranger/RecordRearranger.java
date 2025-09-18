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

import com.ocadotechnology.gembus.test.easyrandom.RecordReflectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

class RecordRearranger {
    static <T> T copyRecord(T record, final Map<String, Supplier<?>> overrides) {
        Class<T> recordClass = (Class<T>) record.getClass();
        validateOverrides(record.getClass(), overrides);
        Object[] constructorParams = Arrays.stream(recordClass.getRecordComponents())
                .map(param -> getComponentValue(record, overrides, param))
                .toArray(Object[]::new);
        return RecordReflectionUtils.instantiateRecord(recordClass, constructorParams);
    }

    private static <T> Object getComponentValue(T record, Map<String, Supplier<?>> overrides, RecordComponent param) {
        if (overrides.containsKey(param.getName())) {
            return overrides.get(param.getName()).get();
        } else {
            try {
                Method accessor = param.getAccessor();
                accessor.setAccessible(true);
                return accessor.invoke(record);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * @throws IllegalArgumentException is thrown when the provided record does not contain field that is specified in the overrides map.
     */
    private static <T> void validateOverrides(Class<T> recordClass, final Map<String, Supplier<?>> overrides) throws IllegalArgumentException {
        Set<String> recordFieldNames = Arrays.stream(recordClass.getRecordComponents())
                .map(RecordComponent::getName)
                .collect(Collectors.toSet());
        overrides.keySet().forEach(key -> {
            if (!recordFieldNames.contains(key)) {
                throw new IllegalArgumentException("Failed to override field " + key + " in class " + recordClass.getName());
            }
        });
    }
}
