package io.github.trae.hytale.framework.event;

import io.github.trae.hytale.framework.helper.EventHelper;

/**
 * Marker interface for event listener classes within the framework.
 *
 * <p>Classes implementing this interface can be registered with
 * {@link EventHelper} to receive
 * event callbacks. Methods annotated with
 * {@link io.github.trae.hytale.framework.event.annotations.EventHandler}
 * and accepting a single event parameter will be automatically discovered
 * and bound to the event bus during registration.</p>
 */
public interface EventListener {
}