package io.github.trae.hytale.framework.system;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.system.RefChangeSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.trae.hytale.framework.system.interfaces.ICustomRefChangeSystem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Abstract base class for custom entity-store ECS ref change systems.
 *
 * <p>Bridges the Hytale {@link RefChangeSystem} API with the framework's
 * {@link ICustomRefChangeSystem} contract. Fires when a specific component
 * type is added, set, or removed on an entity.</p>
 *
 * @param <T> the component type this system watches
 */
public abstract class CustomEntityRefChangeSystem<T extends Component<EntityStore>> extends RefChangeSystem<EntityStore, T> implements ICustomRefChangeSystem<EntityStore, T> {

    private final ComponentType<EntityStore, T> componentType;

    public CustomEntityRefChangeSystem(@Nonnull final ComponentType<EntityStore, T> componentType) {
        this.componentType = componentType;
    }

    @Nonnull
    @Override
    public ComponentType<EntityStore, T> componentType() {
        return this.componentType;
    }

    @Override
    public void onComponentAdded(@Nonnull final Ref<EntityStore> ref, @Nonnull final T component, @Nonnull final Store<EntityStore> store, @Nonnull final CommandBuffer<EntityStore> commandBuffer) {
        this.onAdded(ref, component, store, commandBuffer);
    }

    @Override
    public void onComponentSet(@Nonnull final Ref<EntityStore> ref, @Nullable final T oldComponent, @Nonnull final T newComponent, @Nonnull final Store<EntityStore> store, @Nonnull final CommandBuffer<EntityStore> commandBuffer) {
        this.onSet(ref, oldComponent, newComponent, store, commandBuffer);
    }

    @Override
    public void onComponentRemoved(@Nonnull final Ref<EntityStore> ref, @Nonnull final T component, @Nonnull final Store<EntityStore> store, @Nonnull final CommandBuffer<EntityStore> commandBuffer) {
        this.onRemoved(ref, component, store, commandBuffer);
    }
}