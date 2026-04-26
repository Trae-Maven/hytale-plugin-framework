package io.github.trae.hytale.framework.wrappers;

import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import io.github.trae.hytale.framework.wrappers.interfaces.IPlayerRefWrapper;
import io.github.trae.hytale.framework.wrappers.models.Chunk;
import io.github.trae.hytale.framework.wrappers.models.EntityLocation;
import lombok.AllArgsConstructor;

import java.util.UUID;

/**
 * Wraps a {@link PlayerRef} to provide chunk and location access
 * without requiring the player entity to be loaded in a world store.
 *
 * <p>Position is derived from the {@link PlayerRef#getTransform()} snapshot,
 * and the world is resolved via {@link Universe#getWorld(UUID)} from the
 * stored world UUID. Both methods return null if the world UUID is absent
 * or the world is no longer loaded.</p>
 */
@AllArgsConstructor
public class PlayerRefWrapper implements IPlayerRefWrapper {

    private final PlayerRef playerRef;

    /**
     * Returns the chunk the player is currently in, or null if the
     * world UUID is unset or the world is not loaded.
     */
    @Override
    public Chunk getChunk() {
        final UUID worldUuid = this.playerRef.getWorldUuid();
        if (worldUuid == null) {
            return null;
        }

        final World world = Universe.get().getWorld(worldUuid);
        if (world == null) {
            return null;
        }

        return Chunk.of(world, this.playerRef.getTransform().getPosition());
    }

    /**
     * Returns the player's entity location including rotation, or null
     * if the world UUID is unset or the world is not loaded.
     */
    @Override
    public EntityLocation getLocation() {
        final UUID worldUuid = this.playerRef.getWorldUuid();
        if (worldUuid == null) {
            return null;
        }

        final World world = Universe.get().getWorld(worldUuid);
        if (world == null) {
            return null;
        }

        final Transform transform = this.playerRef.getTransform();

        return EntityLocation.of(world, transform.getPosition(), transform.getRotation());
    }
}