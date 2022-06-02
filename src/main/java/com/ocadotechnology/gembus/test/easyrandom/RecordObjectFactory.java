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

import com.ocadotechnology.gembus.test.CurrentEnhancedRandom;
import org.jeasy.random.api.ObjectFactory;
import org.jeasy.random.api.RandomizerContext;
import org.jeasy.random.util.ReflectionUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class RecordObjectFactory {

    private static final int INITIAL_DEPTH = 0;
    private static final ThreadLocal<Integer> DEPTH_COUNT = ThreadLocal.withInitial(() -> INITIAL_DEPTH);
    private final ObjectFactory objectFactory;

    public RecordObjectFactory(ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
    }

    public <T> T createRandomRecord(Class<T> recordType, RandomizerContext context) {
        try {
            increaseDepthCount(context);
            Function<RecordComponent, Object> arrangeRandom = r -> getSome(r, context);
            Function<RecordComponent, Object> arrangeEmpty = r -> {
                Object value = DepthLimitationObjectFactory.produceEmptyValueForField(r.getType());
                if (value == null && !r.getType().isRecord()) {
                    value = getSome(r, context);
                }
                return value;
            };

            Object[] randomValues;
            if (DepthLimitationObjectFactory.isItDeepestRandomizationDepth(context, DEPTH_COUNT.get())) {
                randomValues = arrangeConstructorParams(recordType, arrangeEmpty);
            } else {
                randomValues = arrangeConstructorParams(recordType, arrangeRandom);
            }
            return RecordReflectionUtils.instantiateRecord(recordType, randomValues);
        } finally {
            decreaseDepthCount();
        }
    }

    @NotNull
    private <T> Object[] arrangeConstructorParams(Class<T> recordType, Function<RecordComponent, Object> arrangeRandom) {
        return Arrays.stream(recordType.getRecordComponents())
                .map(arrangeRandom)
                .toArray(Object[]::new);
    }

    private void decreaseDepthCount() {
        Integer currentDepth = DEPTH_COUNT.get();
        DEPTH_COUNT.set(currentDepth - 1);
    }

    private void increaseDepthCount(RandomizerContext context) {
        Integer currentDepth = DEPTH_COUNT.get();
        if (currentDepth == INITIAL_DEPTH) {
            currentDepth = context.getCurrentRandomizationDepth();
        }
        currentDepth = currentDepth + 1;
        DEPTH_COUNT.set(currentDepth);
    }

    private Object getSome(RecordComponent recordComponent, RandomizerContext context) {
        Class<?> fieldType = recordComponent.getType();
        if (ReflectionUtils.isArrayType(fieldType)) {
            return getRandomArray(recordComponent, context);
        } else if (ReflectionUtils.isCollectionType(fieldType)) {
            return getRandomCollection(recordComponent, context);
        } else if (ReflectionUtils.isOptionalType(fieldType)) {
            return getRandomOptional(recordComponent, context);
        } else if (ReflectionUtils.isMapType(fieldType)) {
            return getRandomMap(recordComponent, context, objectFactory);
        } else {
            return CurrentEnhancedRandom.get().nextObject(fieldType);
        }
    }

    private static Optional<?> getRandomOptional(RecordComponent field, RandomizerContext context) {
        return new RecordOptionalPopulator(CurrentEnhancedRandom.getEasyRandom()).getRandomOptional(field, context);
    }

    private static Collection<?> getRandomCollection(RecordComponent field, RandomizerContext context) {
        return new RecordCollectionPopulator(CurrentEnhancedRandom.getEasyRandom()).getRandomCollection(field, context);
    }

    private static Object getRandomArray(RecordComponent field, RandomizerContext context) {
        return new RecordArrayPopulator(CurrentEnhancedRandom.getEasyRandom()).getRandomArray(field, context);
    }

    private Map<?, ?> getRandomMap(RecordComponent field, RandomizerContext context, ObjectFactory objectFactory) {
        return new RecordMapPopulator(CurrentEnhancedRandom.getEasyRandom(), objectFactory).getRandomMap(field, context);
    }

}


