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
package com.ocadotechnology.gembus.test.rearrangerkt

import com.ocadotechnology.gembus.test.rearangerkt.Rearranger
import com.ocadotechnology.gembus.test.some
import com.ocadotechnology.gembus.test.someInt
import com.ocadotechnology.gembus.test.someString
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.lang.reflect.InvocationTargetException

class RearrangerKotlinTest {

    @Test
    fun `SHOULD copy instance with overridden properties WHEN working with data class`() {
        //given
        val original = some<DataClass>()
        val unchanged = original.other
        assertThat(unchanged).isNotNull()

        //when
        val copy = Rearranger.copy(original) {
            DataClass::name set "overridden"
            DataClass::number set 2
        }

        //then
        assertThat(copy.name).isEqualTo("overridden")
        assertThat(copy.number).isEqualTo(2)
        assertThat(copy.other).isEqualTo(unchanged)
    }

    @Test
    fun `SHOULD copy instance with overridden properties WHEN working with plain class`() {
        //given
        val original = some<PojoClass>()
        val unchanged = original.other
        assertThat(unchanged).isNotNull()

        //when
        val copy = Rearranger.copy(original) {
            PojoClass::name set "overridden"
            PojoClass::number set 2
        }

        //then
        assertThat(copy.name).isEqualTo("overridden")
        assertThat(copy.number).isEqualTo(2)
        assertThat(copy.other).isEqualTo(unchanged)
    }

    @Test
    fun `SHOULD copy instance with overridden properties WHEN working with constructorless immutable class`() {
        //given
        val original = some<NoConstructorImmutable>()
        val unchanged = original.other
        assertThat(unchanged).isNotNull()

        //when
        val copy = Rearranger.copy(original) {
            NoConstructorImmutable::name set "overridden"
            NoConstructorImmutable::number set 2
        }

        //then
        assertThat(copy.name).isEqualTo("overridden")
        assertThat(copy.number).isEqualTo(2)
        assertThat(copy.other).isEqualTo(unchanged)
    }

    @Test
    fun `SHOULD copy instance with overridden properties WHEN working with constructorless mutable class`() {
        //given
        val original = some<NoConstructorMutable>()
        val unchanged = original.other
        assertThat(unchanged).isNotNull()

        //when
        val copy = Rearranger.copy(original) {
            NoConstructorMutable::name set "overridden"
            NoConstructorMutable::number set 2
        }

        //then
        assertThat(copy.name).isEqualTo("overridden")
        assertThat(copy.number).isEqualTo(2)
        assertThat(copy.other).isEqualTo(unchanged)
    }


    @Test
    fun `SHOULD copy instance with overridden properties WHEN working with properties with backing fields`() {
        //given
        val original = some<ClassWithBackingField>()
        val unchanged = original.regularProperty
        assertThat(unchanged).isNotNull()

        //when
        val copy = Rearranger.copy(original) {
            ClassWithBackingField::customProperty set "overridden"
        }

        //then
        assertThat(copy.customProperty).isEqualTo("overridden")
        assertThat(copy.regularProperty).isEqualTo(unchanged)
    }

    @Test
    fun `SHOULD copy instance with overridden properties WHEN working with collections`() {
        //given
        val original = some<ClassWithCollections>()
        val unchangedMap = original.mapProperty
        assertThat(unchangedMap).isNotEmpty()

        //when
        val newList = listOf(someString(), someString())
        val copy = Rearranger.copy(original) {
            ClassWithCollections::listProperty set newList
        }

        //then
        assertThat(copy.listProperty).isEqualTo(newList)
        assertThat(copy.mapProperty).isEqualTo(unchangedMap)
    }

    @Test
    fun `SHOULD copy instance with overridden properties WHEN working with abstract types`() {
        //given
        val original = some<ConcreteClass>()
        val unchanged = original.concreteProperty
        assertThat(unchanged).isNotNull()

        //when
        val copy = Rearranger.copy(original) {
            ConcreteClass::abstractProperty set "overridden"
        }

        //then
        assertThat(copy.abstractProperty).isEqualTo("overridden")
        assertThat(copy.concreteProperty).isEqualTo(unchanged)
    }

    @Test
    fun `SHOULD copy instance with overridden properties WHEN working with not-null fields`() {
        //given
        val original = some<ClassWithNotNullField>()
        val unchanged = original.nullableField
        assertThat(unchanged).isNotNull()
        val otherUnchanged = original.otherField
        assertThat(otherUnchanged).isNotNull()

        //when
        val copy = Rearranger.copy(original) {
            ClassWithNotNullField::notNullField set "overridden"
        }

        //then
        assertThat(copy.notNullField).isEqualTo("overridden")
        assertThat(copy.nullableField).isEqualTo(unchanged)
        assertThat(copy.otherField).isEqualTo(otherUnchanged)
    }

