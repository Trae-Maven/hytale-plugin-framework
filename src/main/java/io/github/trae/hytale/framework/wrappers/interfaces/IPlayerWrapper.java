package io.github.trae.hytale.framework.wrappers.interfaces;

import io.github.trae.hytale.framework.wrappers.models.Chunk;
import io.github.trae.hytale.framework.wrappers.models.EntityLocation;

public interface IPlayerWrapper {

    Chunk getChunk();

    EntityLocation getLocation();
}