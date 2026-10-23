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

import java.lang.reflect.Field;

class MyEasyRandomParameters extends EasyRandomParameters {

    private int objectPoolSize;

    @Override
    public int getObjectPoolSize() {
        return this.objectPoolSize;
    }

    @Override
    public void setObjectPoolSize(int objectPoolSize) {
        this.objectPoolSize = objectPoolSize;
    }

    @Override
    public EasyRandomParameters copy() {
        MyEasyRandomParameters copy = new MyEasyRandomParameters();
        copy.setSeed(this.getSeed());
        copy.setObjectPoolSize(this.getObjectPoolSize());
        copy.setRandomizationDepth(this.getRandomizationDepth());
        copy.setCharset(this.getCharset());
        copy.setScanClasspathForConcreteTypes(this.isScanClasspathForConcreteTypes());
        copy.setOverrideDefaultInitialization(this.isOverrideDefaultInitialization());
        copy.setIgnoreRandomizationErrors(this.isIgnoreRandomizationErrors());
        copy.setBypassSetters(this.isBypassSetters());
        copy.setCollectionSizeRange(this.getCollectionSizeRange());
        copy.setStringLengthRange(this.getStringLengthRange());
        copy.setDateRange(this.getDateRange());
        copy.setTimeRange(this.getTimeRange());
        copy.setExclusionPolicy(this.getExclusionPolicy());
        copy.setObjectFactory(this.getObjectFactory());
        copy.setRandomizerProvider(this.getRandomizerProvider());
        setReflectively(copy,"customRandomizerRegistry", getReflectively("customRandomizerRegistry"));
        setReflectively(copy,"exclusionRandomizerRegistry", getReflectively("exclusionRandomizerRegistry"));
        setReflectively(copy,"userRegistries", getReflectively("userRegistries"));
        setReflectively(copy,"fieldExclusionPredicates", getReflectively("fieldExclusionPredicates"));
        setReflectively(copy,"typeExclusionPredicates", getReflectively("typeExclusionPredicates"));
        return copy;
    }

    private void setReflectively(MyEasyRandomParameters target, String fieldName, Object valueToSet) {
        try {
            Field field = target.getClass().getSuperclass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, valueToSet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Object getReflectively(String fieldName) {
        try {
            Field field = this.getClass().getSuperclass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}