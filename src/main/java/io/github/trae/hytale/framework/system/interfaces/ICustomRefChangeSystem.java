package io.github.trae.hytale.framework.system.interfaces;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ICustomRefChangeSystem<ECS_TYPE, T extends Component<ECS_TYPE>> extends CustomSystem {

    void onAdded(@Nonnull Ref<ECS_TYPE> ref, @Nonnull T component, @Nonnull Store<ECS_TYPE> store, @Nonnull CommandBuffer<ECS_TYPE> commandBuffer);

    void onSet(@Nonnull Ref<ECS_TYPE> ref, @Nullable T oldComponent, @Nonnull T newComponent, @Nonnull Store<ECS_TYPE> store, @Nonnull CommandBuffer<ECS_TYPE> commandBuffer);

    void onRemoved(@Nonnull Ref<ECS_TYPE> ref, @Nonnull T component, @Nonnull Store<ECS_TYPE> store, @Nonnull CommandBuffer<ECS_TYPE> commandBuffer);
}