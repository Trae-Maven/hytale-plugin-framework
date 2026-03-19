package io.github.trae.hytale.framework.helper;

import com.hypixel.hytale.component.SystemType;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.function.consumer.BooleanConsumer;
import io.github.trae.hytale.framework.HytalePlugin;
import io.github.trae.hytale.framework.helper.abstracts.AbstractHelper;
import io.github.trae.hytale.framework.system.CustomChunkEventSystem;
import io.github.trae.hytale.framework.system.CustomEntityEventSystem;
import io.github.trae.hytale.framework.system.interfaces.ICustomEventSystem;

import java.util.LinkedHashMap;

/**
 * Helper responsible for managing ECS (Entity Component System) event system registrations.
 *
 * <p>Handles registration of {@link CustomEntityEventSystem} and {@link CustomChunkEventSystem}
 * instances with the plugin's {@link com.hypixel.hytale.server.core.universe.world.storage.EntityStore}
 * registry. Only systems implementing {@link ICustomEventSystem} are accepted.</p>
 *
 * <p>Each registered system is tracked alongside a {@link BooleanConsumer} cleanup callback
 * that handles unregistration. The boolean parameter indicates whether the cleanup is
 * occurring during server shutdown ({@code true}) or normal unregistration ({@code false}).</p>
 */
public class SystemHelper extends AbstractHelper<EntityEventSystem<?, ?>> {

    /**
     * Map of registered systems to their cleanup callbacks.
     * The {@link BooleanConsumer} accepts {@code true} for shutdown, {@code false} for normal unregistration.
     */
    private final LinkedHashMap<EntityEventSystem<?, ?>, BooleanConsumer> REGISTRATIONS = new LinkedHashMap<>();

    /**
     * Creates a new {@link SystemHelper} bound to the given plugin.
     *
     * @param plugin the owning plugin instance
     */
    public SystemHelper(final HytalePlugin plugin) {
        super(plugin);
    }

    /**
     * Registers an ECS event system with the entity store registry.
     *
     * <p>Only accepts systems implementing {@link ICustomEventSystem}. Duplicate
     * registrations are silently ignored. Supports both {@link CustomEntityEventSystem}
     * and {@link CustomChunkEventSystem} variants, registering each with the
     * appropriate {@link SystemType}.</p>
     *
     * @param entityEventSystem the event system to register
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void register(final EntityEventSystem<?, ?> entityEventSystem) {
        // Only accept framework-managed event systems
        if (!(entityEventSystem instanceof ICustomEventSystem<?, ?>)) {
            return;
        }

        // Prevent duplicate registration
        if (this.REGISTRATIONS.containsKey(entityEventSystem)) {
            return;
        }

        // Register entity-store-based event systems
        if (entityEventSystem instanceof final CustomEntityEventSystem<?> customEntityEventSystem) {
            final SystemType systemType = this.getPlugin().getEntityStoreRegistry().registerSystemType((Class) customEntityEventSystem.getClass());

            this.REGISTRATIONS.put(customEntityEventSystem, (shutdown) -> {
                if (!(shutdown)) {
                    systemType.getRegistry().unregisterSystemType(systemType);
                }
            });
        }

        // Register chunk-store-based event systems
        if (entityEventSystem instanceof final CustomChunkEventSystem<?> customChunkEventSystem) {
            final SystemType systemType = this.getPlugin().getEntityStoreRegistry().registerSystemType((Class) customChunkEventSystem.getClass());

            this.REGISTRATIONS.put(customChunkEventSystem, (shutdown) -> {
                if (!(shutdown)) {
                    systemType.getRegistry().unregisterSystemType(systemType);
                }
            });
        }
    }

    /**
     * Unregisters an ECS event system and invokes its cleanup callback.
     *
     * <p>The cleanup callback is invoked with {@code false}, indicating this is
     * a normal unregistration rather than a server shutdown. If the system was
     * never registered, this call is a no-op.</p>
     *
     * @param entityEventSystem the event system to unregister
     */
    @Override
    public void unregister(final EntityEventSystem<?, ?> entityEventSystem) {
        // Only accept framework-managed event systems
        if (!(entityEventSystem instanceof ICustomEventSystem<?, ?>)) {
            return;
        }

        final BooleanConsumer unregister = this.REGISTRATIONS.remove(entityEventSystem);
        if (unregister == null) {
            return;
        }

        // Pass false to indicate this is not a shutdown-triggered cleanup
        unregister.accept(false);
    }
}