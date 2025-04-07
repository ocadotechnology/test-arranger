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

import java.util.List;

import static com.ocadotechnology.gembus.test.Arranger.someString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class AndroidReflectionHelperTest {

    @Test
    void shouldFindCustomArranger_whenValidClassesAreGivenInProperties() {
        //given
        Class customArranger1 = com.ocadotechnology.gembus.test.ChildArranger.class;
        Class customArranger2 = com.ocadotechnology.gembus.test.ParentArranger.class;
        String propertyValue = customArranger1.getName() + "," + customArranger2.getName();
        System.setProperty(PropertiesWrapperTest.androidCustomArrangers, propertyValue);

        //when
        List<Class<CustomArranger>> actual = AndroidReflectionHelper.findCustomArrangerClasses().toList();

        //then
        assertThat(actual).hasSize(2);
        assertThat(actual).contains(customArranger1);
        assertThat(actual).contains(customArranger2);
    }

    @Test
    void shouldNoReturnClasses_whenDoNoExtendCustomArranger() {
        //given
        String propertyValue = Child.class.getName();
        System.setProperty(PropertiesWrapperTest.androidCustomArrangers, propertyValue);

        //when
        List<Class<CustomArranger>> actual = AndroidReflectionHelper.findCustomArrangerClasses().toList();

        //then
        assertThat(actual).isEmpty();
    }

    @Test
    void shouldNotReturnClass_whenGivenNameIsNotValidClassName() {
        //given
        System.setProperty(PropertiesWrapperTest.androidCustomArrangers, someString());

        //when
        List<Class<CustomArranger>> actual = AndroidReflectionHelper.findCustomArrangerClasses().toList();

        //then
        assertThat(actual).isEmpty();
    }
}