package com.ocado.gembus.test;

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
    protected MyEntity some() {
        return new MyEntity(ArrangerBuilderTest.hardcoded_string);
    }
}