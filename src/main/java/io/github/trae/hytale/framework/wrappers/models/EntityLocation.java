package io.github.trae.hytale.framework.wrappers.models;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import io.github.trae.hytale.framework.wrappers.models.interfaces.IEntityLocation;
import io.github.trae.utilities.UtilJava;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Objects;

/**
 * Represents a double-precision entity position within a world, including yaw and pitch rotation.
 * Used for entity-level operations where sub-block precision is needed.
 *
 * <p>Rotation follows the Hytale SDK convention where {@link TransformComponent#getRotation()}
 * returns a {@link Vector3f} with x=pitch, y=yaw, z=roll.</p>
 */
@AllArgsConstructor
@Getter
public class EntityLocation implements IEntityLocation {

    private final World world;
    private final double x, y, z;
    private final float yaw, pitch;

    /**
     * Creates an EntityLocation with no rotation (yaw/pitch both 0).
     */
    public EntityLocation(final World world, final double x, final double y, final double z) {
        this(world, x, y, z, 0.0F, 0.0F);
    }

    /**
     * Creates an EntityLocation from a Hytale SDK position vector with no rotation.
     */
    public static EntityLocation of(final World world, final Vector3d vector3d) {
        return new EntityLocation(world, vector3d.getX(), vector3d.getY(), vector3d.getZ());
    }

    /**
     * Creates an EntityLocation from a Hytale SDK position and rotation vector.
     * Rotation is mapped as: yaw = vector3f.y, pitch = vector3f.x.
     */
    public static EntityLocation of(final World world, final Vector3d vector3d, final Vector3f vector3f) {
        return new EntityLocation(world, vector3d.getX(), vector3d.getY(), vector3d.getZ(), vector3f.getY(), vector3f.getX());
    }

    /**
     * Returns the chunk this entity location falls within.
     * Uses {@link Math#floor} to handle negative coordinates correctly before bit shifting.
     */
    @Override
    public Chunk getChunk() {
        return new Chunk(this.getWorld(), (int) Math.floor(this.getX()) >> 5, (int) Math.floor(this.getZ()) >> 5);
    }

    /**
     * Converts this entity location to a block location by flooring each coordinate.
     * Uses {@link Math#floor} to handle negative coordinates correctly
     * (e.g. -0.5 becomes -1, not 0).
     */
    @Override
    public BlockLocation toBlockLocation() {
        return new BlockLocation(this.getWorld(), (int) Math.floor(this.getX()), (int) Math.floor(this.getY()), (int) Math.floor(this.getZ()));
    }

    /**
     * Serializes this EntityLocation to a map for persistence.
     */
    public static LinkedHashMap<String, Object> serialize(final EntityLocation entityLocation) {
        return UtilJava.createMap(new LinkedHashMap<>(), map -> {
            map.put("WORLD", entityLocation.getWorld().getName());
            map.put("X", entityLocation.getX());
            map.put("Y", entityLocation.getY());
            map.put("Z", entityLocation.getZ());
            map.put("YAW", entityLocation.getYaw());
            map.put("PITCH", entityLocation.getPitch());
        });
    }

    /**
     * Deserializes an EntityLocation from a previously serialized map.
     *
     * @return the deserialized EntityLocation, or null if the world is not loaded
     */
    public static EntityLocation deserialize(final LinkedHashMap<String, Object> serializedMap) {
        final String worldName = UtilJava.cast(String.class, serializedMap.get("WORLD"));
        final Double x = UtilJava.cast(Double.class, serializedMap.get("X"));
        final Double y = UtilJava.cast(Double.class, serializedMap.get("Y"));
        final Double z = UtilJava.cast(Double.class, serializedMap.get("Z"));
        final Float yaw = UtilJava.cast(Float.class, serializedMap.get("YAW"));
        final Float pitch = UtilJava.cast(Float.class, serializedMap.get("PITCH"));

        final World world = Universe.get().getWorld(worldName);
        if (world == null) {
            return null;
        }

        return new EntityLocation(world, x, y, z, yaw, pitch);
    }

    @Override
    public String toString() {
        return "%s{world=%s, x=%s, y=%s, z=%s}".formatted(this.getClass().getSimpleName(), this.getWorld().getName(), this.getX(), this.getY(), this.getZ());
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof final EntityLocation entityLocation) {
            if (!(entityLocation.getWorld().getName().equals(this.getWorld().getName()))) {
                return false;
            }

            return entityLocation.getX() == this.getX() && entityLocation.getY() == this.getY() && entityLocation.getZ() == this.getZ();
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getWorld().getName(), this.getX(), this.getY(), this.getZ());
    }
}