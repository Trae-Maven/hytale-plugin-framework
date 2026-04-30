package io.github.trae.hytale.framework.system;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.trae.hytale.framework.system.data.SystemContext;
import io.github.trae.hytale.framework.system.interfaces.ICustomTickingSystem;

import javax.annotation.Nonnull;

/**
 * Abstract base class for custom entity-store ECS ticking systems.
 *
 * <p>Bridges the Hytale {@link EntityTickingSystem} API with the framework's
 * {@link ICustomTickingSystem} contract by converting the raw
 * {@link #tick(float, int, ArchetypeChunk, Store, CommandBuffer)} parameters
 * into a {@link SystemContext} and delegating to
 * {@link ICustomTickingSystem#onTick(float, SystemContext)}.</p>
 *
 * @see CustomEntityEventSystem
 * @see SystemContext
 */
public abstract class CustomEntityTickingSystem extends EntityTickingSystem<EntityStore> implements ICustomTickingSystem<EntityStore> {

    /**
     * Handles a tick by wrapping the raw parameters into a
     * {@link SystemContext} and delegating to {@link #onTick(float, SystemContext)}.
     *
     * @param dt             the delta time since the last tick
     * @param index          the entity's index within the archetype chunk
     * @param archetypeChunk the chunk containing the entity's component data
     * @param store          the backing entity store
     * @param commandBuffer  the command buffer for deferred mutations
     */
    @Override
    public void tick(final float dt, final int index, @Nonnull final ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull final Store<EntityStore> store, @Nonnull final CommandBuffer<EntityStore> commandBuffer) {
        this.onTick(dt, new SystemContext<>(index, archetypeChunk, store, commandBuffer));
    }
}