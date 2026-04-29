package io.github.trae.hytale.framework.wrappers.interfaces;

import com.hypixel.hytale.math.vector.Vector3d;
import io.github.trae.hytale.framework.wrappers.BlockLocation;
import io.github.trae.hytale.framework.wrappers.Location;

public interface IEntityLocation extends Location {

    BlockLocation toBlockLocation();

    Vector3d toVector();
}