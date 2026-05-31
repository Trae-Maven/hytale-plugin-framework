package io.github.trae.hytale.framework.event.annotations;

import io.github.trae.hytale.framework.event.constants.EventPriority;
import io.github.trae.hytale.framework.event.types.CustomEvent;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as an event handler.
 * The method must have exactly one parameter which extends {@link CustomEvent}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EventHandler {

    /**
     * Whether this handler should receive events dispatched under any key (global),
     * or only events dispatched without a key (keyed to {@link Void}).
     *
     * <p>Set to {@code true} for events that are keyed by a non-{@link Void} type
     * (e.g. {@link com.hypixel.hytale.server.core.event.events.player.AddPlayerToWorldEvent}
     * which is keyed by world name), so the handler receives all dispatches regardless
     * of key. When {@code false}, registration uses the standard keyless overload and
     * will only receive {@link Void}-keyed events.</p>
     *
     * <p>Defaults to {@code false}. Only set to {@code true} when the target event
     * implements {@link com.hypixel.hytale.event.IBaseEvent} with a non-{@link Void}
     * key type.</p>
     *
     * @return {@code true} to register globally, {@code false} for keyless registration
     */
    boolean global() default false;

    /**
     * The priority of this handler. Handlers with lower values execute first.
     *
     * <p>Defaults to {@link EventPriority#NORMAL}. Use constants from {@link EventPriority}
     * or any custom integer value for fine-grained ordering.</p>
     *
     * @return the priority value
     */
    int priority();

    /**
     * Whether this handler should skip cancelled events.
     *
     * <p>When set to {@code true}, this handler will not be invoked if a previous handler
     * has already cancelled the event. When set to {@code false}, this handler will always
     * be invoked regardless of the event's cancellation state.</p>
     *
     * @return {@code true} to skip cancelled events, {@code false} to receive all events
     */
    boolean ignoreCancelled() default false;
}