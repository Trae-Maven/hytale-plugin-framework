package io.github.trae.hytale.framework.utility;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.trae.hytale.framework.wrappers.Chunk;
import lombok.experimental.UtilityClass;

import java.util.UUID;

@UtilityClass
public class UtilChunk {

    /**
     * Resolves the chunk a player is in from their {@link PlayerRef} transform snapshot.
     * Returns null if the world UUID is unset or the world is not loaded.
     */
    public static Chunk getChunkByPlayerRef(final PlayerRef playerRef) {
        final UUID worldUuid = playerRef.getWorldUuid();
        if (worldUuid == null) {
            return null;
        }

        final World world = Universe.get().getWorld(worldUuid);
        if (world == null) {
            return null;
        }

        return Chunk.of(world, playerRef.getTransform().getPosition());
    }

    /**
     * Resolves the chunk a player is in from their {@link Player} entity's
     * {@link TransformComponent} in the ECS store.
     * Returns null if the entity reference or transform component is unavailable.
     */
    public static Chunk getChunkByPlayer(final Player player) {
        final Ref<EntityStore> reference = player.getReference();
        if (reference == null) {
            return null;
        }

        final TransformComponent transformComponent = reference.getStore().getComponent(reference, TransformComponent.getComponentType());
        if (transformComponent == null) {
            return null;
        }

        return Chunk.of(player.getWorld(), transformComponent.getPosition());
    }
}