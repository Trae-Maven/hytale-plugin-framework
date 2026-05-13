package io.github.trae.hytale.framework.utility;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.trae.hytale.framework.wrappers.BlockLocation;
import io.github.trae.hytale.framework.wrappers.EntityLocation;
import lombok.experimental.UtilityClass;

import java.util.UUID;

@UtilityClass
public class UtilLocation {

    /**
     * Resolves a player's location including rotation from their {@link PlayerRef}
     * transform snapshot. Returns null if the world UUID is unset or the world is not loaded.
     */
    public static EntityLocation getEntityLocationByPlayerRef(final PlayerRef playerRef) {
        final UUID worldUuid = playerRef.getWorldUuid();
        if (worldUuid == null) {
            return null;
        }

        final World world = Universe.get().getWorld(worldUuid);
        if (world == null) {
            return null;
        }

        final Transform transform = playerRef.getTransform();

        return EntityLocation.of(world, transform.getPosition(), transform.getRotation());
    }

    /**
     * Resolves a player's location including rotation from their {@link Player} entity's
     * {@link TransformComponent} in the ECS store.
     * Returns null if the entity reference or transform component is unavailable.
     */
    public static EntityLocation getEntityLocationByPlayer(final Player player) {
        final Ref<EntityStore> reference = player.getReference();
        if (reference == null) {
            return null;
        }

        final TransformComponent transformComponent = reference.getStore().getComponent(reference, TransformComponent.getComponentType());
        if (transformComponent == null) {
            return null;
        }

        final World world = player.getWorld();
        if (world == null) {
            return null;
        }

        return EntityLocation.of(world, transformComponent.getPosition(), transformComponent.getRotation());
    }

    /**
     * Checks whether two block locations are within a given block distance
     * of each other using squared-distance comparison for performance.
     *
     * <p>Returns {@code false} if either location is {@code null} or if the
     * locations belong to different worlds.</p>
     *
     * @param fromBlockLocation the source block location
     * @param toBlockLocation   the target block location
     * @param distance          the maximum allowed distance
     * @return {@code true} if both locations are within the specified range
     */
    public static boolean isWithinDistance(final BlockLocation fromBlockLocation, final BlockLocation toBlockLocation, final int distance) {
        if (fromBlockLocation == null || toBlockLocation == null) {
            return false;
        }

        if (!(fromBlockLocation.getWorldName().equals(toBlockLocation.getWorldName()))) {
            return false;
        }

        return fromBlockLocation.toVector().distanceSquaredTo(toBlockLocation.toVector()) <= (distance * distance);
    }

    /**
     * Checks whether two entity locations are within a given distance
     * of each other using squared-distance comparison for performance.
     *
     * <p>Useful for movement tolerance checks, teleport validation,
     * and proximity-based entity operations.</p>
     *
     * <p>Returns {@code false} if either location is {@code null} or if the
     * locations belong to different worlds.</p>
     *
     * @param fromEntityLocation the source entity location
     * @param toEntityLocation   the target entity location
     * @param distance           the maximum allowed distance
     * @return {@code true} if both locations are within the specified range
     */
    public static boolean isWithinDistance(final EntityLocation fromEntityLocation, final EntityLocation toEntityLocation, final double distance) {
        if (fromEntityLocation == null || toEntityLocation == null) {
            return false;
        }

        if (!(fromEntityLocation.getWorldName().equals(toEntityLocation.getWorldName()))) {
            return false;
        }

        return fromEntityLocation.toVector().distanceSquaredTo(toEntityLocation.toVector()) <= (distance * distance);
    }
}