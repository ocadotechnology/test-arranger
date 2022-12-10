package com.ocadotechnology.gembus.test;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ArrangerTestOverrideDefaults {

    @AfterEach
    public void cleanupProperties() {
         System.getProperties().remove(PropertiesWrapperTest.overrideKey);
    }

    @Test
    void shouldOverrideDefaultsWhenOverrideDefaultsPropertyIsSet() {
        //given
        System.setProperty(PropertiesWrapperTest.overrideKey, "true");

        //when
        //Arranger.some is initialized too early and the property won't affect it
        EnhancedRandom enhancedRandom = ArrangersConfigurer.instance().defaultRandom();
        WithDefaults actual = enhancedRandom.nextObject(WithDefaults.class);

        //then
        assertThat(actual.getEmpty()).isNotEmpty();
        assertThat(actual.getList()).isNotEmpty();
    }

    @Test
    void shouldRespectDefaultsWhenOverrideDefaultsPropertyIsNotSet() {
        //when
        WithDefaults actual = Arranger.some(WithDefaults.class);

        //then
        assertThat(actual.getEmpty()).isEmpty();
        assertThat(actual.getList()).isEmpty();
    }
}

class WithDefaults {
    private String empty = "";
    private List<String> list = new ArrayList<String>();


    public String getEmpty() {
        return empty;
    }

    public List<String> getList() {
        return list;
    }
}