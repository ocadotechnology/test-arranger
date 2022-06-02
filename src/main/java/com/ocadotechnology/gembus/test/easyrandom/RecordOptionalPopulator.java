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
import org.jeasy.random.util.ReflectionUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.util.Optional;

public class RecordOptionalPopulator {

    private final EasyRandom easyRandom;

    public RecordOptionalPopulator(EasyRandom easyRandom) {
        this.easyRandom = easyRandom;
    }

    public Optional<?> getRandomOptional(RecordComponent field, RandomizerContext randomizerContext) {
        Type type = field.getGenericType();
        if (ReflectionUtils.isParameterizedType(type)) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type genericType = parameterizedType.getActualTypeArguments()[0];
            if (ReflectionUtils.isPopulatable(genericType)) {
                Object item = AggressiveWrapper.doPopulateBean((Class<?>)genericType, easyRandom, randomizerContext);
                return Optional.of(item);
            } else {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }
}
