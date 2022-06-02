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

import java.lang.reflect.Array;
import java.lang.reflect.RecordComponent;

public class RecordArrayPopulator {

    private final EasyRandom easyRandom;

    public RecordArrayPopulator(EasyRandom easyRandom) {
        this.easyRandom = easyRandom;
    }

    public Object getRandomArray(RecordComponent field, RandomizerContext randomizerContext) {
        int randomSize = AggressiveWrapper.getRandomCollectionSize(easyRandom, randomizerContext);
        Class<?> fieldType = field.getType();
        Class<?> type = fieldType.getComponentType();
        Object result = Array.newInstance(type, randomSize);
        for (int i = 0; i < randomSize; i++) {
            Object randomElement = AggressiveWrapper.doPopulateBean((Class<?>)type, easyRandom, randomizerContext);
            Array.set(result, i, randomElement);
        }
        return result;
    }
}
