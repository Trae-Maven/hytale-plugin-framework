package io.github.trae.hytale.framework.utility;

import com.hypixel.hytale.event.IAsyncEvent;
import com.hypixel.hytale.event.IEvent;
import com.hypixel.hytale.server.core.HytaleServer;
import lombok.experimental.UtilityClass;

import java.util.concurrent.CompletableFuture;

/**
 * Utility class for dispatching events through the Hytale event bus.
 *
 * <p>Provides convenience methods for both synchronous ({@link IEvent}) and
 * asynchronous ({@link IAsyncEvent}) event dispatch, with optional return
 * semantics via the {@code supply} variants.</p>
 *
 * <p>All dispatch methods resolve the runtime event class via {@link Object#getClass()}
 * and delegate to the appropriate dispatcher on {@link HytaleServer#getEventBus()}.</p>
 */
@UtilityClass
public class UtilEvent {

    /**
     * Dispatches a synchronous event through the event bus.
     *
     * <p>The event is dispatched using {@code dispatchFor(Class).dispatch(Event)},
     * which invokes all registered listeners synchronously on the calling thread.</p>
     *
     * @param event     the event instance to dispatch
     * @param <KeyType> the key type of the event
     * @param <Event>   the event type, extending {@link IEvent}
     * @throws IllegalArgumentException if {@code event} is {@code null}
     */
    @SuppressWarnings("unchecked")
    public static <KeyType, Event extends IEvent<KeyType>> void dispatch(final Event event) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null.");
        }

        // Resolve the runtime class for the event bus lookup
        final Class<Event> eventClass = (Class<Event>) event.getClass();

        HytaleServer.get().getEventBus().dispatchFor(eventClass).dispatch(event);
    }

    /**
     * Dispatches an asynchronous event through the event bus.
     *
     * <p>The event is dispatched using {@code dispatchForAsync(Class).dispatch(Event)},
     * which invokes all registered async listeners off the calling thread.</p>
     *
     * @param event     the async event instance to dispatch
     * @param <KeyType> the key type of the event
     * @param <Event>   the event type, extending {@link IAsyncEvent}
     * @throws IllegalArgumentException if {@code event} is {@code null}
     */
    @SuppressWarnings("unchecked")
    public static <KeyType, Event extends IAsyncEvent<KeyType>> void dispatchAsynchronous(final Event event) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null.");
        }

        // Resolve the runtime class for the async event bus lookup
        final Class<Event> eventClass = (Class<Event>) event.getClass();

        HytaleServer.get().getEventBus().dispatchForAsync(eventClass).dispatch(event);
    }

    /**
     * Dispatches a synchronous event and returns the event after all listeners have processed it.
     *
     * <p>This is useful when callers need to inspect the event state post-dispatch,
     * such as checking cancellation status on a {@link io.github.trae.hytale.framework.event.types.CustomCancellableEvent}.</p>
     *
     * @param event     the event instance to dispatch
     * @param <KeyType> the key type of the event
     * @param <Event>   the event type, extending {@link IEvent}
     * @return the same event instance, after synchronous dispatch has completed
     * @throws IllegalArgumentException if {@code event} is {@code null}
     */
    @SuppressWarnings("unchecked")
    public static <KeyType, Event extends IEvent<KeyType>> Event supply(final Event event) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null.");
        }

        // Resolve the runtime class for the event bus lookup
        final Class<Event> eventClass = (Class<Event>) event.getClass();

        return HytaleServer.get().getEventBus().dispatchFor(eventClass).dispatch(event);
    }

    /**
     * Dispatches an asynchronous event and returns a {@link CompletableFuture} that
     * completes with the event once all async listeners have processed it.
     *
     * <p>This is useful when callers need to chain further async operations on the
     * event result, or await completion of async handlers.</p>
     *
     * @param event     the async event instance to dispatch
     * @param <KeyType> the key type of the event
     * @param <Event>   the event type, extending {@link IAsyncEvent}
     * @return a {@link CompletableFuture} that resolves to the dispatched event
     * @throws IllegalArgumentException if {@code event} is {@code null}
     */
    @SuppressWarnings("unchecked")
    public static <KeyType, Event extends IAsyncEvent<KeyType>> CompletableFuture<Event> supplyAsynchronous(final Event event) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null.");
        }

        // Resolve the runtime class for the async event bus lookup
        final Class<Event> eventClass = (Class<Event>) event.getClass();

        return HytaleServer.get().getEventBus().dispatchForAsync(eventClass).dispatch(event);
    }
}