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

import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;

class RecordObjectFactory {

    static <T> T createRandomRecord(Class<T> recordType) {
        try {
            Object[] randomValues = Arrays.stream(recordType.getRecordComponents())
                    .map(r -> Arranger.some(r.getType()))
                    .toArray(Object[]::new);
            return getConstructor(recordType).newInstance(randomValues);
        } catch (Exception e) {
            throw new ObjectCreationException("Unable to create a random instance of recordType " + recordType, e);
        }
    }

    static private <T> Constructor<T> getConstructor(Class<T> recordType) throws NoSuchMethodException {
        Class<?>[] componentTypes = Arrays.stream(recordType.getRecordComponents())
                .map(RecordComponent::getType)
                .toArray(Class[]::new);
        return recordType.getDeclaredConstructor(componentTypes);
    }
}
