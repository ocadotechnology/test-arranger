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

import java.lang.reflect.ParameterizedType;
import java.util.HashMap;


/**
 * <p>Extend this class to provide custom Arranger implementations.
 * It will be automatically picked up by {@link Arranger} and used whenever a new instance of {@code T} is created by the {@link Arranger}.</p>
 * <p>
 * Custom Arranger example (in kotlin):
 * <pre>{@code
 *  class EntityArranger : CustomArranger<Entity>() {
 *       override fun instance(): Entity = enhancedRandom.nextObject(Entity::class.java).copy(field=fixedValue)
 *  }
 * }</pre>
 */
public abstract class CustomArranger<T> {

    protected EnhancedRandom enhancedRandom = null;
    protected final Class<T> type;

    protected CustomArranger() {
        this.type = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        initEnhancedRandom();
    }

    protected CustomArranger(Class<T> type) {
        this.type = type;
        initEnhancedRandom();
    }

    private void initEnhancedRandom() {
        if (ArrangersConfigurer.defaultInitialized.get()) {
            enhancedRandom = ArrangersConfigurer.instance().defaultRandom();
        } else {
            enhancedRandom = new EnhancedRandom.Builder(ArrangersConfigurer::getEasyRandomDefaultParameters).build(new HashMap<>(), SeedHelper.calculateSeed());
        }
    }

    /**
     * <p>Method for internal Arranger creation of objects of type T.
     * Do no use this method directly to generate test objects. Use {@code some<T>()} in Kotlin and {@link Arranger#some(Class, String...)} in Java instead.</p>
     *
     * <p>However, never use {@code some<T>()} nor {@link Arranger#some(Class, String...)} in the {@link CustomArranger#instance()} method implementation.
     * Inside the method the {@code enhancedRandom} field should be used instead.</p>
     *
     * @return instance of type T, by default a random one
     */
    protected T instance() {
        return enhancedRandom.nextObject(type);
    }

    final void setEnhancedRandom(EnhancedRandom enhancedRandom) {
        this.enhancedRandom = enhancedRandom;
    }
}
