package io.github.trae.hytale.framework.wrappers;

import com.hypixel.hytale.server.core.universe.world.World;

public interface Location {

    World getWorld();

    Chunk getChunk();
}