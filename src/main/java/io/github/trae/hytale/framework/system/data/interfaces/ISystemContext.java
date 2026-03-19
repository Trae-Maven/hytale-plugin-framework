package io.github.trae.hytale.framework.system.data.interfaces;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;

public interface ISystemContext<ECS_TYPE> {

    Ref<ECS_TYPE> getRef();

    <T extends Component<ECS_TYPE>> void addComponent(final ComponentType<ECS_TYPE, T> componentType, T component);

    <T extends Component<ECS_TYPE>> T getComponent(final ComponentType<ECS_TYPE, T> componentType);
}