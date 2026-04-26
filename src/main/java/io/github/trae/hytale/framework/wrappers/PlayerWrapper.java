package io.github.trae.hytale.framework.wrappers;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.trae.hytale.framework.wrappers.interfaces.IPlayerWrapper;
import io.github.trae.hytale.framework.wrappers.models.Chunk;
import io.github.trae.hytale.framework.wrappers.models.EntityLocation;
import lombok.AllArgsConstructor;

/**
 * Wraps a {@link Player} entity to provide chunk and location access
 * via the ECS {@link TransformComponent} on the player's entity store.
 *
 * <p>Unlike {@link PlayerRefWrapper}, this requires the player to be
 * actively loaded in a world with a valid {@link Ref} and reads position
 * directly from the entity store rather than the {@link com.hypixel.hytale.server.core.universe.PlayerRef}
 * transform snapshot. Both methods return null if the entity reference
 * is invalid or the transform component is missing.</p>
 */
@AllArgsConstructor
public class PlayerWrapper implements IPlayerWrapper {

    private final Player player;

    /**
     * Returns the chunk the player entity is currently in, or null if
     * the entity reference or transform component is unavailable.
     */
    @Override
    public Chunk getChunk() {
        final Ref<EntityStore> reference = this.player.getReference();
        if (reference == null) {
            return null;
        }

        final TransformComponent transformComponent = reference.getStore().getComponent(reference, TransformComponent.getComponentType());
        if (transformComponent == null) {
            return null;
        }

        return Chunk.of(this.player.getWorld(), transformComponent.getPosition());
    }

    /**
     * Returns the player entity's location including rotation, or null
     * if the entity reference or transform component is unavailable.
     */
    @Override
    public EntityLocation getLocation() {
        final Ref<EntityStore> reference = this.player.getReference();
        if (reference == null) {
            return null;
        }

        final TransformComponent transformComponent = reference.getStore().getComponent(reference, TransformComponent.getComponentType());
        if (transformComponent == null) {
            return null;
        }

        return EntityLocation.of(this.player.getWorld(), transformComponent.getPosition(), transformComponent.getRotation());
    }
}