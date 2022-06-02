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
package org.jeasy.random;

import org.jeasy.random.api.RandomizerContext;
import org.jeasy.random.randomizers.range.IntegerRangeRandomizer;

/**
 * EasyRandom is in maintenance mode, and is not keen on accepting bugfixes.
 * This class is a workaround, i.e. it hacks Easy random to deliver features not available there.
 */
public class AggressiveWrapper {

    public static int getRandomCollectionSize(EasyRandom easyRandom, RandomizerContext randomizerContext) {
        RandomizationContext context = (RandomizationContext)randomizerContext;
        EasyRandomParameters parameters = context.getParameters();
        EasyRandomParameters.Range<Integer> collectionSizeRange = parameters.getCollectionSizeRange();
        return new IntegerRangeRandomizer(collectionSizeRange.getMin(), collectionSizeRange.getMax(), easyRandom.nextLong()).getRandomValue();
    }

    public static <T> T doPopulateBean(Class<T> type, EasyRandom easyRandom, RandomizerContext randomizerContext) {
        RandomizationContext context = (RandomizationContext)randomizerContext;
        return easyRandom.doPopulateBean(type, context);
    }
}
