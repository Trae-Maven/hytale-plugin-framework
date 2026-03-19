package io.github.trae.hytale.framework.system;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.EcsEvent;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.trae.hytale.framework.system.data.SystemContext;
import io.github.trae.hytale.framework.system.interfaces.ICustomEventSystem;

import javax.annotation.Nonnull;

/**
 * Abstract base class for custom entity-store ECS event systems.
 *
 * <p>Bridges the Hytale {@link EntityEventSystem} API with the framework's
 * {@link ICustomEventSystem} contract by converting the raw
 * {@link #handle(int, ArchetypeChunk, Store, CommandBuffer, EcsEvent)} parameters
 * into a {@link SystemContext} and delegating to
 * {@link ICustomEventSystem#onEvent(EcsEvent, SystemContext)}.</p>
 *
 * <p>Concrete implementations define their event handling logic in
 * {@code onEvent} and declare the required component types via the
 * ECS system annotation.</p>
 *
 * @param <EventType> the ECS event type this system handles
 * @see CustomChunkEventSystem
 * @see SystemContext
 */
public abstract class CustomEntityEventSystem<EventType extends EcsEvent> extends EntityEventSystem<EntityStore, EventType> implements ICustomEventSystem<EntityStore, EventType> {

    /**
     * Creates a new entity event system for the given event type.
     *
     * @param eventType the class of the ECS event this system listens for
     */
    public CustomEntityEventSystem(@Nonnull final Class<EventType> eventType) {
        super(eventType);
    }

    /**
     * Handles an incoming ECS event by wrapping the raw parameters into a
     * {@link SystemContext} and delegating to {@link #onEvent(EcsEvent, SystemContext)}.
     *
     * @param index          the entity's index within the archetype chunk
     * @param archetypeChunk the chunk containing the entity's component data
     * @param store          the backing entity store
     * @param commandBuffer  the command buffer for deferred mutations
     * @param eventType      the ECS event instance
     */
    @Override
    public void handle(final int index, @Nonnull final ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull final Store<EntityStore> store, @Nonnull final CommandBuffer<EntityStore> commandBuffer, @Nonnull final EventType eventType) {
        this.onEvent(eventType, new SystemContext<>(index, archetypeChunk, store, commandBuffer));
    }
}