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