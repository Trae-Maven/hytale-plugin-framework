package io.github.trae.hytale.framework.wrappers.interfaces;

import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import io.github.trae.hytale.framework.wrappers.Chunk;

public interface IBlockLocation {

    Chunk getChunk();

    BlockType getBlockType();
}