package com.ocado.gembus.test;

import io.github.benas.randombeans.api.EnhancedRandom;

import java.lang.reflect.ParameterizedType;

/**
 * <p>Extend this class to provide custom Arranger implementations.
 * It will be automatically picked up by {@link Arranger} and used whenever a new instance of {@code T} is created by the {@link Arranger}.</p>
 * <p>
 * Custom Arranger example (in kotlin):
 * <pre>{@code
 *  class EntityArranger : CustomArranger<Entity>() {
 *       override fun instance(): Entity = enhancedRandom.nextObject(Entity::class.java).copy(field=fixedValue)
 *  }
 * }</pre>
 */
public abstract class CustomArranger<T> {

    protected EnhancedRandom enhancedRandom = ArrangerBuilder.getEnhancedRandomBuilder().build();
    protected final Class<T> type = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];

    /**
     * <p>Method for internal Arranger creation of objects of type T.
     * Do no use this method directly to generate test objects.
     * Use {@code some<T>()} in Kotlin and {@link Arranger#some(Class, String...)} in Java instead.</p>
     *
     * <p>However, never use {@code some<T>()} nor {@link Arranger#some(Class, String...)} in the {@link CustomArranger#instance()} method implementation.
     * Inside the method the {@code enhancedRandom} field should be used instead.</p>
     *
     * @return instance of type T, by default a random one
     */
    protected T instance() {
        return enhancedRandom.nextObject(type);
    }

    final void setEnhancedRandom(EnhancedRandom enhancedRandom) {
        this.enhancedRandom = enhancedRandom;
    }
}
