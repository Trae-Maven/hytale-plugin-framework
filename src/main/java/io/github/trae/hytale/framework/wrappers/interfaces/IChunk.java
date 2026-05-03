package io.github.trae.hytale.framework.wrappers.interfaces;

import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.universe.world.World;
import io.github.trae.hytale.framework.wrappers.BlockLocation;
import io.github.trae.hytale.framework.wrappers.Chunk;

import java.util.List;

public interface IChunk {

    World getWorld();

    BlockLocation getBlockAt(final int blockX, final int blockY, final int blockZ);

    List<BlockLocation> getBlockLocations();

    List<BlockLocation> getOutlineBlockLocations(final int blockY);

    List<BlockLocation> getOutlineHighestBlockLocations();

    List<Chunk> getNearbyChunks(final int distance, final boolean includeSelf);

    <EntityType extends Entity> List<EntityType> getEntitiesByType(final Class<EntityType> clazz);

    List<Entity> getEntities();
}