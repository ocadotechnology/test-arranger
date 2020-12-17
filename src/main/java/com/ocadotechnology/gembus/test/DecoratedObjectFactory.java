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
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Delegates instance creation to EasyRandom object factory but decorates the process by
 * avoiding null in nested objects
 * and disabling cache to prevent the possibility of infinite loops in nested objects.
 */
public class DecoratedObjectFactory implements ObjectFactory {
    private final ObjenesisObjectFactory originalFactory = new ObjenesisObjectFactory();
    private final boolean cacheEnable;

    public DecoratedObjectFactory(boolean cacheEnable) {
        this.cacheEnable = cacheEnable;
    }

    @Override
    public <T> T createInstance(Class<T> type, RandomizerContext context) throws ObjectCreationException {
        try {
            T result = originalFactory.createInstance(type, context);
            if (!cacheEnable) {
                disableCache(type, context);
            }
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

    private <T> void disableCache(Class<T> type, RandomizerContext context) {
        try {
            Field populatedBeans = context.getClass().getDeclaredField("populatedBeans");
            populatedBeans.setAccessible(true);
            Map<Class<?>, List<Object>> cache = (Map<Class<?>, List<Object>>) populatedBeans.get(context);
            cache.put(type, Collections.emptyList());
        } catch (Exception e) {
            e.printStackTrace();
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
