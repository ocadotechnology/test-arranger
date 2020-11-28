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

import org.jeasy.random.ObjectCreationException;
import org.jeasy.random.ObjenesisObjectFactory;
import org.jeasy.random.api.ObjectFactory;
import org.jeasy.random.api.RandomizerContext;
import org.jeasy.random.util.ReflectionUtils;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;

public class NullSafeObjectFactory implements ObjectFactory {
    private ObjenesisObjectFactory originalFactory = new ObjenesisObjectFactory();

    @Override
    public <T> T createInstance(Class<T> type, RandomizerContext context) throws ObjectCreationException {
        try {
            T result = originalFactory.createInstance(type, context);
            if (isItDeepestRandomizationDepth(context)) {
                ReflectionUtils.getDeclaredFields(result).forEach(field -> {
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
            return result;
        } catch (Exception e) {
            throw new ObjectCreationException("Unable to create a new instance of " + type, e);
        }
    }

    @Nullable
    private Object produceEmptyValueForField(Class<?> fieldType) {
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

    private boolean isItDeepestRandomizationDepth(RandomizerContext context) {
        return context.getCurrentRandomizationDepth() == context.getParameters().getRandomizationDepth() - 1;
    }
}
