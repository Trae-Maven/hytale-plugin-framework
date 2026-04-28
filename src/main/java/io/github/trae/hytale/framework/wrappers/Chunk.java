package io.github.trae.hytale.framework.wrappers;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.EntityChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.trae.hytale.framework.wrappers.interfaces.IChunk;
import io.github.trae.utilities.UtilJava;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

/**
 * Represents a chunk column (32x320x32) within a world.
 * Hytale chunks are 32 blocks wide (bit shift 5), unlike Minecraft's 16-wide chunks.
 * Chunks are identified by their X/Z coordinates and have no Y component,
 * as they span the full world height (0-319).
 */
@RequiredArgsConstructor
@Getter
public class Chunk implements IChunk {

    /**
     * Bit shift for converting block coords to chunk coords (32 = 2^5).
     */
    public static final int SHIFT = 5;

    /**
     * Bitmask for extracting local block coords within a chunk (0-31).
     */
    public static final int MASK = 31;

    /**
     * Chunk width in blocks (X axis).
     */
    public static final int WIDTH = 32;

    /**
     * Chunk height in blocks (Y axis, full column).
     */
    public static final int HEIGHT = 320;

    /**
     * Chunk depth in blocks (Z axis).
     */
    public static final int DEPTH = 32;

    /**
     * Total number of blocks in a chunk column.
     */
    public static final int VOLUME = WIDTH * HEIGHT * DEPTH;

    private final String worldName;
    private final int x, z;

    private World world;

    public static Chunk of(final World world, final Vector3d vector3d) {
        return new Chunk(world.getName(), (int) Math.floor(vector3d.getX()) >> 5, (int) Math.floor(vector3d.getZ()) >> 5);
    }

    /**
     * Returns the world this chunk belongs to.
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
     * Returns a BlockLocation at the given block coordinates within this chunk's world.
     */
    @Override
    public BlockLocation getBlockAt(final int blockX, final int blockY, final int blockZ) {
        return new BlockLocation(this.getWorldName(), blockX, blockY, blockZ);
    }

    /**
     * Returns every block location in this chunk column.
     * Warning: allocates {@link #VOLUME} (327,680) BlockLocation objects.
     */
    @Override
    public List<BlockLocation> getBlockLocations() {
        return UtilJava.createCollection(new ArrayList<>(VOLUME), list -> {
            final int minBlockX = this.getX() << SHIFT;
            final int minBlockZ = this.getZ() << SHIFT;

            for (int blockX = minBlockX; blockX < minBlockX + WIDTH; blockX++) {
                for (int blockZ = minBlockZ; blockZ < minBlockZ + DEPTH; blockZ++) {
                    for (int blockY = 0; blockY < HEIGHT; blockY++) {
                        list.add(new BlockLocation(this.getWorldName(), blockX, blockY, blockZ));
                    }
                }
            }
        });
    }

    /**
     * Returns the perimeter block locations of this chunk at a given Y level.
     * Only the outer ring (north/south edges + east/west edges excluding corners) is included.
     */
    @Override
    public List<BlockLocation> getOutlineBlockLocations(final int blockY) {
        return UtilJava.createCollection(new ArrayList<>(), list -> {
            final int minBlockX = this.getX() << SHIFT;
            final int minBlockZ = this.getZ() << SHIFT;

            final int maxBlockX = minBlockX + WIDTH - 1;
            final int maxBlockZ = minBlockZ + DEPTH - 1;

            for (int blockX = minBlockX; blockX <= maxBlockX; blockX++) {
                list.add(new BlockLocation(this.getWorldName(), blockX, blockY, minBlockZ));
                list.add(new BlockLocation(this.getWorldName(), blockX, blockY, maxBlockZ));
            }

            for (int blockZ = minBlockZ + 1; blockZ < maxBlockZ; blockZ++) {
                list.add(new BlockLocation(this.getWorldName(), minBlockX, blockY, blockZ));
                list.add(new BlockLocation(this.getWorldName(), maxBlockX, blockY, blockZ));
            }
        });
    }

