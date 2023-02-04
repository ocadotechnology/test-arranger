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
import com.ocadotechnology.gembus.test.easyrandom.RecordObjectFactory;
import org.jeasy.random.ObjectCreationException;
import org.jeasy.random.ObjenesisObjectFactory;
import org.jeasy.random.api.ObjectFactory;
import org.jeasy.random.api.RandomizerContext;

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
    private final RecordObjectFactory recordFactory = new RecordObjectFactory(this);
    private final boolean cacheEnable;

    public DecoratedObjectFactory(boolean cacheEnable) {
        this.cacheEnable = cacheEnable;
    }

    @Override
    public <T> T createInstance(Class<T> type, RandomizerContext context) throws ObjectCreationException {
        try {
            T result = InstanceProducerHelper.createLeafInstance(originalFactory, type, context);
            if (!cacheEnable) {
                disableCache(type, context);
            }
            if (type.isRecord()) {
                return recordFactory.createRandomRecord(type, context);
            }
            if (DepthLimitationObjectFactory.isItDeepestRandomizationDepth(context, context.getCurrentRandomizationDepth())) {
                InstanceProducerHelper.initializeLeafInstance(type, result);
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

}
