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

import java.util.Objects;
import java.util.stream.Stream;

public class AndroidReflectionHelper {
    static Stream<Class<CustomArranger>> findCustomArrangerClasses() {
        final ClassLoader classLoader = AndroidReflectionHelper.class.getClassLoader();
        return PropertiesWrapper.getAndroidCustomArrangers().stream()
                .map(className -> {
                    Class<CustomArranger> result = null;
                    try {
                        Class<?> aClass = classLoader.loadClass(className);
                        if (CustomArranger.class.isAssignableFrom(aClass)) {
                            result = (Class<CustomArranger>) aClass;
                        } else {
                            System.err.println(className + " is not a subclass of CustomArranger, will not be used as custom arranger");
                        }
                    } catch (ClassNotFoundException e) {
                        System.err.println(className + " not found, will not be used as custom arranger");
                    }
                    return result;
                })
                .filter(Objects::nonNull);
    }
}
