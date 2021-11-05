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

import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.math.BigInteger;

import static com.ocadotechnology.gembus.test.Arranger.some;
import static org.assertj.core.api.Assertions.assertThat;

public class ArrangerBeanValidationSupportTest {

    @Test
    void should_supportPositiveAnnotation() {
        //given
        for (int i = 0; i < 25; i++) {

            //when
            PositiveValidatedBean actual = some(PositiveValidatedBean.class);

            //then
            assertThat(actual.bigDecimal.doubleValue()).isGreaterThan(0);
            assertThat(actual.bigInteger.doubleValue()).isGreaterThan(0);
            assertThat(actual.intNumber).isGreaterThan(0);
            assertThat(actual.longNumber).isGreaterThan(0);
            assertThat(actual.floatNumber).isGreaterThan(0);
            assertThat(actual.doubleNumber).isGreaterThan(0);
            assertThat(actual.shortNumber).isGreaterThan((short) 0);
            assertThat(actual.byteNumber).isGreaterThan((byte) 0);
        }
    }

}

class PositiveValidatedBean {
    @Positive BigDecimal bigDecimal;
    @Positive BigInteger bigInteger;
    @Positive int intNumber;
    @Positive Long longNumber;
    @Positive float floatNumber;
    @Positive Double doubleNumber;
    @Positive short shortNumber;
    @Positive byte byteNumber;
}