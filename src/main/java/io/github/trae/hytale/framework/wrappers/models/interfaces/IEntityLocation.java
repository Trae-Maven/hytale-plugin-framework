package io.github.trae.hytale.framework.wrappers.models.interfaces;

import io.github.trae.hytale.framework.wrappers.models.BlockLocation;
import io.github.trae.hytale.framework.wrappers.models.Location;

public interface IEntityLocation extends Location {

    BlockLocation toBlockLocation();
}