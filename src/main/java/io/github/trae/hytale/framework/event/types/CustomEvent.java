package io.github.trae.hytale.framework.event.types;

import com.hypixel.hytale.event.IEvent;

/**
 * Base class for custom synchronous events within the framework.
 *
 * <p>Implements {@link IEvent} with a {@link Void} key type, indicating
 * that no key-based filtering is applied during dispatch. Extend this
 * class to define concrete synchronous events.</p>
 *
 * @see CustomCancellableEvent
 * @see CustomAsyncEvent
 */
public class CustomEvent implements IEvent<Void> {
}