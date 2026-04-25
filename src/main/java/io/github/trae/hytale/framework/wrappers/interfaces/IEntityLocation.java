package io.github.trae.hytale.framework.wrappers.interfaces;

import io.github.trae.hytale.framework.wrappers.BlockLocation;
import io.github.trae.hytale.framework.wrappers.Chunk;

public interface IEntityLocation {

    Chunk getChunk();

    BlockLocation toBlockLocation();
}