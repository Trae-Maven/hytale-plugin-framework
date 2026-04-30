package io.github.trae.hytale.framework.system.interfaces;

import io.github.trae.hytale.framework.system.data.SystemContext;

/**
 * Contract for custom ECS ticking systems within the framework.
 *
 * <p>Mirrors {@link ICustomEventSystem} but for per-tick processing
 * rather than event-driven handling.</p>
 *
 * @param <ECS_TYPE> the ECS store type (e.g. {@code EntityStore})
 */
public interface ICustomTickingSystem<ECS_TYPE> {

    /**
     * Called each tick for every entity matching the system's query.
     *
     * @param dt      the delta time since the last tick
     * @param context the system context wrapping the entity's ECS data
     */
    void onTick(final float dt, final SystemContext<ECS_TYPE> context);
}