    @Test
    fun `SHOULD copy instance with overridden properties WHEN working with not-null fields and all args constructor`() {
        //given
        val original = some<ClassWithNotNullFieldAndAllArgsConstructor>()
        val unchanged = original.nullableField
        assertThat(unchanged).isNotNull()

        //when
        val copy = Rearranger.copy(original) {
            ClassWithNotNullFieldAndAllArgsConstructor::notNullField set "overridden"
        }

        //then
        assertThat(copy.notNullField).isEqualTo("overridden")
        assertThat(copy.nullableField).isEqualTo(unchanged)
    }

    @Test
    fun `SHOULD copy instance with overridden properties WHEN working with fields that cannot be instantiated out of the box`() {
        //given
        val original = ClassWithAbstractField(some<ConcreteClass>(), someString())
        val unchanged = original.simpleField
        assertThat(unchanged).isNotNull()

        //when
        val someObject = some<ConcreteClass>()
        val anotherObject = some<ConcreteClass>()
        val copy = Rearranger.copy(original) {
            ClassWithAbstractField::abstractField set someObject
            ClassWithAbstractField::anotherAbstractField set anotherObject
        }

        //then
        assertThat(copy.abstractField).isEqualTo(someObject)
        assertThat(copy.anotherAbstractField).isEqualTo(anotherObject)
        assertThat(copy.simpleField).isEqualTo(unchanged)
    }

    @Test
    fun `SHOULD override using null WHEN field in data class is nullable`() {
        //given
        val original = some<DataClass>()
        val unchanged = original.other
        assertThat(unchanged).isNotNull()

        //when
        val copy = Rearranger.copy(original) {
            DataClass::other set null
        }

        //then
        assertThat(copy.other).isNull()
        assertThat(original.other).isEqualTo(unchanged)
    }

    @Test
    fun `SHOULD override using null WHEN field in plain class is nullable`() {
        //given
        val original = some<PojoClass>()
        val unchanged = original.other
        assertThat(unchanged).isNotNull()

        //when
        val copy = Rearranger.copy(original) {
            PojoClass::other set null
        }

        //then
        assertThat(copy.other).isNull()
        assertThat(original.other).isEqualTo(unchanged)
    }

    @Test
    fun `SHOULD override using null WHEN field in constructorless class is nullable`() {
        //given
        val original = some<NoConstructorImmutable>()
        val unchanged = original.name
        assertThat(unchanged).isNotNull()

        //when
        val copy = Rearranger.copy(original) {
            NoConstructorImmutable::name set null
        }

        //then
        assertThat(copy.name).isNull()
        assertThat(original.name).isEqualTo(unchanged)
    }

    @Test
    fun `SHOULD throw exception WHEN trying to set null to a not null field`() {
        //given
        val original = some<DataClass>()

        //when
        assertThatThrownBy {
            Rearranger.copy(original) {
                DataClass::name set null
            }
        }
            //then
            .isInstanceOf(InvocationTargetException::class.java)
            .hasRootCauseInstanceOf(NullPointerException::class.java)
    }

    @Test
    fun `SHOULD override inherited fields WHEN overriden in child class`() {
        //given
        val original = some<PojoClassChild>()
        val overriderString = "overridden"
        val overriderInt = someInt()

        //when
        val copy = Rearranger.copy(original) {
            PojoClassChild::name set overriderString
            PojoClassChild::number set overriderInt
        }

        //then
        assertThat(copy.name).isEqualTo(overriderString)
        assertThat(copy.number).isEqualTo(overriderInt)
    }
}

// Classes needed for the tests
data class DataClass(val name: String, val number: Int, val other: String?)

open class PojoClass(open val name: String, val number: Int, val other: String?)

class PojoClassChild(override val name: String, number: Int, other: String?) : PojoClass(name, number, other)

class NoConstructorImmutable(val name: String? = null) {
    val number: Int? = null
    lateinit var other: String
}

class NoConstructorMutable {
    var name: String? = null
    var number: Int? = null
    lateinit var other: String
}

class ClassWithBackingField {
    var regularProperty: String? = null

    var customProperty: String = ""
        private set
}

class ClassWithCollections {
    var listProperty: List<String>? = null
    var mapProperty: Map<String, String>? = null
}

abstract class AbstractClass {
    abstract val abstractProperty: String
}

class ConcreteClass : AbstractClass() {
    override var abstractProperty: String = ""
    var concreteProperty: String? = null
}

class ClassWithNotNullFieldAndAllArgsConstructor {
    var notNullField: String?
    var nullableField: String? = null

    constructor(notNullField: String?, nullableField: String?) {
        require(notNullField != null) { "notNullField must not be null" }
        this.notNullField = notNullField
        this.nullableField = nullableField
    }
}

class ClassWithNotNullField {
    var notNullField: String?
    var nullableField: String? = null
    var otherField: String? = null

    constructor(notNullField: String?, nullableField: String?) {
        require(notNullField != null) { "notNullField must not be null" }
        this.notNullField = notNullField
        this.nullableField = nullableField
    }
}

class ClassWithAbstractField(
    val anotherAbstractField: AbstractClass? = null,
    var simpleField: String? = null
) {
    lateinit var abstractField: AbstractClass
}