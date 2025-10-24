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
package com.ocadotechnology.gembus.test.rearranger;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Test utility that produces a shallow copy of an existing instance while allowing selected
 * field values to be overridden. This Java implementation plays a role analogous to the Kotlin
 * {@code com.ocadotechnology.gembus.test.rearangerkt.Rearranger} singleton, but uses a simple
 * {@code Map<String, Supplier<?>>} based override syntax instead of Kotlin property references.
 * <p>
 * Supports both Java Records and regular (POJO) classes from a single entry point.</li>
 * <p>
 * <p><strong>Usage example (POJO):</strong>
 * <pre>{@code
 * PojoClass original = some(PojoClass.class);
 * PojoClass modified = Rearranger.copy(original, Map.of(
 *     "name", () -> "Overridden Name",
 *     "number", () -> 42
 * ));
 * }</pre>
 * <strong>Usage example (Record):</strong>
 * <pre>{@code
 * MyRecord original = new MyRecord("Alice", 10);
 * MyRecord modified = Rearranger.copy(original, Map.of(
 *     "name", () -> "Bob"
 * ));
 * }</pre>
 * <p>
 * Notes / Limitations:
 * <ul>
 *   <li>This is a shallow copy: referenced mutable objects are not cloned.</li>
 *   <li>Each supplier is invoked exactly once (during copy construction).</li>
 *   <li>Overrides must reference existing field / record component names; otherwise an {@link IllegalArgumentException} is thrown.</li>
 *   <li>Reflection may set accessibility to true to reach non-public members (for non-record classes).</li>
 * </ul>
 * <p>
 *
 * @see com.ocadotechnology.gembus.test.rearrangerkt.Rearranger Kotlin counterpart using property references
 */
public class Rearranger {

    /**
     * Creates a shallow copy of the provided {@code object}, applying zero or more field overrides.
     *
     * @param object    the original instance to copy; must not be {@code null}
     * @param overrides map whose keys are field (or record component) names and whose values are suppliers
     *                  producing the replacement values. A missing key means the original value is retained.
     * @param <T>       the static type of the copied object
     * @return a new instance of {@code T} with supplied overrides applied (shallow copy)
     * @throws IllegalArgumentException if an override refers to a field/component that does not exist on the class
     * @throws RuntimeException         wrapping reflective failure encountered during copying
     */
    public static <T> T copy(T object, Map<String, Supplier<?>> overrides) {
        try {
            if (object.getClass().isRecord()) {
                return RecordRearranger.copyRecord(object, overrides);
            } else {
                return ObjectRearranger.copyObject(object, overrides);
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    static <T> void validateOverrides(Map<String, Supplier<?>> overrides, Set<String> fieldNames, String className) {
        overrides.keySet().forEach(fieldToOverride -> {
            if (!fieldNames.contains(fieldToOverride)) {
                throw new IllegalArgumentException("Failed to override field " + fieldToOverride + " in class " + className + ". Field not found.");
            }
        });
    }
}
