package io.github.trae.hytale.framework.helper;

import com.hypixel.hytale.function.consumer.BooleanConsumer;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.trae.hytale.framework.HytalePlugin;
import io.github.trae.hytale.framework.helper.abstracts.AbstractHelper;
import io.github.trae.hytale.framework.system.CustomChunkEventSystem;
import io.github.trae.hytale.framework.system.CustomEntityEventSystem;
import io.github.trae.hytale.framework.system.CustomEntityTickingSystem;
import io.github.trae.hytale.framework.system.interfaces.ICustomEventSystem;
import io.github.trae.hytale.framework.system.interfaces.ICustomTickingSystem;

import java.util.LinkedHashMap;

public class SystemHelper extends AbstractHelper<Object> {

    private final LinkedHashMap<Object, BooleanConsumer> REGISTRATIONS = new LinkedHashMap<>();

    public SystemHelper(final HytalePlugin plugin) {
        super(plugin);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void register(final Object system) {
        if (!(system instanceof ICustomEventSystem<?, ?>) && !(system instanceof ICustomTickingSystem<?>)) {
            return;
        }

        if (this.REGISTRATIONS.containsKey(system)) {
            return;
        }

        if (system instanceof final CustomEntityEventSystem<?> customEntityEventSystem) {
            this.getPlugin().getEntityStoreRegistry().registerSystem(customEntityEventSystem);

            this.REGISTRATIONS.put(customEntityEventSystem, (shutdown) -> {
                if (!(shutdown)) {
                    final Class<?> systemClass = customEntityEventSystem.getClass();
                    if (EntityStore.REGISTRY.hasSystemClass((Class) systemClass)) {
                        EntityStore.REGISTRY.unregisterSystem((Class) systemClass);
                    }
                }
            });
        }

        if (system instanceof final CustomChunkEventSystem<?> customChunkEventSystem) {
            this.getPlugin().getChunkStoreRegistry().registerSystem(customChunkEventSystem);

            this.REGISTRATIONS.put(customChunkEventSystem, (shutdown) -> {
                if (!(shutdown)) {
                    final Class<?> systemClass = customChunkEventSystem.getClass();
                    if (ChunkStore.REGISTRY.hasSystemClass((Class) systemClass)) {
                        ChunkStore.REGISTRY.unregisterSystem((Class) systemClass);
                    }
                }
            });
        }

        if (system instanceof final CustomEntityTickingSystem customEntityTickingSystem) {
            this.getPlugin().getEntityStoreRegistry().registerSystem(customEntityTickingSystem);

            this.REGISTRATIONS.put(customEntityTickingSystem, (shutdown) -> {
                if (!(shutdown)) {
                    final Class<?> systemClass = customEntityTickingSystem.getClass();
                    if (EntityStore.REGISTRY.hasSystemClass((Class) systemClass)) {
                        EntityStore.REGISTRY.unregisterSystem((Class) systemClass);
                    }
                }
            });
        }
    }

    @Override
    public void unregister(final Object system) {
        if (!(system instanceof ICustomEventSystem<?, ?>) && !(system instanceof ICustomTickingSystem<?>)) {
            return;
        }

        final BooleanConsumer unregister = this.REGISTRATIONS.remove(system);
        if (unregister == null) {
            return;
        }

        unregister.accept(false);
    }
}