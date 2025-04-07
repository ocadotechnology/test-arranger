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

import io.github.classgraph.ClassGraph;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class ReflectionHelper {

    Map<Class<?>, CustomArranger<?>> customArrangers;
    private final List<Constructor<?>> arrangerConstructors;

    ReflectionHelper() {
        Stream<Class<CustomArranger>> customArrangerClasses;
        String vmName = System.getProperty("java.vm.name");
        if ("Dalvik".equals(vmName)) {
            customArrangerClasses = AndroidReflectionHelper.findCustomArrangerClasses();
        } else {
            customArrangerClasses = getCustomArrangerClasses();
        }
        arrangerConstructors = customArrangerClasses
                .filter(clazz -> isNotAbstract(clazz))
                .map(clazz -> extractConstructor(clazz))
                .filter(constructor -> constructor.isPresent())
                .map(constructor -> constructor.get())
                .collect(Collectors.toList());
    }

    Map<Class<?>, CustomArranger<?>> createAllCustomArrangers() {
        return customArrangers = arrangerConstructors.stream()
                .map(constructor -> createCustomArranger(constructor))
                .filter(customArranger -> customArranger != null)
                .collect(Collectors.toMap(
                        customArranger -> customArranger.type,
                        customArranger -> customArranger,
                        (arrangerA, arrangerB) -> {
                            throw new IllegalArgumentException("There are two arrangers registered for " + arrangerA.type.getName()
                                                                       + ", those are " + arrangerA.getClass().getName() + " and " + arrangerB.getClass().getName());
                        }));
    }

    private static Stream<Class<CustomArranger>> getCustomArrangerClasses() {
        return new ClassGraph()
                .acceptPackages(PropertiesWrapper.getRootPackage())
                .enableAllInfo()
                .scan()
                .getSubclasses(CustomArranger.class.getName())
                .loadClasses(CustomArranger.class, true)
                .stream();
    }

    private CustomArranger<?> createCustomArranger(Constructor<?> constructor) {
        try {
            constructor.setAccessible(true);
            return (CustomArranger) constructor.newInstance();
        } catch (Exception e) {
            System.err.println("Cannot create arranger for " + constructor.getName());
            return null;
        }
    }

    private boolean isNotAbstract(Class<CustomArranger> clazz) {
        return !Modifier.isAbstract(clazz.getModifiers());
    }

    private Optional<Constructor<?>> extractConstructor(Class<?> clazz) {
        final Optional<Constructor<?>> result = Arrays.stream(clazz.getDeclaredConstructors())
                .filter(constructor -> constructor.getParameterCount() == 0)
                .findAny();
        if (!result.isPresent()) {
            System.err.println(clazz.getCanonicalName() + " has no suitable constructor.");
        }
        return result;
    }
}