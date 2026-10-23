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

import org.jeasy.random.EasyRandomParameters;
import org.jeasy.random.api.RandomizerContext;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


class DecoratedObjectFactoryTest {

    DummyRandomizerContext randomizerContext = new DummyRandomizerContext();

    @Test
    void updateRandomizationContextWhenCacheIsDisabled() {
        //given
        DecoratedObjectFactory factory = new DecoratedObjectFactory(false);
        Class<?> type = DecoratedObjectFactoryTest.class;

        //when
        factory.createInstance(type, randomizerContext);

        //then
        assertThat(randomizerContext.getPopulatedBeans().get(type))
                .isNotNull()
                .isEmpty();
    }

    @Test
    void doNotUpdateRandomizationContextWhenCacheIsEnabled() {
        //given
        DecoratedObjectFactory factory = new DecoratedObjectFactory(true);
        Class<?> type = DecoratedObjectFactoryTest.class;

        //when
        factory.createInstance(type, randomizerContext);

        //then
        assertThat(randomizerContext.getPopulatedBeans().get(type))
                .isNull();
    }
}

class DummyRandomizerContext implements RandomizerContext {

    private Map<Class<?>, List<Object>> populatedBeans = new HashMap<>();

    public Map<Class<?>, List<Object>> getPopulatedBeans() {
        return populatedBeans;
    }

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

    @Override
    public boolean hasExceededRandomizationDepth() {
        return false;
    }
}