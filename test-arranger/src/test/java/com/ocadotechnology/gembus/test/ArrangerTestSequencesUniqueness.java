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

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class ArrangerTestSequencesUniqueness {
    @Test
    void sequencesOfLongsShouldNotBeReusedBetweenRandomizers() {
        //given
        Set<Long> longs = new HashSet<>();
        int N = 10;

        //when
        for (int i = 0; i < N; i++) {
            longs.add(Arranger.some(Long.class));
            longs.add(Arranger.someLong());
            longs.add(Arranger.some(ClassWithPrimitiveLong.class).number);
            longs.add(Arranger.some(ClassWithObjectLong.class).number);
        }

        //then
        assertThat(longs).hasSize(4 * N);
    }

    @Test
    void sequencesOfFloatsShouldNotBeReusedBetweenRandomizers() {
        //given
        Set<Float> floats = new HashSet<>();
        int N = 10;

        //when
        for (int i = 0; i < N; i++) {
            floats.add(Arranger.some(Float.class));
            floats.add(Arranger.some(float.class));
            floats.add(Arranger.some(ClassWithPrimitiveFloat.class).number);
            floats.add(Arranger.some(ClassWithObjectFloat.class).number);
        }

        //then
        assertThat(floats).hasSize(4 * N);
    }

    @Test
    void sequencesOfLongsShouldNotBeReusedBetweenSimplifiedRandomizers() {
        //given
        Set<Long> longs = new HashSet<>();
        int N = 10;

        //when
        for (int i = 0; i < N; i++) {
            longs.add(Arranger.someSimplified(Long.class));
            longs.add(Arranger.someSimplified(long.class));
            longs.add(Arranger.someSimplified(ClassWithPrimitiveLong.class).number);
            longs.add(Arranger.someSimplified(ClassWithObjectLong.class).number);
        }

        //then
        assertThat(longs).hasSize(4 * N);
    }

    @Test
    void sequencesOfFloatsShouldNotBeReusedBetweenSimplifiedRandomizers() {
        //given
        Set<Float> floats = new HashSet<>();
        int N = 10;

        //when
        for (int i = 0; i < N; i++) {
            floats.add(Arranger.someSimplified(Float.class));
            floats.add(Arranger.someSimplified(float.class));
            floats.add(Arranger.someSimplified(ClassWithPrimitiveFloat.class).number);
            floats.add(Arranger.someSimplified(ClassWithObjectFloat.class).number);
        }

        //then
        assertThat(floats).hasSize(4 * N);
    }
}

class ClassWithObjectLong {
    Long number;
}

class ClassWithPrimitiveLong {
    long number;
}

class ClassWithObjectFloat {
    Float number;
}

class ClassWithPrimitiveFloat {
    float number;
}