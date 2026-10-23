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
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ArrangersConfigurerTest {

    final static String hardcoded_string = "hardcoded string";

    @Test
    public void customArrangerShouldBeRegistered_and_usedForNestedClass() {
        //given
        final EnhancedRandom random = ArrangersConfigurer.instance().defaultRandom();

        //when
        final Wrapper wrapper = random.nextObject(Wrapper.class);

        //then
        assertEquals(hardcoded_string, wrapper.myEntity.text);
    }

    @Test
    void should_useTheHardcodedSeed_when_randomSeedIsNotEnabled() {
        //given
        System.setProperty("arranger.randomseed", "false");

        //when
        EasyRandomParameters actual = ArrangersConfigurer.getEasyRandomDefaultParameters();

        //then
        assertThat(actual.getSeed()).isEqualTo(123);
    }

    @Test
    void should_useRandomSeed_when_randomSeedIsEnabled() {
        //given
        System.setProperty("arranger.randomseed", "true");

        //when
        EasyRandomParameters actual1 = ArrangersConfigurer.getEasyRandomDefaultParameters();
        EasyRandomParameters actual2 = ArrangersConfigurer.getEasyRandomDefaultParameters();

        //then
        assertThat(actual1.getSeed()).isNotEqualTo(123);
        assertThat(actual2.getSeed()).isNotEqualTo(123);
        assertThat(actual1.getSeed()).isNotEqualTo(actual2.getSeed());
    }

    @Test
    void should_useTheHardcodedSeedForSimplified_when_randomSeedIsNotEnabled() {
        //given
        System.setProperty("arranger.randomseed", "false");

        //when
        EasyRandomParameters actual = ArrangersConfigurer.getEasyRandomSimplifiedParameters();

        //then
        assertThat(actual.getSeed()).isEqualTo(123);
    }

    @Test
    void should_useRandomSeedForSimplified_when_randomSeedIsEnabled() {
        //given
        System.setProperty("arranger.randomseed", "true");

        //when
        EasyRandomParameters actual1 = ArrangersConfigurer.getEasyRandomSimplifiedParameters();
        EasyRandomParameters actual2 = ArrangersConfigurer.getEasyRandomSimplifiedParameters();

        //then
        assertThat(actual1.getSeed()).isNotEqualTo(123);
        assertThat(actual2.getSeed()).isNotEqualTo(123);
        assertThat(actual1.getSeed()).isNotEqualTo(actual2.getSeed());
    }
}

class Wrapper {
    final MyEntity myEntity;

    Wrapper(MyEntity myEntity) {
        this.myEntity = myEntity;
    }
}

class MyEntity {
    final String text;

    MyEntity(String text) {
        this.text = text;
    }
}

class MyEntityArranger extends CustomArranger<MyEntity> {

    @Override
    protected MyEntity instance() {
        return new MyEntity(ArrangersConfigurerTest.hardcoded_string);
    }
}