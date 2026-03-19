package io.github.trae.hytale.framework.helper.abstracts;

import io.github.trae.hytale.framework.HytalePlugin;
import io.github.trae.hytale.framework.helper.abstracts.interfaces.IAbstractHelper;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Abstract base class for framework helpers that manage registerable components.
 *
 * <p>Each helper is bound to a specific {@link HytalePlugin} instance, which provides
 * access to the plugin's registries (event, command, system, etc.). Concrete
 * implementations define the registration and unregistration logic for their
 * component type via {@link IAbstractHelper}.</p>
 *
 * @param <Type> the type of component this helper manages
 * @see io.github.trae.hytale.framework.helper.ListenerHelper
 * @see io.github.trae.hytale.framework.helper.CommandHelper
 * @see io.github.trae.hytale.framework.helper.SystemHelper
 */
@AllArgsConstructor
public abstract class AbstractHelper<Type> implements IAbstractHelper<Type> {

    /**
     * The owning plugin instance, providing access to Hytale registries.
     */
    @Getter(AccessLevel.PROTECTED)
    private final HytalePlugin plugin;
}