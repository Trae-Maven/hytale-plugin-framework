package io.github.trae.hytale.framework.wrappers.interfaces;

import io.github.trae.hytale.framework.wrappers.BlockLocation;
import io.github.trae.hytale.framework.wrappers.Location;

public interface IEntityLocation extends Location {

    BlockLocation toBlockLocation();
}