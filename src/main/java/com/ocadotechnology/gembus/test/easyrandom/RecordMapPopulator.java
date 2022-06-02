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
package com.ocadotechnology.gembus.test.easyrandom;

import org.jeasy.random.AggressiveWrapper;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.api.ObjectFactory;
import org.jeasy.random.api.RandomizerContext;
import org.jeasy.random.util.ReflectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.util.EnumMap;
import java.util.Map;

import static org.jeasy.random.util.ReflectionUtils.isInterface;
import static org.jeasy.random.util.ReflectionUtils.isParameterizedType;

public class RecordMapPopulator {

    private final EasyRandom easyRandom;
    private final ObjectFactory objectFactory;

    public RecordMapPopulator(EasyRandom easyRandom, ObjectFactory objectFactory) {
        this.easyRandom = easyRandom;
        this.objectFactory = objectFactory;
    }

    public Map<?, ?> getRandomMap(RecordComponent field, RandomizerContext context) {
        int randomSize = AggressiveWrapper.getRandomCollectionSize(easyRandom, context);
        Class<?> fieldType = field.getType();
        Type fieldGenericType = field.getGenericType();
        Map<Object, Object> map;

        if (isInterface(fieldType)) {
            map = (Map<Object, Object>) ReflectionUtils.getEmptyImplementationForMapInterface(fieldType);
        } else {
            try {
                map = (Map<Object, Object>) fieldType.getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                // Creating EnumMap with objenesis by-passes the constructor with keyType which leads to CCE at insertion time
                if (fieldType.isAssignableFrom(EnumMap.class)) {
                    if (isParameterizedType(fieldGenericType)) {
                        Type type = ((ParameterizedType) fieldGenericType).getActualTypeArguments()[0];
                        map = new EnumMap((Class<?>) type);
                    } else {
                        return null;
                    }
                } else {
                    map = (Map<Object, Object>) objectFactory.createInstance(fieldType, context);
                }
            }
        }

        if (ReflectionUtils.isParameterizedType(fieldGenericType)) { // populate only parameterized types, raw types will be empty
            ParameterizedType parameterizedType = (ParameterizedType) fieldGenericType;
            Type keyType = parameterizedType.getActualTypeArguments()[0];
            Type valueType = parameterizedType.getActualTypeArguments()[1];
            if (ReflectionUtils.isPopulatable(keyType) && ReflectionUtils.isPopulatable(valueType)) {
                for (int index = 0; index < randomSize; index++) {
                    Object randomKey = AggressiveWrapper.doPopulateBean((Class<?>) keyType, easyRandom, context);
                    Object randomValue = AggressiveWrapper.doPopulateBean((Class<?>) valueType, easyRandom, context);
                    if (randomKey != null) {
                        map.put(randomKey, randomValue);
                    }
                }
            }
        }
        return map;
    }
}
