package io.github.trae.hytale.framework.utility;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import lombok.experimental.UtilityClass;

import java.util.Optional;
import java.util.UUID;

/**
 * Utility class for UUID-related operations.
 */
@UtilityClass
public class UtilUUID {

    /**
     * Resolves the {@link UUID} associated with an entity via its {@link UUIDComponent}.
     *
     * <p>Navigates the entity's {@link Ref} to its backing {@link Store} and
     * reads the {@link UUIDComponent}. Returns {@link Optional#empty()} if the
     * entity has no active reference or no {@link UUIDComponent} attached.</p>
     *
     * @param entity the entity to resolve the UUID for
     * @return an {@link Optional} containing the UUID, or empty if unavailable
     */
    public static Optional<UUID> getIdByEntity(final Entity entity) {
        final Ref<EntityStore> entityReference = entity.getReference();
        if (entityReference == null) {
            return Optional.empty();
        }

        final Store<EntityStore> store = entityReference.getStore();

        final UUIDComponent uuidComponent = store.getComponent(entityReference, UUIDComponent.getComponentType());
        if (uuidComponent == null) {
            return Optional.empty();
        }

        return Optional.of(uuidComponent.getUuid());
    }
}