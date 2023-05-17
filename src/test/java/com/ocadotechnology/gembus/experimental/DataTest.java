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
package com.ocadotechnology.gembus.experimental;

import com.ocadotechnology.gembus.bugfix.DescriptionDto;
import com.ocadotechnology.gembus.test.experimental.Data;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.ocadotechnology.gembus.test.Arranger.some;
import static org.assertj.core.api.Assertions.assertThat;

class DataTest {

    @Test
    void shouldCopyRecordButOverrideFieldsWithGivenSupplier() {
        //given
        DescriptionDto original = some(DescriptionDto.class);
        String overrider = some(String.class);

        //when
        DescriptionDto copy = Data.copy(original, Map.of("value", () -> overrider));

        //then
        assertThat(overrider).as("The test is meaningless when the condition is not met").isNotEqualTo(original.value());
        assertThat(copy.locale()).as("Locale should be copied").isEqualTo(original.locale());
        assertThat(copy.value()).as("Value should be overriden").isEqualTo(overrider);
    }
}