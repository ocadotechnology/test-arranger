package com.ocado.gembus.test;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReflectionsRootTest {

    private final String key = "arranger.root";

    @AfterEach
    public void cleanupProperties() {
            System.getProperties().remove(key);
    }

    @Test
    public void shouldFindRootInSystemProperties() {
        //given
        String expected = Arranger.some(String.class);
        System.setProperty(key, expected);

        //when
        final String actual = ReflectionsRoot.getRootPackage();

        //then
        assertEquals(expected, actual);
    }

    @Test
    public void shouldFindRootInPropertiesFile() {
        //when
        final String actual = ReflectionsRoot.getRootPackage();

        //then
        assertEquals("com.ocado.gembus", actual);
    }
}
