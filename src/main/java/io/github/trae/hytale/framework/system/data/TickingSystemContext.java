package io.github.trae.hytale.framework.system.data;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import io.github.trae.hytale.framework.system.data.abstracts.AbstractSystemContext;
import lombok.Getter;

import javax.annotation.Nonnull;

/**
 * Context object for {@link io.github.trae.hytale.framework.system.annotations.TickSystemHandler}
 * methods, combining the tick delta time and entity archetype data into a single parameter.
 *
 * <p>Provides access to the delta time since the last tick, the entity's archetype chunk
 * for direct component reads, and the command buffer for deferred mutations.</p>
 */
@Getter
public class TickingSystemContext<ECS_TYPE> extends AbstractSystemContext<ECS_TYPE> {

    /**
     * The delta time in seconds since the last tick.
     */
    private final float deltaTime;

    /**
     * The entity's index within the archetype chunk.
     */
    private final int index;

    /**
     * The archetype chunk containing the entity's component data.
     */
    @Nonnull
    private final ArchetypeChunk<ECS_TYPE> archetypeChunk;

    /**
     * Creates a new tick system context.
     *
     * @param deltaTime      the delta time since the last tick
     * @param index          the entity's index within the archetype chunk
     * @param archetypeChunk the chunk containing the entity's component data
     * @param store          the backing entity store
     * @param commandBuffer  the command buffer for deferred mutations
     */
    public TickingSystemContext(final float deltaTime, final int index, @Nonnull final ArchetypeChunk<ECS_TYPE> archetypeChunk, @Nonnull final Store<ECS_TYPE> store, @Nonnull final CommandBuffer<ECS_TYPE> commandBuffer) {
        super(store, commandBuffer);

        this.deltaTime = deltaTime;
        this.index = index;
        this.archetypeChunk = archetypeChunk;
    }

    /**
     * Returns a {@link Ref} handle to the entity at the current index.
     *
     * @return a reference to the entity
     */
    public Ref<ECS_TYPE> getRef() {
        return this.archetypeChunk.getReferenceTo(this.index);
    }

    /**
     * Retrieves a component from the entity at the current index.
     *
     * @param componentType the type descriptor of the component to retrieve
     * @param <T>           the component type
     * @return the component instance
     */
    public <T extends Component<ECS_TYPE>> T getComponent(@Nonnull final ComponentType<ECS_TYPE, T> componentType) {
        return this.archetypeChunk.getComponent(this.index, componentType);
    }

    /**
     * Queues a component addition for the referenced entity via the {@link CommandBuffer}.
     *
     * @param componentType the type descriptor of the component to add
     * @param component     the component instance
     * @param <T>           the component type
     */
    public <T extends Component<ECS_TYPE>> void addComponent(@Nonnull final ComponentType<ECS_TYPE, T> componentType, @Nonnull final T component) {
        this.getCommandBuffer().addComponent(this.getRef(), componentType, component);
    }
}