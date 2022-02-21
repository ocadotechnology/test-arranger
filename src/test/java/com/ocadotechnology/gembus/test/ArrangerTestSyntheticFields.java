package com.ocadotechnology.gembus.test;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ArrangerTestSyntheticFields {
    @Test
    void shouldNotInitializeSyntheticFields() {
        //when
        Outer.Inner.Test actual = Arranger.some(Outer.Inner.Test.class);

        //then
        assertThat(actual.test).isNotNull();
        assertThat(actual.getViaSyntheticField()).isNull();
    }
}

class Outer {
    class Inner {
        class Test {
            String test;

            Outer.Inner getViaSyntheticField() {
                return Outer.Inner.this;
            }
        }
    }
}