package com.ocadotechnology.gembus.bugfix.concurrency;

import com.ocadotechnology.gembus.test.Arranger;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class ConcurrencyTest {

    @Test
    void concurrentSomeWithOverridesDoesNotThrow() {
        assertDoesNotThrow(() -> {
            IntStream.range(0, 10).parallel().mapToObj(i -> createCustomStruct()).toList();
        });
    }

    private CustomStruct createCustomStruct() {
        return Arranger.some(CustomStruct.class, Map.of("id", () -> "id", "uuid", UUID::randomUUID));
    }
}

