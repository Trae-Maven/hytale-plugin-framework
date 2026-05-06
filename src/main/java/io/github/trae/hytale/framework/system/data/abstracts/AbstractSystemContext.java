package io.github.trae.hytale.framework.system.data.abstracts;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.annotation.Nonnull;

/**
 * Abstract base context shared by all annotation-driven ECS system handlers.
 *
 * <p>Provides access to the backing {@link Store} and {@link CommandBuffer},
 * which are common to every system type (event, tick, and ref change).</p>
 *
 * @see io.github.trae.hytale.framework.system.data.EventSystemContext
 * @see io.github.trae.hytale.framework.system.data.TickingSystemContext
 * @see io.github.trae.hytale.framework.system.data.RefChangeSystemContext
 */
@AllArgsConstructor
@Getter
public abstract class AbstractSystemContext<ECS_TYPE> {

    /**
     * The backing store for the ECS world.
     */
    @Nonnull
    private final Store<ECS_TYPE> store;

    /**
     * The command buffer for deferred component mutations.
     */
    @Nonnull
    private final CommandBuffer<ECS_TYPE> commandBuffer;
}