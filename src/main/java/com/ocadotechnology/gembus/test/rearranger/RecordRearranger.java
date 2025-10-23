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
import org.jeasy.random.ObjectCreationException;

import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

class RecordRearranger {
    static <T> T copyRecord(T record, final Map<String, Supplier<?>> overrides) throws ReflectiveOperationException {
        @SuppressWarnings("unchecked")
        Class<T> recordClass = (Class<T>) record.getClass();
        validateOverrides(record.getClass(), overrides);

        RecordComponent[] components = recordClass.getRecordComponents();
        Object[] constructorParams = new Object[components.length];
        for (int i = 0; i < components.length; i++) {
            constructorParams[i] = getComponentValue(record, overrides, components[i]);
        }
        try {
            return RecordReflectionUtils.instantiateRecord(recordClass, constructorParams);
        } catch (ObjectCreationException e) {
            String message = "Failed rearranging record '" + recordClass.getName() + "' with overrides " + OverridesPrettyPrinter.describeOverrides(overrides);
            throw new IllegalArgumentException(message, e);
        }
    }

    private static <T> Object getComponentValue(T record, Map<String, Supplier<?>> overrides, RecordComponent param) throws ReflectiveOperationException {
        if (overrides.containsKey(param.getName())) {
            return overrides.get(param.getName()).get();
        } else {
            Method accessor = param.getAccessor();
            accessor.setAccessible(true);
            return accessor.invoke(record);
        }
    }

    /**
     * @throws IllegalArgumentException is thrown when the provided record does not contain field that is specified in the overrides map.
     */
    private static <T> void validateOverrides(Class<T> recordClass, final Map<String, Supplier<?>> overrides) throws IllegalArgumentException {
        Set<String> recordFieldNames = Arrays.stream(recordClass.getRecordComponents())
                .map(RecordComponent::getName)
                .collect(Collectors.toSet());
        Rearranger.validateOverrides(overrides, recordFieldNames, recordClass.getName());
    }
}
