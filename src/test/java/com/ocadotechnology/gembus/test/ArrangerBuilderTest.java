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

import io.github.benas.randombeans.api.EnhancedRandom;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class ArrangerBuilderTest {

    final static String hardcoded_string = "hardcoded string";

    @Test
    public void customArrangerShouldBeRegistered_and_usedForNestedClass() {
        //given
        final EnhancedRandom random = ArrangerBuilder.instance().buildArranger(Optional.empty());

        //when
        final Wrapper wrapper = random.nextObject(Wrapper.class);

        //then
        assertEquals(hardcoded_string, wrapper.myEntity.text);
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
        return new MyEntity(ArrangerBuilderTest.hardcoded_string);
    }
}