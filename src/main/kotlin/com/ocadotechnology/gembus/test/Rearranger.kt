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
package com.ocadotechnology.gembus.test

import org.objenesis.Objenesis
import org.objenesis.ObjenesisStd
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField

/**
 * Utility for creating modified copies of test data objects regardless of their mutability.
 * Allows creation of new instances with selected properties overridden without having to
 * manually implement copy constructors or builder patterns.
 */
object Rearranger {

    /**
     * Creates a copy of the given instance, allowing to override some of its properties.
     * Example usage:
     * ```
     * val original = SomeClass(id = 123, name = "Original")
     * val modified = Rearranger.copy(original) {
     *  SomeClass::name set "Modified"
     * }
     * ```
     */
    inline fun <reified T : Any> copy(
        instance: T,
        block: CopyScope<T>.() -> Unit
    ): T {
        val scope = CopyScope(instance)
        scope.block()
        return scope.build()
    }

    class CopyScope<T : Any>(private val original: T) {
        private val overrides = mutableMapOf<KProperty1<T, *>, Any?>()
        private val objenesis: Objenesis = ObjenesisStd()

        /**
         * Override a property value in the copy: `SomeClass::foo set newValue`
         */
        infix fun <R> KProperty1<T, R>.set(value: R) {
            overrides[this] = value
        }

        fun build(): T {
            val props = original::class.memberProperties
                .filterIsInstance<KProperty1<T, Any?>>()

            val matchingCtor = findMatchingConstructor(original::class, props)

            if (matchingCtor != null) {
                return createCopyWithOverridesUsingContructor(matchingCtor, props)
            } else {
                val toFillWithValues = objenesis.newInstance(original::class.java)
                setValuesInCopiedInstance(props, toFillWithValues)
                return toFillWithValues
            }
        }

        private fun findMatchingConstructor(
            kClass: KClass<out T>,
            props: List<KProperty1<T, Any?>>
        ) = kClass.constructors.firstOrNull { ctor ->
            val paramNames = ctor.parameters.mapNotNull { it.name }.toSet()
            val propNames = props.map { it.name }.toSet()
            propNames.all { it in paramNames }
        }

        private fun createCopyWithOverridesUsingContructor(
            matchingCtor: KFunction<T>,
            props: List<KProperty1<T, Any?>>
        ): T {
            val args = matchingCtor.parameters.associateWith { param ->
                val prop = props.first { it.name == param.name }
                overrides[prop] ?: prop.get(original)
            }
            @Suppress("UNCHECKED_CAST")
            return matchingCtor.callBy(args) as T
        }

        private fun setValuesInCopiedInstance(props: List<KProperty1<T, Any?>>, toFillWithValues: T) {
            props.forEach { prop ->
                val value = overrides[prop] ?: prop.get(original)
                setFieldValue(prop, toFillWithValues, value)
            }
        }

        private fun setFieldValue(fieldType: KProperty1<T, Any?>, field: T, value: Any?) {
            when (fieldType) {
                is KMutableProperty1<T, Any?> -> {
                    fieldType.isAccessible = true
                    fieldType.set(field, value)
                }

                else -> {
                    fieldType.javaField?.apply {
                        isAccessible = true
                        set(field, value)
                    }
                }
            }
        }
    }
}