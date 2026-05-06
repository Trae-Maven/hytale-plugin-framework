package io.github.trae.hytale.framework.system;

import io.github.trae.hytale.framework.system.annotations.EventSystemHandler;
import io.github.trae.hytale.framework.system.annotations.RefChangeHandler;
import io.github.trae.hytale.framework.system.annotations.TickSystemHandler;

/**
 * Marker interface for classes that contain ECS system handler methods.
 *
 * <p>Any class that wants to use annotation-driven ECS system registration
 * must implement this interface. Methods are discovered by scanning for
 * {@link EventSystemHandler}, {@link TickSystemHandler}, and {@link RefChangeHandler}
 * annotations.</p>
 *
 * @see EventSystemHandler
 * @see TickSystemHandler
 * @see RefChangeHandler
 */
public interface SystemListener {
}