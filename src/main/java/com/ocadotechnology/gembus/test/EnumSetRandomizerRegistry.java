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

import org.jeasy.random.EasyRandomParameters;
import org.jeasy.random.annotation.Priority;
import org.jeasy.random.api.Randomizer;
import org.jeasy.random.api.RandomizerRegistry;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

/**
 * EnumSet has no accessible constructor, therefore the generic mechanism EasyRandom uses for collections
 * (reflective instantiation with a fallback to Objenesis) ends up with an InstantiationError.
 * This registry recognizes fields of type EnumSet, extracts the enum type from the field declaration
 * and creates the set with the dedicated EnumSet factory method.
 * <p>
 * The priority is aligned with {@link CustomArrangerRandomizerRegistry}, i.e. lower than the
 * BeanValidationRandomizerRegistry one but greater than the InternalRandomizerRegistry one.
 */
@Priority(-3)
public class EnumSetRandomizerRegistry implements RandomizerRegistry {

    private final Random random;
    private EasyRandomParameters.Range<Integer> collectionSizeRange = new EasyRandomParameters().getCollectionSizeRange();

    public EnumSetRandomizerRegistry(long seed) {
        this.random = new Random(seed);
    }

    @Override
    public void init(EasyRandomParameters parameters) {
        this.collectionSizeRange = parameters.getCollectionSizeRange();
    }

    @Override
    public Randomizer<?> getRandomizer(Field field) {
        if (!isEnumSet(field.getType())) {
            return null;
        }
        return EnumTypeExtractor.extractEnumType(field.getGenericType())
                .<Randomizer<?>>map(enumType -> () -> randomEnumSet(enumType))
                .orElse(null);
    }

    /**
     * @return always null, the enum type cannot be determined without the field declaration
     */
    @Override
    public Randomizer<?> getRandomizer(Class<?> type) {
        return null;
    }

    static boolean isEnumSet(Class<?> type) {
        return EnumSet.class.isAssignableFrom(type);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private EnumSet<?> randomEnumSet(Class<? extends Enum> enumType) {
        EnumSet result = EnumSet.noneOf(enumType);
        List<Enum> constants = new ArrayList<>(Arrays.asList(enumType.getEnumConstants()));
        Collections.shuffle(constants, random);
        result.addAll(constants.subList(0, sizeOfSetOfGivenNumberOfConstants(constants.size())));
        return result;
    }

    private int sizeOfSetOfGivenNumberOfConstants(int numberOfConstants) {
        int max = Math.min(collectionSizeRange.getMax(), numberOfConstants);
        int min = Math.min(collectionSizeRange.getMin(), max);
        return min + random.nextInt(1 + max - min);
    }
}
