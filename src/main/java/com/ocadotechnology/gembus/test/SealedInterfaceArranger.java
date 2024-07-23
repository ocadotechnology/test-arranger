package com.ocadotechnology.gembus.test;

public class SealedInterfaceArranger<T> extends CustomArranger<T> {

    public SealedInterfaceArranger() {
    }

    protected SealedInterfaceArranger(Class<T> clazz) {
        super(clazz);
    }

    @Override
    protected T instance() {
        Class<?>[] subclasses = type.getPermittedSubclasses();
        return (T) enhancedRandom.nextObject(subclasses[enhancedRandom.nextInt(subclasses.length)]);
    }
}
