package com.ocadotechnology.gembus.test;

import org.junit.jupiter.api.Test;

import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.math.BigInteger;

import static com.ocadotechnology.gembus.test.Arranger.some;
import static org.assertj.core.api.Assertions.assertThat;

public class ArrangerBeanValidationSupportTest {

    @Test
    void should_supportPositiveAnnotation() {
        //given
        for (int i = 0; i < 25; i++) {

            //when
            PositiveValidatedBean actual = some(PositiveValidatedBean.class);

            //then
            assertThat(actual.bigDecimal.doubleValue()).isGreaterThan(0);
            assertThat(actual.bigInteger.doubleValue()).isGreaterThan(0);
            assertThat(actual.intNumber).isGreaterThan(0);
            assertThat(actual.longNumber).isGreaterThan(0);
            assertThat(actual.floatNumber).isGreaterThan(0);
            assertThat(actual.doubleNumber).isGreaterThan(0);
            assertThat(actual.shortNumber).isGreaterThan((short) 0);
            assertThat(actual.byteNumber).isGreaterThan((byte) 0);
        }
    }

}

class PositiveValidatedBean {
    @Positive BigDecimal bigDecimal;
    @Positive BigInteger bigInteger;
    @Positive int intNumber;
    @Positive Long longNumber;
    @Positive float floatNumber;
    @Positive Double doubleNumber;
    @Positive short shortNumber;
    @Positive byte byteNumber;
}