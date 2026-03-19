package io.github.trae.hytale.framework.system;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.EcsEvent;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import io.github.trae.hytale.framework.system.data.SystemContext;
import io.github.trae.hytale.framework.system.interfaces.ICustomEventSystem;

import javax.annotation.Nonnull;

/**
 * Abstract base class for custom chunk-store ECS event systems.
 *
 * <p>Bridges the Hytale {@link EntityEventSystem} API with the framework's
 * {@link ICustomEventSystem} contract by converting the raw
 * {@link #handle(int, ArchetypeChunk, Store, CommandBuffer, EcsEvent)} parameters
 * into a {@link SystemContext} and delegating to
 * {@link ICustomEventSystem#onEvent(EcsEvent, SystemContext)}.</p>
 *
 * <p>Functionally identical to {@link CustomEntityEventSystem} but operates
 * on {@link ChunkStore} rather than
 * {@link com.hypixel.hytale.server.core.universe.world.storage.EntityStore},
 * targeting chunk-level component data.</p>
 *
 * @param <EventType> the ECS event type this system handles
 * @see CustomEntityEventSystem
 * @see SystemContext
 */
public abstract class CustomChunkEventSystem<EventType extends EcsEvent> extends EntityEventSystem<ChunkStore, EventType> implements ICustomEventSystem<ChunkStore, EventType> {

    /**
     * Creates a new chunk event system for the given event type.
     *
     * @param eventType the class of the ECS event this system listens for
     */
    public CustomChunkEventSystem(@Nonnull final Class<EventType> eventType) {
        super(eventType);
    }

    /**
     * Handles an incoming ECS event by wrapping the raw parameters into a
     * {@link SystemContext} and delegating to {@link #onEvent(EcsEvent, SystemContext)}.
     *
     * @param index          the entity's index within the archetype chunk
     * @param archetypeChunk the chunk containing the entity's component data
     * @param store          the backing chunk store
     * @param commandBuffer  the command buffer for deferred mutations
     * @param eventType      the ECS event instance
     */
    @Override
    public void handle(final int index, @Nonnull final ArchetypeChunk<ChunkStore> archetypeChunk, @Nonnull final Store<ChunkStore> store, @Nonnull final CommandBuffer<ChunkStore> commandBuffer, @Nonnull final EventType eventType) {
        this.onEvent(eventType, new SystemContext<>(index, archetypeChunk, store, commandBuffer));
    }
}