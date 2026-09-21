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

import org.jeasy.random.EasyRandomParameters;
import org.jeasy.random.api.Randomizer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class EnumSetRandomizerRegistryTest {

    private final EnumSetRandomizerRegistry registry = registryWithCollectionSizeRange(1, 4);

    @Test
    @DisplayName("SHOULD deliver a randomizer WHEN the field is an EnumSet with the enum type given")
    void randomizerForEnumSetField() throws NoSuchFieldException {
        //when
        Randomizer<?> actual = registry.getRandomizer(fieldNamed("enumSet"));

        //then
        assertThat(actual).isNotNull();
        assertThat(colorsOf(actual)).isNotEmpty().isSubsetOf(Color.values());
    }

    @Test
    @DisplayName("SHOULD deliver no randomizer WHEN the EnumSet field is declared with a raw type")
    void noRandomizerForRawEnumSetField() throws NoSuchFieldException {
        //expect
        assertThat(registry.getRandomizer(fieldNamed("rawEnumSet"))).isNull();
    }

    @Test
    @DisplayName("SHOULD deliver no randomizer WHEN the field is a collection other than EnumSet")
    void noRandomizerForOtherCollectionField() throws NoSuchFieldException {
        //expect
        assertThat(registry.getRandomizer(fieldNamed("colors"))).isNull();
        assertThat(registry.getRandomizer(fieldNamed("text"))).isNull();
    }

    @Test
    @DisplayName("SHOULD deliver no randomizer WHEN only the type is known, as the enum cannot be derived from it")
    void noRandomizerByType() {
        //expect
        assertThat(registry.getRandomizer(EnumSet.class)).isNull();
    }

    @Test
    @DisplayName("SHOULD respect the configured collection size range")
    void respectCollectionSizeRange() throws NoSuchFieldException {
        //given
        EnumSetRandomizerRegistry oneElementOnly = registryWithCollectionSizeRange(1, 1);

        //when
        Randomizer<?> actual = oneElementOnly.getRandomizer(fieldNamed("enumSet"));

        //then
        assertThat(colorsOf(actual)).hasSize(1);
    }

    @SuppressWarnings("unchecked")
    private EnumSet<Color> colorsOf(Randomizer<?> randomizer) {
        return (EnumSet<Color>) randomizer.getRandomValue();
    }

    private EnumSetRandomizerRegistry registryWithCollectionSizeRange(int min, int max) {
        EnumSetRandomizerRegistry result = new EnumSetRandomizerRegistry(Arranger.someLong());
        result.init(new EasyRandomParameters().collectionSizeRange(min, max));
        return result;
    }

    private Field fieldNamed(String name) throws NoSuchFieldException {
        return WithEnumSets.class.getDeclaredField(name);
    }

    enum Color {
        RED,
        GREEN,
        BLUE
    }

    @SuppressWarnings({"rawtypes", "unused"})
    static class WithEnumSets {
        EnumSet<Color> enumSet;
        EnumSet rawEnumSet;
        Set<Color> colors;
        List<String> text;
    }
}
