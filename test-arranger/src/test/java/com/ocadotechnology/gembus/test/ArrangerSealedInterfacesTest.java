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
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static com.ocadotechnology.gembus.test.Arranger.some;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ArrangerSealedInterfacesTest {

    @Test
    void should_arrangeSealedInterface() {
        //when
        SealedInterfaceData actual = some(SealedInterfaceData.class);

        //then
        assertThat(actual).isInstanceOfAny(ConcreteData1.class, ConcreteData2.class);
    }

    @Test
    void should_arrangeSealedInterfaceWithCustomArranger() {
        //when
        SealedInterfaceWithCustomArranger actual = some(SealedInterfaceWithCustomArranger.class);

        //then
        assertThat(actual).isEqualTo(new ConcreteDataWithCustomArranger("expected-test-string"));
    }

    @Test
    void should_arrangeDataWithSealedInterfaceField() {
        //when
        DataWithAbstract actual = some(DataWithAbstract.class, "excludedSealedInterface", "nonSealedInterface");

        //then
        assertThat(actual.sealedInterfaceData()).isInstanceOfAny(ConcreteData1.class, ConcreteData2.class);
        assertThat(actual.excludedSealedInterface()).isNull();
    }

    @Test
    void should_arrangeDataWithSealedInterfaceFieldOverride() {
        //when
        ConcreteData1 expected = new ConcreteData1("expected-data");
        DataWithAbstract actual = some(DataWithAbstract.class, Map.of(
                "sealedInterfaceData", () -> expected,
                "excludedSealedInterface", () -> null,
                "nonSealedInterface", () -> null));

        //then
        assertThat(actual.sealedInterfaceData()).isEqualTo(expected);
        assertThat(actual.excludedSealedInterface()).isNull();
    }

    @Test
    void should_arrangeDataWithNestedSealedInterfaceField() {
        //when
        NestedSealedInterfaceData actual = some(NestedSealedInterfaceData.class);

        //then
        assertThat(actual).isInstanceOf(ConcreteNested1.class);
        assertThat((ConcreteNested1) actual)
                .extracting(ConcreteNested1::sealedInterfaceData)
                .isInstanceOfAny(ConcreteData1.class, ConcreteData2.class);

    }

    @Test
    void should_arrangeRecordWithListWithSealedInterfaceField() {
        //when
        RootRecordWithNestedList actual = some(RootRecordWithNestedList.class);

        //then
        assertThat(actual.nestedRecordWithSealedInterfaces().get(0))
                .extracting(NestedRecordWithSealedInterface::sealedInterfaceData)
                .isInstanceOfAny(ConcreteData1.class, ConcreteData2.class);
    }

    @Test
    void should_failForNonSealedInterface() {
        //when
        assertThatThrownBy(() -> some(DataWithAbstract.class, "excludedSealedInterface"))

                //then
                .isInstanceOf(ObjectCreationException.class);

    }

}

record DataWithAbstract(Integer value,
                        SealedInterfaceData sealedInterfaceData,
                        ExcludedSealedInterface excludedSealedInterface,
                        NonSealedInterface nonSealedInterface) {
    DataWithAbstract {
    }

    DataWithAbstract(String name) {
        this(0, new ConcreteData1(""), new ExcludedData1(), new NonSealedInterface() {
        });
    }
}

sealed interface SealedInterfaceData permits ConcreteData1, ConcreteData2 {
}

record ConcreteData1(String value) implements SealedInterfaceData {
}

final class ConcreteData2 implements SealedInterfaceData {

    Integer value;

}

sealed interface ExcludedSealedInterface permits ExcludedData1 {
}

record ExcludedData1() implements ExcludedSealedInterface {
}

interface NonSealedInterface {
}

sealed interface NestedSealedInterfaceData permits ConcreteNested1 {
}

record ConcreteNested1(SealedInterfaceData sealedInterfaceData) implements NestedSealedInterfaceData {
}

sealed interface SealedInterfaceWithCustomArranger permits ConcreteDataWithCustomArranger {
}

record ConcreteDataWithCustomArranger(String test) implements SealedInterfaceWithCustomArranger {
}

class CustomSealedInterfaceArranger extends CustomArranger<SealedInterfaceWithCustomArranger> {

    @Override
    protected SealedInterfaceWithCustomArranger instance() {
        return new ConcreteDataWithCustomArranger("expected-test-string");
    }
}

record RootRecordWithNestedList(List<NestedRecordWithSealedInterface> nestedRecordWithSealedInterfaces) {
}

record NestedRecordWithSealedInterface(SealedInterfaceData sealedInterfaceData) {
}
