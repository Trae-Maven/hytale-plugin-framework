package io.github.trae.hytale.framework.system.interfaces;

import com.hypixel.hytale.component.system.EcsEvent;
import io.github.trae.hytale.framework.system.data.SystemContext;

public interface ICustomEventSystem<ECS_TYPE, EventType extends EcsEvent> extends CustomSystem {

    void onEvent(final EventType event, final SystemContext<ECS_TYPE> context);
}