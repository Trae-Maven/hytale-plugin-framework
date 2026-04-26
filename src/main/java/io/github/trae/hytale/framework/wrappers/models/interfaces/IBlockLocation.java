package io.github.trae.hytale.framework.wrappers.models.interfaces;

import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import io.github.trae.hytale.framework.wrappers.models.Location;

public interface IBlockLocation extends Location {

    BlockType getBlockType();
}