package io.github.trae.hytale.framework.wrappers;

import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import io.github.trae.hytale.framework.wrappers.interfaces.IBlockLocation;
import io.github.trae.utilities.UtilJava;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.LinkedHashMap;

/**
 * Represents an integer-precision block position within a world.
 * Used for block-level operations such as getting/setting block types.
 */
@AllArgsConstructor
@Getter
public class BlockLocation implements IBlockLocation {

    private final World world;
    private final int x, y, z;

    /**
     * Creates a BlockLocation from a Hytale SDK {@link Vector3i}.
     *
     * @param world    the world this location belongs to
     * @param vector3i the block position vector
     * @return a new BlockLocation
     */
    public static BlockLocation of(final World world, final Vector3i vector3i) {
        return new BlockLocation(world, vector3i.getX(), vector3i.getY(), vector3i.getZ());
    }

    /**
     * Returns the chunk this block location falls within.
     * Uses bit shift of 5 (Hytale chunks are 32 blocks wide).
     */
    @Override
    public Chunk getChunk() {
        return new Chunk(this.getWorld(), this.getX() >> 5, this.getZ() >> 5);
    }

    /**
     * Returns the block type at this location in the world.
     */
    @Override
    public BlockType getBlockType() {
        return this.getWorld().getBlockType(this.getX(), this.getY(), this.getZ());
    }

    /**
     * Serializes this BlockLocation to a map for persistence.
     */
    public static LinkedHashMap<String, Object> serialize(final BlockLocation blockLocation) {
        return UtilJava.createMap(new LinkedHashMap<>(), map -> {
            map.put("WORLD", blockLocation.getWorld().getName());
            map.put("X", blockLocation.getX());
            map.put("Y", blockLocation.getY());
            map.put("Z", blockLocation.getZ());
        });
    }

    /**
     * Deserializes a BlockLocation from a previously serialized map.
     *
     * @return the deserialized BlockLocation, or null if the world is not loaded
     */
    public static BlockLocation deserialize(final LinkedHashMap<String, Object> serializedMap) {
        final String worldName = UtilJava.cast(String.class, serializedMap.get("WORLD"));
        final Integer x = UtilJava.cast(Integer.class, serializedMap.get("X"));
        final Integer y = UtilJava.cast(Integer.class, serializedMap.get("Y"));
        final Integer z = UtilJava.cast(Integer.class, serializedMap.get("Z"));

        final World world = Universe.get().getWorld(worldName);
        if (world == null) {
            return null;
        }

        return new BlockLocation(world, x, y, z);
    }
}