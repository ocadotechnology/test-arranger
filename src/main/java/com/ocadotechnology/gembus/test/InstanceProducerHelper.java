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

import com.ocadotechnology.gembus.test.easyrandom.DepthLimitationObjectFactory;
import org.jeasy.random.EasyRandomParameters;
import org.jeasy.random.ObjenesisObjectFactory;
import org.jeasy.random.api.RandomizerContext;
import org.jeasy.random.util.ReflectionUtils;

public class InstanceProducerHelper {

    private static final ObjenesisObjectFactory dummyFactory = new ObjenesisObjectFactory();
    private static final RandomizerContext dummyRandomizerContext = new RandomizerContext() {
        @Override
        public Class<?> getTargetType() {
            return null;
        }

        @Override
        public Object getRootObject() {
            return null;
        }

        @Override
        public Object getCurrentObject() {
            return null;
        }

        @Override
        public String getCurrentField() {
            return null;
        }

        @Override
        public int getCurrentRandomizationDepth() {
            return 0;
        }

        @Override
        public EasyRandomParameters getParameters() {
            return new EasyRandomParameters();
        }
    };

    public static <T> T createLeafInstance(ObjenesisObjectFactory factory, Class<T> type, RandomizerContext context) {
        return factory.createInstance(type, context);
    }

    public static <T> T createLeafInstance(Class<T> type) {
        T result = dummyFactory.createInstance(type, dummyRandomizerContext);
        initializeLeafInstance(type, result);
        return result;
    }

    /** It's a leaf in a nesting structure, i.e. initialized with nulls and empty collections. */
    public static <T> void initializeLeafInstance(Class<T> type, T result) {
        ReflectionUtils.getDeclaredFields(result).stream()
                .filter(field -> !field.isSynthetic())
                .forEach(field -> {
                    try {
                        Object emptyOne = DepthLimitationObjectFactory.produceEmptyValueForField(field.getType());
                        if (emptyOne != null) {
                            ReflectionUtils.setProperty(result, field, emptyOne);
                        }
                    } catch (Exception e) {
                        System.err.println("Unable to set " + type.getName() + "." + field.getName() + ". " + e.getMessage());
                    }
                });
    }
}
