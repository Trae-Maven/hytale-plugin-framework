package io.github.trae.hytale.framework.wrappers;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import io.github.trae.hytale.framework.wrappers.interfaces.IEntityLocation;
import io.github.trae.utilities.UtilJava;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Objects;

/**
 * Represents a double-precision entity position within a world, including yaw and pitch rotation.
 * Used for entity-level operations where sub-block precision is needed.
 *
 * <p>Rotation follows the Hytale SDK convention where {@link TransformComponent#getRotation()}
 * returns a {@link Vector3f} with x=pitch, y=yaw, z=roll.</p>
 */
@RequiredArgsConstructor
@Getter
public class EntityLocation implements IEntityLocation {

    private final String worldName;
    private final double x, y, z;
    private final float yaw, pitch;

    private World world;
    private Chunk chunk;
    private BlockLocation blockLocation;

    /**
     * Creates an EntityLocation with no rotation (yaw/pitch both 0).
     */
    public EntityLocation(final World world, final double x, final double y, final double z) {
        this(world.getName(), x, y, z, 0.0F, 0.0F);
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
        return new EntityLocation(world.getName(), vector3d.getX(), vector3d.getY(), vector3d.getZ(), vector3f.getY(), vector3f.getX());
    }

    /**
     * Returns the world this entity location belongs to.
     * Lazily resolves and caches the World instance from the world name.
     */
    @Override
    public World getWorld() {
        if (this.world == null) {
            this.world = Universe.get().getWorld(this.getWorldName());
        }

        return this.world;
    }

    /**
     * Returns the chunk this entity location falls within.
     * Lazily resolves and caches the Chunk instance using bit shift of 5 (Hytale chunks are 32 blocks wide).
     */
    @Override
    public Chunk getChunk() {
        if (this.chunk == null) {
            this.chunk = new Chunk(this.getWorldName(), (int) Math.floor(this.getX()) >> 5, (int) Math.floor(this.getZ()) >> 5);
        }

        return this.chunk;
    }

    /**
     * Converts this entity location to a block location by flooring each coordinate.
     * Lazily resolves and caches the BlockLocation instance.
     * Uses {@link Math#floor} to handle negative coordinates correctly
     * (e.g. -0.5 becomes -1, not 0).
     */
    @Override
    public BlockLocation toBlockLocation() {
        if (this.blockLocation == null) {
            this.blockLocation = new BlockLocation(this.getWorldName(), (int) Math.floor(this.getX()), (int) Math.floor(this.getY()), (int) Math.floor(this.getZ()));
        }

        return this.blockLocation;
    }

    /**
     * Converts this entity location to a {@link Vector3d} position vector
     * with yaw and pitch assigned.
     *
     * @return the double position vector with rotation
     */
    @Override
    public Vector3d toVector() {
        return new Vector3d(this.getX(), this.getY(), this.getZ()).assign(this.getYaw(), this.getPitch());
    }

    /**
     * Serializes this EntityLocation to a map for persistence.
     */
    public static LinkedHashMap<String, Object> serialize(final EntityLocation entityLocation) {
        if (entityLocation == null) {
            return null;
        }

        return UtilJava.createMap(new LinkedHashMap<>(), map -> {
            map.put("WORLD", entityLocation.getWorldName());
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
        if (serializedMap == null || serializedMap.isEmpty()) {
            return null;
        }

        final String worldName = UtilJava.cast(String.class, serializedMap.get("WORLD"));
        final Double x = UtilJava.cast(Double.class, serializedMap.get("X"));
        final Double y = UtilJava.cast(Double.class, serializedMap.get("Y"));
        final Double z = UtilJava.cast(Double.class, serializedMap.get("Z"));
        final Float yaw = UtilJava.cast(Float.class, serializedMap.get("YAW"));
        final Float pitch = UtilJava.cast(Float.class, serializedMap.get("PITCH"));

        return new EntityLocation(worldName, x, y, z, yaw, pitch);
    }

    @Override
    public String toString() {
        return "%s{world=%s, x=%s, y=%s, z=%s}".formatted(this.getClass().getSimpleName(), this.getWorldName(), this.getX(), this.getY(), this.getZ());
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof final EntityLocation entityLocation) {
            if (!(entityLocation.getWorldName().equals(this.getWorldName()))) {
                return false;
            }

            return entityLocation.getX() == this.getX() && entityLocation.getY() == this.getY() && entityLocation.getZ() == this.getZ();
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getWorldName(), this.getX(), this.getY(), this.getZ());
    }
}