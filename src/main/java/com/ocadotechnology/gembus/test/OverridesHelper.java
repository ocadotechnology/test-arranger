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

import org.jeasy.random.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.function.Supplier;

public class OverridesHelper {

    static <T> void applyOverrides(T result, Map<String, Supplier<?>> overrides) {
        Class type = result.getClass();
        overrides.forEach((key, value) -> {
            try {
                Field field = result.getClass().getDeclaredField(key);
                ReflectionUtils.setFieldValue(result, field, value.get());
            } catch (NoSuchFieldException e) {
                System.err.println("Field selected for an override i.e. " + key + " was not found in class " + type.getName());
            } catch (IllegalArgumentException | IllegalAccessException e) {
                System.err.println("Access rejected when trying to override " + key + " field in class " + type.getName() + ": " + e.getMessage());
            }
        });
    }
}
