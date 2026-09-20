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

import org.jeasy.random.ObjectCreationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ArrangerAbstractTypesTest {

    @AfterEach
    public void cleanupProperties() {
        System.getProperties().remove(PropertiesWrapperTest.scanClasspathKey);
    }

    @Test
    @DisplayName("SHOULD fill field of abstract type with an instance of a concrete subtype WHEN classpath scanning is enabled")
    void shouldInstantiateAbstractFieldWithConcreteSubtype() {
        //given
        System.setProperty(PropertiesWrapperTest.scanClasspathKey, "true");

        //when
        WithAbstractField actual = randomRespectingCurrentProperties().nextObject(WithAbstractField.class);

        //then
        assertThat(actual.vehicle).isInstanceOfAny(Car.class, Bike.class);
        assertThat(actual.vehicle.wheels).isNotZero();
    }

    @Test
    @DisplayName("SHOULD fill field of interface type with an instance of an implementing class WHEN classpath scanning is enabled")
    void shouldInstantiateInterfaceFieldWithImplementation() {
        //given
        System.setProperty(PropertiesWrapperTest.scanClasspathKey, "true");

        //when
        WithInterfaceField actual = randomRespectingCurrentProperties().nextObject(WithInterfaceField.class);

        //then
        assertThat(actual.engine).isInstanceOf(DieselEngine.class);
        assertThat(((DieselEngine) actual.engine).horsePower).isNotZero();
    }

    @Test
    @DisplayName("SHOULD not fill field of abstract type WHEN classpath scanning is disabled, i.e. the default behaviour is preserved")
    void shouldNotInstantiateAbstractFieldByDefault() {
        //expect
        assertThatThrownBy(() -> Arranger.some(WithAbstractField.class))
                .isInstanceOf(ObjectCreationException.class)
                .hasRootCauseInstanceOf(InstantiationError.class)
                .hasMessageContaining(WithAbstractField.class.getName());
    }

    @Test
    @DisplayName("SHOULD arrange an instance of a concrete subtype WHEN an abstract type is requested directly")
    void shouldInstantiateRequestedAbstractType() {
        //given
        System.setProperty(PropertiesWrapperTest.scanClasspathKey, "true");

        //when
        Vehicle actual = randomRespectingCurrentProperties().nextObject(Vehicle.class);

        //then
        assertThat(actual).isInstanceOfAny(Car.class, Bike.class);
    }

    @Test
    @DisplayName("SHOULD WHEN the abstract type has no concrete subtypes")
    void shouldThrowOnMissingSubtype() {
        //expect
        assertThatThrownBy(() -> Arranger.some(UnknownVehicle.class))
                .isInstanceOf(ObjectCreationException.class)
                .hasRootCauseInstanceOf(InstantiationError.class)
                .hasMessageContaining(UnknownVehicle.class.getName());
    }

    /**
     * The properties are read when an EnhancedRandom is built, but the one used by Arranger.some is built when the
     * Arranger class is loaded, i.e. too early for a test to influence it. A dedicated instance is needed and it is
     * created without custom arrangers on purpose - ArrangersConfigurer.defaultRandom() would rebind the shared
     * CustomArranger instances to a random configured for this test.
     */
    private EnhancedRandom randomRespectingCurrentProperties() {
        return new EnhancedRandom.Builder(ArrangersConfigurer::getEasyRandomDefaultParameters)
                .build(new HashMap<>(), SeedHelper.calculateSeed());
    }

    /* The types used for arranging abstract fields have to be public, easy-random takes into account only public
     * concrete subtypes when scanning the classpath. */

    public static abstract class UnknownVehicle {
        int wheels;
    }

    public static abstract class Vehicle {
        int wheels;
    }

    public static class Car extends Vehicle {
        String brand;
    }

    public static class Bike extends Vehicle {
        boolean electric;
    }

    public interface Engine {
    }

    public static class DieselEngine implements Engine {
        int horsePower;
    }

    public static class WithAbstractField {
        Vehicle vehicle;
    }

    public static class WithInterfaceField {
        Engine engine;
    }
}
