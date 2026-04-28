package io.github.trae.hytale.framework.wrappers.interfaces;

import com.hypixel.hytale.server.core.entity.Entity;
import io.github.trae.hytale.framework.wrappers.BlockLocation;

import java.util.List;

public interface IChunk {

    BlockLocation getBlockAt(final int blockX, final int blockY, final int blockZ);

    List<BlockLocation> getBlockLocations();

    List<BlockLocation> getOutlineBlockLocations(final int blockY);

    List<BlockLocation> getOutlineHighestBlockLocations();

    <EntityType extends Entity> List<EntityType> getEntitiesByType(final Class<EntityType> clazz);

    List<Entity> getEntities();
}