    /**
     * Returns the perimeter block locations of this chunk, each at the highest block Y
     * for that position. Uses the chunk's heightmap to determine the top solid block
     * at each outline coordinate, producing a terrain-following border.
     *
     * @return the outline block locations at their respective highest Y, or an empty list if the chunk is not loaded
     */
    @Override
    public List<BlockLocation> getOutlineHighestBlockLocations() {
        final WorldChunk worldChunk = this.getWorld().getChunkIfLoaded(ChunkUtil.indexChunkFromBlock(this.getX() << SHIFT, this.getZ() << SHIFT));
        if (worldChunk == null) {
            return List.of();
        }

        return UtilJava.createCollection(new ArrayList<>(), list -> {
            final int minBlockX = this.getX() << SHIFT;
            final int minBlockZ = this.getZ() << SHIFT;

            final int maxBlockX = minBlockX + WIDTH - 1;
            final int maxBlockZ = minBlockZ + DEPTH - 1;

            for (int blockX = minBlockX; blockX <= maxBlockX; blockX++) {
                final int localX = blockX & MASK;

                list.add(new BlockLocation(this.getWorldName(), blockX, worldChunk.getHeight(localX, 0), minBlockZ));
                list.add(new BlockLocation(this.getWorldName(), blockX, worldChunk.getHeight(localX, MASK), maxBlockZ));
            }

            for (int blockZ = minBlockZ + 1; blockZ < maxBlockZ; blockZ++) {
                final int localZ = blockZ & MASK;

                list.add(new BlockLocation(this.getWorldName(), minBlockX, worldChunk.getHeight(0, localZ), blockZ));
                list.add(new BlockLocation(this.getWorldName(), maxBlockX, worldChunk.getHeight(MASK, localZ), blockZ));
            }
        });
    }

    /**
     * Returns all entities in this chunk that are instances of the given type.
     * Scans the chunk's entity holders and resolves entity components via archetype introspection,
     * avoiding the deprecated {@code EntityUtils} methods.
     *
     * @param clazz the entity class to filter by (e.g. Entity.class, LivingEntity.class, Player.class)
     * @return a list of matching entities, or an empty list if the chunk is not loaded
     */
    @Override
    public <EntityType extends Entity> List<EntityType> getEntitiesByType(final Class<EntityType> clazz) {
        return UtilJava.createCollection(new ArrayList<>(), list -> {
            final WorldChunk worldChunk = this.getWorld().getChunkIfLoaded(ChunkUtil.indexChunkFromBlock(this.getX() << SHIFT, this.getZ() << SHIFT));
            if (worldChunk == null) {
                return;
            }

            final EntityChunk entityChunk = worldChunk.getEntityChunk();
            if (entityChunk == null) {
                return;
            }

            for (final Holder<EntityStore> entityHolder : entityChunk.getEntityHolders()) {
                final Archetype<EntityStore> archetype = entityHolder.getArchetype();
                if (archetype == null) {
                    continue;
                }

                for (int index = archetype.getMinIndex(); index < archetype.length(); index++) {
                    final ComponentType<EntityStore, ?> componentType = archetype.get(index);
                    if (componentType == null) {
                        continue;
                    }

                    if (!(clazz.isAssignableFrom(componentType.getTypeClass()))) {
                        continue;
                    }

                    final EntityType entity = UtilJava.cast(clazz, entityHolder.getComponent(componentType));
                    if (entity != null) {
                        list.add(entity);
                    }
                    break;
                }
            }
        });
    }

    /**
     * Returns all entities in this chunk.
     */
    @Override
    public List<Entity> getEntities() {
        return this.getEntitiesByType(Entity.class);
    }

    /**
     * Serializes this Chunk to a map for persistence.
     */
    public static LinkedHashMap<String, Object> serialize(final Chunk chunk) {
        if (chunk == null) {
            return null;
        }

        return UtilJava.createMap(new LinkedHashMap<>(), map -> {
            map.put("WORLD", chunk.getWorldName());
            map.put("X", chunk.getX());
            map.put("Z", chunk.getZ());
        });
    }

    /**
     * Deserializes a Chunk from a previously serialized map.
     *
     * @return the deserialized Chunk, or null if the world is not loaded
     */
    public static Chunk deserialize(final LinkedHashMap<String, Object> serializedMap) {
        if (serializedMap == null) {
            return null;
        }

        final String worldName = UtilJava.cast(String.class, serializedMap.get("WORLD"));
        final Integer x = UtilJava.cast(Integer.class, serializedMap.get("X"));
        final Integer z = UtilJava.cast(Integer.class, serializedMap.get("Z"));

        return new Chunk(worldName, x, z);
    }

    @Override
    public String toString() {
        return "%s{world=%s, x=%s, z=%s}".formatted(this.getClass().getSimpleName(), this.getWorldName(), this.getX(), this.getZ());
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof final Chunk chunk) {
            if (!(chunk.getWorldName().equals(this.getWorldName()))) {
                return false;
            }

            return chunk.getX() == this.getX() && chunk.getZ() == this.getZ();
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getWorldName(), this.getX(), this.getZ());
    }
}