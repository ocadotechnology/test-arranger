package com.ocado.gembus.test

import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.function.Predicate

inline fun <reified T> some(vararg excludedFields: String) =
        Arranger.some(T::class.java, *excludedFields)!!

inline fun <reified T> someObjects(numberOfObjects: Int, vararg excludedFields: String) =
        Arranger.someObjects(T::class.java, numberOfObjects, *excludedFields).iterator().asSequence<T>()

inline fun <reified T> someMatching(crossinline predicate: (T) -> Boolean, vararg excludedFields: String) =
        Arranger.someMatching<T>(T::class.java, Predicate { it -> predicate.invoke(it) }, *excludedFields)!!

fun someInt(): Int = Arranger.someInteger()
fun someLong(): Long = Arranger.someLong()
fun someText(): String = Arranger.someText()
fun someText(minLength: Int, maxLength: Int): String = Arranger.someText(minLength, maxLength)
fun somePriceLikeBigDecimal(): BigDecimal = Arranger.somePriceLikeBigDecimal()
fun someBoolean(): Boolean = Arranger.someBoolean()
fun someTwoDecimalPlacesBigDecimal(): BigDecimal = Arranger.somePriceLikeBigDecimal()
fun someTwoDecimalPlacesBigDecimal(min: BigDecimal, max: BigDecimal): BigDecimal = Arranger.somePriceLikeBigDecimal(min, max)
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
