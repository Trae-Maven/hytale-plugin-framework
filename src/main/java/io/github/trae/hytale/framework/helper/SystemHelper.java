package io.github.trae.hytale.framework.helper;

import com.hypixel.hytale.component.system.ISystem;
import com.hypixel.hytale.function.consumer.BooleanConsumer;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.trae.hytale.framework.HytalePlugin;
import io.github.trae.hytale.framework.helper.abstracts.AbstractHelper;
import io.github.trae.hytale.framework.system.CustomChunkEventSystem;
import io.github.trae.hytale.framework.system.CustomEntityEventSystem;
import io.github.trae.hytale.framework.system.CustomEntityRefChangeSystem;
import io.github.trae.hytale.framework.system.CustomEntityTickingSystem;
import io.github.trae.hytale.framework.system.interfaces.CustomSystem;

import java.util.LinkedHashMap;

public class SystemHelper extends AbstractHelper<CustomSystem> {

    private final LinkedHashMap<Object, BooleanConsumer> REGISTRATIONS = new LinkedHashMap<>();

    public SystemHelper(final HytalePlugin plugin) {
        super(plugin);
    }

    @Override
    public void register(final CustomSystem system) {
        if (this.REGISTRATIONS.containsKey(system)) {
            return;
        }

        if (system instanceof final CustomEntityEventSystem<?> customEntityEventSystem) {
            this.registerEntityStoreSystem(customEntityEventSystem);
        }

        if (system instanceof final CustomChunkEventSystem<?> customChunkEventSystem) {
            this.registerChunkStoreSystem(customChunkEventSystem);
        }

        if (system instanceof final CustomEntityTickingSystem customEntityTickingSystem) {
            this.registerEntityStoreSystem(customEntityTickingSystem);
        }

        if (system instanceof final CustomEntityRefChangeSystem<?> customEntityRefChangeSystem) {
            this.registerEntityStoreSystem(customEntityRefChangeSystem);
        }
    }

    @Override
    public void unregister(final CustomSystem system) {
        final BooleanConsumer unregister = this.REGISTRATIONS.remove(system);
        if (unregister == null) {
            return;
        }

        unregister.accept(false);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void registerEntityStoreSystem(final ISystem<EntityStore> system) {
        this.getPlugin().getEntityStoreRegistry().registerSystem(system);

        this.REGISTRATIONS.put(system, (shutdown) -> {
            if (!(shutdown)) {
                final Class<?> systemClass = system.getClass();
                if (EntityStore.REGISTRY.hasSystemClass((Class) systemClass)) {
                    EntityStore.REGISTRY.unregisterSystem((Class) systemClass);
                }
            }
        });
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void registerChunkStoreSystem(final ISystem<ChunkStore> system) {
        this.getPlugin().getChunkStoreRegistry().registerSystem(system);

        this.REGISTRATIONS.put(system, (shutdown) -> {
            if (!(shutdown)) {
                final Class<?> systemClass = system.getClass();
                if (EntityStore.REGISTRY.hasSystemClass((Class) systemClass)) {
                    EntityStore.REGISTRY.unregisterSystem((Class) systemClass);
                }
            }
        });
    }
}