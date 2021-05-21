/**
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
package com.ocado.gembus.test

import com.ocadotechnology.gembus.test.some
import com.ocadotechnology.gembus.test.someInt
import com.ocadotechnology.gembus.test.someSimplified
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class KotlinArrangerTest {

    @Test
    fun testSomeSimplified() {
        //when
        val actual = someSimplified<A>()

        //then
        assertThat(actual.objects).isNotNull
        assertThat(actual.ts).isNotNull
    }

    @Test
    fun testSomeWithAdjustment() {
        //given
        val someNumber = someInt()
        val someString = some<String>()

        //when
        val notSoRandom = some<BB> {
            number = someNumber
            txt = someString
        }

        //then
        assertThat(notSoRandom.number).isEqualTo(someNumber)
        assertThat(notSoRandom.txt).isEqualTo(someString)
    }
}

class A(var ts: List<A>) {
    val objects: List<A>? = null
}

class BB(var number: Int, var txt: String)
