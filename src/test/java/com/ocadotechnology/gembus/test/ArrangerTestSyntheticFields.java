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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ArrangerTestSyntheticFields {
    @Test
    void shouldNotInitializeSyntheticFields() {
        //when
        Outer.Inner.Test actual = Arranger.some(Outer.Inner.Test.class);

        //then
        assertThat(actual.test).isNotNull();
        assertThat(actual.getViaSyntheticField()).isNull();
    }
}

class Outer {
    class Inner {
        class Test {
            String test;

            Outer.Inner getViaSyntheticField() {
                return Outer.Inner.this;
            }
        }
    }
}