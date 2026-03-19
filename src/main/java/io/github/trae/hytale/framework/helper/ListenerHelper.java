package io.github.trae.hytale.framework.helper;

import com.hypixel.hytale.event.EventRegistration;
import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.event.IAsyncEvent;
import com.hypixel.hytale.event.IBaseEvent;
import io.github.trae.hytale.framework.HytalePlugin;
import io.github.trae.hytale.framework.event.Listener;
import io.github.trae.hytale.framework.event.annotations.EventHandler;
import io.github.trae.hytale.framework.helper.abstracts.AbstractHelper;
import io.github.trae.utilities.UtilMethod;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

/**
 * Helper responsible for managing event listener registrations within a {@link HytalePlugin}.
 *
 * <p>Scans {@link Listener} instances for methods annotated with {@link EventHandler},
 * resolves the event class from the method's single parameter, and registers them
 * with the plugin's {@link EventRegistry}. Supports both synchronous and asynchronous
 * event handlers.</p>
 *
 * <p>Each listener is tracked alongside its list of {@link EventRegistration} handles,
 * enabling clean bulk unregistration via {@link #unregister(Listener)}.</p>
 */
public class ListenerHelper extends AbstractHelper<Listener> {

    /**
     * Map of listeners to their associated event registration handles.
     */
    private final LinkedHashMap<Listener, List<EventRegistration<?, ?>>> REGISTRATIONS = new LinkedHashMap<>();

    /**
     * Creates a new {@link ListenerHelper} bound to the given plugin.
     *
     * @param plugin the owning plugin instance
     */
    public ListenerHelper(final HytalePlugin plugin) {
        super(plugin);
    }

    /**
     * Registers a listener by scanning its public methods for {@link EventHandler} annotations.
     *
     * <p>For each annotated method with a valid single-parameter event type, an
     * {@link EventRegistration} is created and stored. Methods that do not match
     * the expected signature (single parameter implementing {@link IBaseEvent})
     * are silently skipped.</p>
     *
     * @param listener the listener instance to register
     */
    @Override
    public void register(final Listener listener) {
        for (final Method method : listener.getClass().getMethods()) {
            if (!(method.isAnnotationPresent(EventHandler.class))) {
                continue;
            }

            // Resolve the event class from the method's single parameter
            this.getEventClassByMethod(method).ifPresent(eventClass -> {
                final EventRegistration<?, ?> eventRegistration = this.getEventRegistration(listener, method, eventClass);

                this.REGISTRATIONS.computeIfAbsent(listener, __ -> new ArrayList<>()).add(eventRegistration);
            });
        }
    }

    /**
     * Unregisters a listener and all of its associated event registrations.
     *
     * <p>Each {@link EventRegistration} is unregistered from the event bus.
     * If the listener was never registered, this call is a no-op.</p>
     *
     * @param listener the listener instance to unregister
     */
    @Override
    public void unregister(final Listener listener) {
        final List<EventRegistration<?, ?>> eventRegistrationList = this.REGISTRATIONS.remove(listener);
        if (eventRegistrationList == null) {
            return;
        }

        eventRegistrationList.forEach(EventRegistration::unregister);
    }

    /**
     * Extracts the event class from a method's parameter list.
     *
     * <p>Valid methods must have exactly one parameter that implements {@link IBaseEvent}.
     * Returns {@link Optional#empty()} if the method does not match this signature.</p>
     *
     * @param method the method to inspect
     * @return an {@link Optional} containing the event class, or empty if invalid
     */
    @SuppressWarnings("unchecked")
    private Optional<Class<IBaseEvent<?>>> getEventClassByMethod(final Method method) {
        if (method.getParameterCount() != 1) {
            return Optional.empty();
        }

        final Class<?> clazz = method.getParameterTypes()[0];

        if (!(IBaseEvent.class.isAssignableFrom(clazz))) {
            return Optional.empty();
        }

        return Optional.of((Class<IBaseEvent<?>>) clazz);
    }

    /**
     * Creates an {@link EventRegistration} for a listener method and event class.
     *
     * <p>Determines whether to use synchronous or asynchronous registration based
     * on whether the event class is assignable to {@link IAsyncEvent}. The priority
     * is read from the {@link EventHandler} annotation on the event class.</p>
     *
     * <p>Handler invocation is delegated to {@link UtilMethod#invoke(Method, Object, Object...)}
     * with exceptions wrapped in a {@link RuntimeException}.</p>
     *
     * @param listener   the listener instance that owns the method
     * @param method     the annotated handler method
     * @param eventClass the resolved event class
     * @return the resulting {@link EventRegistration}
     */
    private EventRegistration<?, ?> getEventRegistration(final Listener listener, final Method method, final Class<IBaseEvent<?>> eventClass) {
        final EventRegistry eventRegistry = this.getPlugin().getEventRegistry();

        final EventHandler annotation = eventClass.getAnnotation(EventHandler.class);

        // Async events are registered with thenApply chaining on the CompletableFuture
        if (eventClass.isAssignableFrom(IAsyncEvent.class)) {
            return eventRegistry.registerAsync((short) annotation.priority(), eventClass, completableFuture ->
                    completableFuture.thenApply(event -> {
                        try {
                            UtilMethod.invoke(method, listener, event);
                        } catch (final Exception e) {
                            throw new RuntimeException(e);
                        }
                        return event;
                    })
            );
        }

        // Synchronous events are registered with a direct consumer
        return eventRegistry.register((short) annotation.priority(), eventClass, event -> {
            try {
                UtilMethod.invoke(method, listener, event);
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}