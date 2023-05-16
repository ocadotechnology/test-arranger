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

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Set;

public record SkuSaveUpdateRequest(
        String retailerSkuId,
        @NotEmpty Set<String> productCodes,
        @NotEmpty @Valid Set<DescriptionDto> descriptions,
        @NotEmpty @Valid List<DescriptionDto> descriptions2,
        @NotBlank String imageUrl) {
}


record MyRecod(
        String is,
        Set<String> strings,
        List<String> stringsWithOverride) {
}