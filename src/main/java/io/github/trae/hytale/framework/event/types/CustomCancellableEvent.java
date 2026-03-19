package io.github.trae.hytale.framework.event.types;

import io.github.trae.hytale.framework.event.types.interfaces.ICustomCancellableEvent;
import lombok.Getter;
import lombok.Setter;

/**
 * A synchronous event that supports cancellation.
 *
 * <p>Extends {@link CustomEvent} with cancellation semantics provided by
 * {@link ICustomCancellableEvent}. Listeners can cancel this event via
 * {@link #setCancelled(boolean)} or {@link #setCancelledWithReason(String)},
 * and callers can inspect the result post-dispatch.</p>
 *
 * @see CustomCancellableAsyncEvent
 */
@Getter
public class CustomCancellableEvent extends CustomEvent implements ICustomCancellableEvent {

    /**
     * Whether this event has been cancelled by a listener.
     */
    @Setter
    private boolean cancelled;

    /**
     * Optional human-readable reason for the cancellation, or {@code null} if unset.
     */
    private String cancelledReason;

    /**
     * Cancels this event with a descriptive reason.
     *
     * <p>Sets the {@code cancelledReason} and marks the event as cancelled.
     * This is a convenience method equivalent to calling
     * {@code setCancelled(true)} after setting the reason.</p>
     *
     * @param cancelledReason the reason for cancellation
     */
    @Override
    public void setCancelledWithReason(final String cancelledReason) {
        this.cancelledReason = cancelledReason;
        this.setCancelled(true);
    }
}