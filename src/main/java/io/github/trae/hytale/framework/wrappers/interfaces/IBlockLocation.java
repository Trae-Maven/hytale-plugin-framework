package io.github.trae.hytale.framework.wrappers.interfaces;

import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import io.github.trae.hytale.framework.wrappers.Location;
import org.joml.Vector3d;
import org.joml.Vector3i;

public interface IBlockLocation extends Location {

    BlockType getBlockType();

    Vector3i getPosition();

    Vector3d getPosition3d();
}