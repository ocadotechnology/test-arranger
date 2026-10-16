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
import java.lang.reflect.Type;
import java.util.Optional;

class EnumTypeExtractor {

    /**
     * @param declaredType the generic type of a field or record component, e.g. {@code EnumSet<Color>}
     * @return the enum used as the type parameter, empty when the declaration is raw or the parameter is not an enum
     */
    @SuppressWarnings("rawtypes")
    static Optional<Class<? extends Enum>> extractEnumType(Type declaredType) {
        if (!(declaredType instanceof ParameterizedType)) {
            return Optional.empty();
        }
        Type[] typeArguments = ((ParameterizedType) declaredType).getActualTypeArguments();
        if (typeArguments.length != 1 || !(typeArguments[0] instanceof Class)) {
            return Optional.empty();
        }
        Class<?> typeArgument = (Class<?>) typeArguments[0];
        if (!typeArgument.isEnum()) {
            return Optional.empty();
        }
        return Optional.of(typeArgument.asSubclass(Enum.class));
    }
}
