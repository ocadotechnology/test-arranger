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
package com.ocadotechnology.gembus.bugfix;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static com.ocadotechnology.gembus.test.Arranger.some;
import static com.ocadotechnology.gembus.test.Arranger.someString;
import static org.assertj.core.api.Assertions.assertThat;

public class SkuSaveUpdateRequestTest {

    @Test
    @Disabled
    void nameOk() {
        var saveUpdateRequest = some(SkuSaveUpdateRequest.class);
        assertThat(saveUpdateRequest.descriptions()).isNotEmpty();
        assertThat(saveUpdateRequest.descriptions().iterator().next().value()).isNotEmpty();
        assertThat(saveUpdateRequest.productCodes()).isNotEmpty();
        assertThat(saveUpdateRequest.productCodes().iterator().next()).isNotEmpty();
    }

    @Test
    @Disabled
    void nameFail() {
        var saveUpdateRequest =
                some(
                        SkuSaveUpdateRequest.class,
                        Map.of("descriptions", () -> Set.of(new DescriptionDto("en-GB", someString())),
                                "productCodes", () -> someString())
                                );
        assertThat(saveUpdateRequest.descriptions2()).isNotEmpty();
        assertThat(saveUpdateRequest.descriptions2().iterator().next().value()).isNotEmpty();
        assertThat(saveUpdateRequest.productCodes()).isNotEmpty();
        assertThat(saveUpdateRequest.productCodes().iterator().next()).isNotEmpty();
    }
}
