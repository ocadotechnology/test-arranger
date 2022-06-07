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

import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.function.Predicate
import java.util.function.Supplier

inline fun <reified T> some(adjustment: T.() -> Unit): T {
    val result = some<T>()
    result.adjustment()
    return result
}

inline fun <reified T> some(vararg excludedFields: String) =
    Arranger.some(T::class.java, *excludedFields)!!

inline fun <reified T> some(overrides: Map<String, () -> Any>): T =
    Arranger.some(T::class.java, overrides.mapValues { Supplier { it.value.invoke() } })

@Deprecated(
    message = "This methods creates a lot of complexity resulting in lesser performance and increased memory consumption.",
    replaceWith = ReplaceWith("some<T>(excludedFields)"),
    level = DeprecationLevel.WARNING
)
inline fun <reified T> someSimplified(vararg excludedFields: String): T =
    Arranger.someSimplified(T::class.java, *excludedFields)!!

inline fun <reified T> someObjects(numberOfObjects: Int, vararg excludedFields: String) =
    Arranger.someObjects(T::class.java, numberOfObjects, *excludedFields).iterator().asSequence<T>()

inline fun <reified T> someObjects(numberOfObjects: Int, overrides: Map<String, () -> Any>) =
    Arranger.someObjects(T::class.java, numberOfObjects, overrides.mapValues { Supplier { it.value.invoke() } })
        .iterator().asSequence<T>()

inline fun <reified T> someMatching(crossinline predicate: (T) -> Boolean, vararg excludedFields: String) =
    Arranger.someMatching<T>(T::class.java, Predicate { predicate.invoke(it) }, *excludedFields)!!

fun someInt(): Int = Arranger.someInteger()
fun someInt(minValue: Int, maxValue: Int): Int = Arranger.someInteger(minValue, maxValue)
fun someLong(): Long = Arranger.someLong()
fun someText(): String = Arranger.someText()
fun someText(maxLength: Int): String = Arranger.someText(maxLength)
fun someText(minLength: Int, maxLength: Int): String = Arranger.someText(minLength, maxLength)
fun someString(): String = Arranger.someString()
fun someString(maxLength: Int): String = Arranger.someString(maxLength)
fun someString(minLength: Int, maxLength: Int): String = Arranger.someString(minLength, maxLength)
fun somePriceLikeBigDecimal(): BigDecimal = Arranger.somePriceLikeBigDecimal()
fun somePriceLikeBigDecimal(min: BigDecimal, max: BigDecimal): BigDecimal = Arranger.somePriceLikeBigDecimal(min, max)
fun someBoolean(): Boolean = Arranger.someBoolean()
fun someTwoDecimalPlacesBigDecimal(): BigDecimal = Arranger.somePriceLikeBigDecimal()
fun somePositiveInt(boundInclusive: Int): Int = Arranger.somePositiveInt(boundInclusive)
fun somePositiveLong(boundInclusive: Long): Long = Arranger.somePositiveLong(boundInclusive)

fun someEmail(): String = Arranger.someEmail()

fun <T> someMatchingOrNull(array: Array<T>, predicate: (T) -> Boolean): T? =
    Arranger.someMatching(array, predicate)

fun <T> someMatching(array: Array<T>, predicate: (T) -> Boolean) =
    someMatchingOrNull(array, predicate) ?: throw RuntimeException("No match found.")

fun someGivenOrLater(given: LocalDate): LocalDate = Arranger.someGivenOrLater(given)
fun someGivenOrEarlier(given: LocalDate): LocalDate = Arranger.someGivenOrEarlier(given)
fun someInstant(): Instant = Arranger.someInstant()
fun <T> someFrom(source: Collection<T>): T = Arranger.someFrom(source)
fun someFirstName(): String = Arranger.someFirstName()
fun someLastName(): String = Arranger.someLastName()
fun someStringLike(regex: String): String = Arranger.someStringLike(regex)