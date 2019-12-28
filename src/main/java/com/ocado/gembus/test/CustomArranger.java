package com.ocado.gembus.test;

import io.github.benas.randombeans.api.EnhancedRandom;

import java.lang.reflect.ParameterizedType;

/**
 * <p>Extend this class to provide custom Arranger implementations.
 * It will be automatically picked up by {@code Arranger} and used whenever a new instance of {@code T} is created by {@code Arranger}.</p>
 *
 * Custom Arranger example (in kotlin):
 * <pre>{@code
 *  class EntityArranger : CustomArranger<Entity>() {
 *       override fun whatever(): Entity = enhancedRandom.nextObject(Entity::class.java).copy(field=fixedValue)
 *  }
 * }</pre>
 */
public abstract class CustomArranger<T> {

    protected EnhancedRandom enhancedRandom;
    protected final Class<T> type = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];

    /**
     * <p>Method for internal Arranger creation of objects of type T.
     * Do no use this method directly to generate test objects.
     * Use {@code some<T>()} instead.</p>
     *
     * <p>However, never use {@code some<T>()} in the {@code whatever()} method implementation.
     * Inside the method the {@code enhancedRandom} field should be used.</p>
     *
     * @return instance of type T, by default a random one
     */
    protected T some(){
        return enhancedRandom.nextObject(type);
    }

    protected void setEnhancedRandom(EnhancedRandom enhancedRandom) {
        this.enhancedRandom = enhancedRandom;
    }
}

