package io.github.trae.hytale.framework.wrappers.interfaces;

import com.hypixel.hytale.math.vector.Rotation3f;
import io.github.trae.hytale.framework.wrappers.BlockLocation;
import io.github.trae.hytale.framework.wrappers.Location;
import org.joml.Vector3d;

public interface IEntityLocation extends Location {

    BlockLocation toBlockLocation();

    Vector3d getPosition();

    Rotation3f getRotation();
}