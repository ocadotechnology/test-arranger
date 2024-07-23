package com.ocadotechnology.gembus.test;

public class SealedInterfaceArranger extends CustomArranger<Object> {

    private final Class<?> clazz;

    public SealedInterfaceArranger(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    protected Object instance() {
        Class<?>[] subclasses = clazz.getPermittedSubclasses();
        return enhancedRandom.nextObject(subclasses[enhancedRandom.nextInt(subclasses.length)]);
    }
}
