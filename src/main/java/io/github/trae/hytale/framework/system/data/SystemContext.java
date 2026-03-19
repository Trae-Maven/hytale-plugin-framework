package io.github.trae.hytale.framework.system.data;

import com.hypixel.hytale.component.*;
import io.github.trae.hytale.framework.system.data.interfaces.ISystemContext;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Concrete implementation of {@link ISystemContext} providing access to ECS data
 * within an event system handler.
 *
 * <p>Wraps the entity index, {@link ArchetypeChunk}, {@link Store}, and
 * {@link CommandBuffer} passed to
 * {@link com.hypixel.hytale.component.system.EntityEventSystem#handle} into a
 * single context object, simplifying the handler signature for
 * {@link io.github.trae.hytale.framework.system.interfaces.ICustomEventSystem#onEvent}.</p>
 *
 * @param <ECS_TYPE> the ECS store type (e.g. {@code EntityStore} or {@code ChunkStore})
 */
@AllArgsConstructor
@Getter
public class SystemContext<ECS_TYPE> implements ISystemContext<ECS_TYPE> {

    /**
     * The entity's index within the archetype chunk.
     */
    private final int index;

    /**
     * The archetype chunk containing the entity's component data.
     */
    private final ArchetypeChunk<ECS_TYPE> archetypeChunk;

    /**
     * The backing store for the ECS world.
     */
    private final Store<ECS_TYPE> store;

    /**
     * The command buffer for deferred component mutations.
     */
    private final CommandBuffer<ECS_TYPE> commandBuffer;

    /**
     * Returns a {@link Ref} handle to the entity at the current index.
     *
     * <p>The reference can be used to identify the entity across archetype
     * chunk boundaries and is valid for the duration of the current tick.</p>
     *
     * @return a reference to the entity
     */
    @Override
    public Ref<ECS_TYPE> getRef() {
        return this.getArchetypeChunk().getReferenceTo(this.getIndex());
    }

    /**
     * Queues a component addition for the referenced entity via the {@link CommandBuffer}.
     *
     * <p>The component is not applied immediately; it is deferred until the
     * command buffer is flushed at the end of the system pass.</p>
     *
     * @param componentType the type descriptor of the component to add
     * @param component     the component instance
     * @param <T>           the component type
     */
    @Override
    public <T extends Component<ECS_TYPE>> void addComponent(final ComponentType<ECS_TYPE, T> componentType, final T component) {
        this.getCommandBuffer().addComponent(this.getRef(), componentType, component);
    }

    /**
     * Retrieves a component from the entity at the current index.
     *
     * <p>Reads directly from the {@link ArchetypeChunk}, providing immediate
     * access to the entity's current component state.</p>
     *
     * @param componentType the type descriptor of the component to retrieve
     * @param <T>           the component type
     * @return the component instance
     */
    @Override
    public <T extends Component<ECS_TYPE>> T getComponent(final ComponentType<ECS_TYPE, T> componentType) {
        return this.getArchetypeChunk().getComponent(this.getIndex(), componentType);
    }
}