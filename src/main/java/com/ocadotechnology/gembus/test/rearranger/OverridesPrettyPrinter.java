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
import java.util.function.Supplier;
import java.util.stream.Collectors;

class OverridesPrettyPrinter {
    static String describeOverrides(Map<String, Supplier<?>> overrides) {
        if (overrides == null || overrides.isEmpty()) {
            return "{}";
        }
        return overrides.entrySet().stream()
                .map(e -> formatOverride(e.getKey(), e.getValue()))
                .collect(Collectors.joining(", ", "{", "}"));
    }

    private static String formatOverride(String name, Supplier<?> supplier) {
        Object value;
        try {
            value = supplier.get();
        } catch (Exception ex) {
            return name + "=???";
        }
        if (value == null) {
            return name + "=null";
        }
        String type = value.getClass().getSimpleName();
        String display = String.valueOf(value);
        return name + "=" + display + "<" + type + ">";
    }
}
