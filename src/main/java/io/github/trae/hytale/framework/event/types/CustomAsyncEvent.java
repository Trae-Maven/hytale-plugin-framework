package io.github.trae.hytale.framework.event.types;

import com.hypixel.hytale.event.IAsyncEvent;

/**
 * Base class for custom asynchronous events within the framework.
 *
 * <p>Implements {@link IAsyncEvent} with a {@link Void} key type, indicating
 * that no key-based filtering is applied during dispatch. Extend this
 * class to define concrete asynchronous events.</p>
 *
 * @see CustomCancellableAsyncEvent
 * @see CustomEvent
 */
public class CustomAsyncEvent implements IAsyncEvent<Void> {
}