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

import org.jeasy.random.ObjectCreationException;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CustomArrangerTest {

    static String whichArrangerWasUsed;
    final static String CHILD = "Child";
    final static String PARENT = "Parent";

    @Test
    public void shouldNotFailWhenInstanceMethodCalledDirectlyOnConstructorCreatedArranger() {
        //given
        ChildArranger sut = new ChildArranger();

        //when
        Child actual = sut.instance();

        //then
        assertThat(actual).isNotNull();
    }

    @Test
    public void shouldGenerateInstancesOfChildClassUsingCustomArranger() {
        //given
        whichArrangerWasUsed = "";
        final EnhancedRandom random = ArrangersConfigurer.instance().defaultRandom();

        //when
        final Child actual = random.nextObject(Child.class);

        //then
        assertEquals(Child.class.getCanonicalName(), actual.getClass().getCanonicalName());
        assertEquals(CHILD, whichArrangerWasUsed);
    }

    @Test
    public void shouldGenerateInstancesOfParentClassUsingCustomArranger() {
        //given
        whichArrangerWasUsed = "";

        //when
        final Parent actual = Arranger.some(Parent.class);

        //then
        assertEquals(Parent.class.getCanonicalName(), actual.getClass().getCanonicalName());
        assertEquals(PARENT, whichArrangerWasUsed);
    }

    @Test
    public void shouldRaiseException_when_TryingToGenerateAbstractClass() {
        //when
        try {
            Arranger.some(AbstractParent.class);
        } catch (Exception actual) {
            //then
            assertTrue(actual instanceof ObjectCreationException);
            assertTrue(actual.getMessage().contains(AbstractParent.class.getName()));
        }
    }

    @Test
    public void customArrangerShouldBreakInfiniteRecursion() {
        //when
        final ForRecursion actual = Arranger.some(ForRecursion.class);

        //then
        assertThat(actual.text.string).isEqualTo(StringWrapperArranger.TEXT);
        assertThat(actual.recursion.get().text.string).isEqualTo(StringWrapperArranger.TEXT);
    }

    @Test
    void shouldUseCustomArrangersWhenStartingFromManuallyCreatedCustomArranger() {
        //given
        ForRecursionArranger forRecursionArranger = new ForRecursionArranger();

        //when
        final ForRecursion actual = forRecursionArranger.create();

        //then
        assertThat(actual.text.string).isEqualTo(StringWrapperArranger.TEXT);
        assertThat(actual.recursion.get().text.string).isEqualTo(StringWrapperArranger.TEXT);
    }
}

abstract class AbstractParent {
    private final String ap;

    protected AbstractParent(String ap) {
        this.ap = ap;
    }
}

class Parent extends AbstractParent {
    private final String p;

    protected Parent(String ap, String p) {
        super(ap);
        this.p = p;
    }
}

class Child extends Parent {
    private final String c;

    protected Child(String ap, String p, String c) {
        super(ap, p);
        this.c = c;
    }
}

class AbstractParentArranger extends CustomArranger<AbstractParentArranger> {
}

class ParentArranger extends CustomArranger<Parent> {
    @Override
    protected Parent instance() {
        CustomArrangerTest.whichArrangerWasUsed = CustomArrangerTest.PARENT;
        return super.instance();
    }
}

class ChildArranger extends CustomArranger<Child> {
    @Override
    protected Child instance() {
        CustomArrangerTest.whichArrangerWasUsed = CustomArrangerTest.CHILD;
        return enhancedRandom.nextObject(type);
    }
}

class ForRecursion {
    StringWrapper text;
    Optional<ForRecursion> recursion;
}

class ForRecursionArranger extends CustomArranger<ForRecursion> {
    @Override
    protected ForRecursion instance() {
        return enhancedRandom.nextObject(ForRecursion.class);
    }
    ForRecursion create(){
        return instance();
    }
}

class StringWrapper {
    String string;
}

class StringWrapperArranger extends CustomArranger<StringWrapper> {
    static final String TEXT = "created using custom arranger";
    @Override
    protected StringWrapper instance() {
        StringWrapper result = enhancedRandom.nextObject(StringWrapper.class);
        result.string = TEXT;
        return result;
    }
}