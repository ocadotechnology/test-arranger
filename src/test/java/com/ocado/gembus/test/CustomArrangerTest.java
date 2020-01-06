package com.ocado.gembus.test;

import io.github.benas.randombeans.api.EnhancedRandom;
import io.github.benas.randombeans.api.ObjectGenerationException;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class CustomArrangerTest {

    static String whichArrangerWasUsed;
    final static String CHILD = "Child";
    final static String PARENT = "Parent";

    @Test
    public void whatever_shouldGenerateInstancesOfChildClassUsingCustomArranger() {
        //given
        whichArrangerWasUsed = "";
        final EnhancedRandom random = ArrangerBuilder.instance().buildArranger(Optional.empty());

        //when
        final Child actual = random.nextObject(Child.class);

        //then
        assertEquals(Child.class.getCanonicalName(), actual.getClass().getCanonicalName());
        assertEquals(CHILD, whichArrangerWasUsed);
    }

    @Test
    public void whatever_shouldGenerateInstancesOfParentClassUsingCustomArranger() {
        //given
        whichArrangerWasUsed = "";

        //when
        final Parent actual = Arranger.some(Parent.class);

        //then
        assertEquals(Parent.class.getCanonicalName(), actual.getClass().getCanonicalName());
        assertEquals(PARENT, whichArrangerWasUsed);
    }

    @Test
    public void whatever_shouldRaiseException_when_TryingToGenerateAbstractClass() {
        //when
        try {
            Arranger.some(AbstractParent.class);
        } catch (Exception actual) {
            //then
            assertTrue(actual instanceof ObjectGenerationException);
            assertTrue(actual.getMessage().contains(AbstractParent.class.getName()));
        }
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