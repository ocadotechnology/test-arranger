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
import org.jeasy.random.api.RandomizerContext;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.util.Collection;

import static org.jeasy.random.util.ReflectionUtils.*;

public class RecordCollectionPopulator {

    private final EasyRandom easyRandom;

    public RecordCollectionPopulator(EasyRandom easyRandom) {
        this.easyRandom = easyRandom;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public Collection<?> getRandomCollection(RecordComponent field, RandomizerContext randomizerContext) {
        int randomSize = AggressiveWrapper.getRandomCollectionSize(easyRandom, randomizerContext);
        Class<?> fieldType = field.getType();
        Type fieldGenericType = field.getGenericType();
        Collection collection;

        if (isInterface(fieldType)) {
            collection = getEmptyImplementationForCollectionInterface(fieldType);
        } else {
            collection = createEmptyCollectionForType(fieldType, randomSize);
        }

        if (isParameterizedType(fieldGenericType)) {
            ParameterizedType parameterizedType = (ParameterizedType) fieldGenericType;
            Type type = parameterizedType.getActualTypeArguments()[0];
            if (isPopulatable(type)) {
                for (int i = 0; i < randomSize; i++) {
                    Object item = AggressiveWrapper.doPopulateBean((Class<?>)type, easyRandom, randomizerContext);
                    collection.add(item);
                }

            }
        }
        return collection;
    }
}
