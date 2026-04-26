package io.github.trae.hytale.framework.wrappers.models.interfaces;

import com.hypixel.hytale.server.core.entity.Entity;
import io.github.trae.hytale.framework.wrappers.models.BlockLocation;

import java.util.List;

public interface IChunk {

    BlockLocation getBlockAt(final int blockX, final int blockY, final int blockZ);

    List<BlockLocation> getBlockLocations();

    List<BlockLocation> getOutlineBlockLocations(final int blockY);

    <EntityType extends Entity> List<EntityType> getEntitiesByType(final Class<EntityType> clazz);

    List<Entity> getEntities();
}