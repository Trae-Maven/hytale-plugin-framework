package io.github.trae.hytale.framework.helper;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EcsEvent;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.component.system.ISystem;
import com.hypixel.hytale.component.system.RefChangeSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.function.consumer.BooleanConsumer;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.trae.hytale.framework.HytalePlugin;
import io.github.trae.hytale.framework.helper.abstracts.AbstractHelper;
import io.github.trae.hytale.framework.system.SystemListener;
import io.github.trae.hytale.framework.system.annotations.EventSystemHandler;
import io.github.trae.hytale.framework.system.annotations.RefChangeHandler;
import io.github.trae.hytale.framework.system.annotations.TickSystemHandler;
import io.github.trae.hytale.framework.system.data.EventSystemContext;
import io.github.trae.hytale.framework.system.data.RefChangeSystemContext;
import io.github.trae.hytale.framework.system.data.TickingSystemContext;
import io.github.trae.hytale.framework.system.enums.RefChangeType;
import io.github.trae.hytale.framework.system.extension.SystemExtension;
import io.github.trae.hytale.framework.system.extension.SystemExtensionImpl;
import io.github.trae.utilities.UtilMethod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Helper responsible for managing annotation-driven ECS system registrations.
 *
 * <p>Scans {@link SystemListener} instances for methods annotated with
 * {@link EventSystemHandler}, {@link TickSystemHandler}, and {@link RefChangeHandler},
 * generates anonymous ECS system implementations at runtime, and registers them
 * directly with the appropriate entity or chunk store registry.</p>
 *
 * <p>Each annotated method receives a single context parameter
 * ({@link EventSystemContext}, {@link TickingSystemContext}, or {@link RefChangeSystemContext})
 * that encapsulates all ECS data for the handler.</p>
 *
 * <p>System ordering is supported via {@link SystemExtensionImpl}, which allows
 * listeners to provide {@link SystemExtension} instances keyed by method name
 * containing {@link com.hypixel.hytale.component.SystemGroup} and
 * {@link Dependency} configurations.</p>
 *
 * <p>This mirrors the pattern established by {@link EventHelper} for standard
 * event handlers, but targets the ECS system layer.</p>
 *
 * @see SystemListener
 * @see SystemExtensionImpl
 * @see EventHelper
 */
public class SystemHelper extends AbstractHelper<SystemListener> {

    /**
     * Map of system listeners to their unregistration callbacks.
     */
    private final LinkedHashMap<SystemListener, List<BooleanConsumer>> REGISTRATIONS = new LinkedHashMap<>();

    /**
     * Creates a new {@link SystemHelper} bound to the given plugin.
     *
     * @param plugin the owning plugin instance
     */
    public SystemHelper(final HytalePlugin plugin) {
        super(plugin);
    }

    /**
     * Registers a system listener by scanning its public methods for system handler annotations.
     *
     * <p>For each annotated method with a valid signature, an anonymous ECS system is generated
     * and registered with the appropriate store registry. {@link RefChangeHandler} methods
     * targeting the same component type are grouped into a single
     * {@link RefChangeSystem}.</p>
     *
     * @param systemListener the system listener instance to register
     */
    @Override
    public void register(final SystemListener systemListener) {
        if (this.REGISTRATIONS.containsKey(systemListener)) {
            return;
        }

        final List<BooleanConsumer> unregistrationList = new ArrayList<>();

        final Map<Class<?>, Map<RefChangeType, Method>> refChangeGroups = new LinkedHashMap<>();
        final Map<Class<?>, RefChangeHandler> refChangeAnnotations = new LinkedHashMap<>();
        final Map<Class<?>, Method> refChangeSampleMethods = new LinkedHashMap<>();

        for (final Method method : systemListener.getClass().getMethods()) {
            if (method.isAnnotationPresent(EventSystemHandler.class)) {
                final BooleanConsumer unregistrationBooleanConsumer = this.registerEventSystem(systemListener, method);
                if (unregistrationBooleanConsumer != null) {
                    unregistrationList.add(unregistrationBooleanConsumer);
                }
            }

            if (method.isAnnotationPresent(TickSystemHandler.class)) {
                final BooleanConsumer unregistrationBooleanConsumer = this.registerTickingSystem(systemListener, method);
                if (unregistrationBooleanConsumer != null) {
                    unregistrationList.add(unregistrationBooleanConsumer);
                }
            }

            if (method.isAnnotationPresent(RefChangeHandler.class)) {
                final RefChangeHandler refChangeAnnotation = method.getAnnotation(RefChangeHandler.class);
                final Class<?> componentClass = this.resolveRefChangeComponentClass(method);
                if (componentClass != null) {
                    refChangeGroups.computeIfAbsent(componentClass, __ -> new EnumMap<>(RefChangeType.class)).put(refChangeAnnotation.type(), method);
                    refChangeAnnotations.putIfAbsent(componentClass, refChangeAnnotation);
                    refChangeSampleMethods.putIfAbsent(componentClass, method);
                }
            }
        }

        for (final Map.Entry<Class<?>, Map<RefChangeType, Method>> entry : refChangeGroups.entrySet()) {
            final RefChangeHandler refChangeAnnotation = refChangeAnnotations.get(entry.getKey());
            final Method sampleMethod = refChangeSampleMethods.get(entry.getKey());

            final BooleanConsumer unregistrationBooleanConsumer = this.registerRefChangeSystem(
                    systemListener,
                    entry.getKey(),
                    entry.getValue(),
                    refChangeAnnotation.query(),
                    sampleMethod
            );

            if (unregistrationBooleanConsumer != null) {
                unregistrationList.add(unregistrationBooleanConsumer);
            }
        }

        this.REGISTRATIONS.put(systemListener, unregistrationList);
    }

    /**
     * Unregisters a system listener and all of its generated systems.
     *
     * @param systemListener the system listener instance to unregister
     */
    @Override
    public void unregister(final SystemListener systemListener) {
        final List<BooleanConsumer> unregistrationList = this.REGISTRATIONS.remove(systemListener);
        if (unregistrationList == null) {
            return;
        }

        for (final BooleanConsumer unregistrationBooleanConsumer : unregistrationList) {
            unregistrationBooleanConsumer.accept(false);
        }
    }

    /**
     * Creates and registers an {@link EntityEventSystem} for an
     * {@link EventSystemHandler}-annotated method.
     *
     * <p>The method must have exactly one parameter of type {@link EventSystemContext}.
     * The event class is resolved from the last generic type argument of the context parameter.
     * The store class is resolved from the first generic type argument.</p>
     *
     * @param systemListener the listener instance that owns the method
     * @param method         the annotated handler method
     * @return the unregistration callback, or {@code null} if the method signature is invalid
     */
    @Nullable
    private BooleanConsumer registerEventSystem(final SystemListener systemListener, final Method method) {
        if (method.getParameterCount() != 1) {
            return null;
        }

        if (!(EventSystemContext.class.isAssignableFrom(method.getParameterTypes()[0]))) {
            return null;
        }

        final Class<? extends EcsEvent> eventClass = this.resolveEventClass(method);
        if (eventClass == null) {
            return null;
        }

        final EventSystemHandler eventSystemAnnotation = method.getAnnotation(EventSystemHandler.class);
        final SystemExtension<?> systemExtension = this.resolveExtension(systemListener, method);

        final Class<?> storeClass = this.resolveStoreClass(method);
        if (storeClass == null) {
            return null;
        }

        final EntityEventSystem<?, ?> entityEventSystem = this.createEntityEventSystem(
                systemListener,
                method,
                eventClass,
                this.resolveQuery(eventSystemAnnotation.query()),
                systemExtension
        );

        return this.registerSystem(entityEventSystem, storeClass);
    }

    /**
     * Creates and registers an {@link EntityTickingSystem} for a
     * {@link TickSystemHandler}-annotated method.
     *
     * <p>The method must have exactly one parameter of type {@link TickingSystemContext}.
     * The store class is resolved from the first generic type argument.</p>
     *
     * @param systemListener the listener instance that owns the method
     * @param method         the annotated handler method
     * @return the unregistration callback, or {@code null} if the method signature is invalid
     */
    @Nullable
    private BooleanConsumer registerTickingSystem(final SystemListener systemListener, final Method method) {
        if (method.getParameterCount() != 1) {
            return null;
        }

        if (!(TickingSystemContext.class.isAssignableFrom(method.getParameterTypes()[0]))) {
            return null;
        }

        final TickSystemHandler tickSystemAnnotation = method.getAnnotation(TickSystemHandler.class);
        final SystemExtension<?> systemExtension = this.resolveExtension(systemListener, method);

        final Class<?> storeClass = this.resolveStoreClass(method);
        if (storeClass == null) {
            return null;
        }

        final EntityTickingSystem<?> entityTickingSystem = this.createEntityTickingSystem(
                systemListener,
                method,
                this.resolveQuery(tickSystemAnnotation.query()),
                systemExtension
        );

        return this.registerSystem(entityTickingSystem, storeClass);
    }

    /**
     * Creates and registers a {@link RefChangeSystem} for a group of
     * {@link RefChangeHandler}-annotated methods targeting the same component type.
     *
     * <p>Each method must have exactly one parameter of type {@link RefChangeSystemContext}.
     * Methods with the same component type generic argument are grouped into a single system,
     * with each method wired to its corresponding {@link RefChangeType} callback.</p>
     *
     * @param systemListener   the listener instance that owns the methods
     * @param componentClass   the component class the system watches
     * @param refChangeMethods the grouped handler methods by {@link RefChangeType}
     * @param queryClasses     the query classes from the annotation
     * @param sampleMethod     a sample method used to resolve the store class
     * @return the unregistration callback, or {@code null} if the store class cannot be resolved
     */
    @Nullable
    private BooleanConsumer registerRefChangeSystem(final SystemListener systemListener, final Class<?> componentClass, final Map<RefChangeType, Method> refChangeMethods, final Class<?>[] queryClasses, final Method sampleMethod) {
        final SystemExtension<?> systemExtension = this.resolveExtension(systemListener, sampleMethod);

        final Class<?> storeClass = this.resolveStoreClass(sampleMethod);
        if (storeClass == null) {
            return null;
        }

        final RefChangeSystem<?, ?> refChangeSystem = this.createRefChangeSystem(
                systemListener,
                refChangeMethods.get(RefChangeType.ADDED),
                refChangeMethods.get(RefChangeType.SET),
                refChangeMethods.get(RefChangeType.REMOVED),
                this.resolveComponentType(componentClass),
                this.resolveQuery(queryClasses),
                systemExtension
        );

        return this.registerSystem(refChangeSystem, storeClass);
    }

    /**
     * Registers an {@link ISystem} with the appropriate store registry based on
     * the resolved store class and returns an unregistration callback.
     *
     * @param system     the system to register
     * @param storeClass the store class determining which registry to use
     * @return the unregistration callback
     * @throws IllegalArgumentException if the store class is not supported
     */
    @SuppressWarnings({"unchecked", "rawtypes", "removal"})
    private BooleanConsumer registerSystem(final ISystem<?> system, final Class<?> storeClass) {
        if (EntityStore.class.isAssignableFrom(storeClass)) {
            this.getPlugin().getEntityStoreRegistry().registerSystem((ISystem) system, true);

            return (shutdown) -> {
                if (!(shutdown)) {
                    final Class<?> systemClass = system.getClass();
                    if (EntityStore.REGISTRY.hasSystemClass((Class) systemClass)) {
                        EntityStore.REGISTRY.unregisterSystem((Class) systemClass);
                    }
                }
            };
        } else if (ChunkStore.class.isAssignableFrom(storeClass)) {
            this.getPlugin().getChunkStoreRegistry().registerSystem((ISystem) system, true);

            return (shutdown) -> {
                if (!(shutdown)) {
                    final Class<?> systemClass = system.getClass();
                    if (ChunkStore.REGISTRY.hasSystemClass((Class) systemClass)) {
                        ChunkStore.REGISTRY.unregisterSystem((Class) systemClass);
                    }
                }
            };
        }

        throw new IllegalArgumentException("Unsupported store class: " + storeClass.getName());
    }

    /**
     * Creates an {@link EntityEventSystem} with the given generic parameters.
     *
     * <p>The generated system delegates {@code handle} calls to the annotated method
     * via {@link UtilMethod#invoke(Method, Object, Object...)}, wrapping all ECS data
     * into an {@link EventSystemContext}.</p>
     *
     * @param systemListener  the listener instance that owns the method
     * @param method          the annotated handler method
     * @param eventClass      the ECS event class
     * @param query           the query for this system
     * @param systemExtension the system extension for group and dependencies, or {@code null}
     * @param <ECS_TYPE>      the ECS store type
     * @param <EventType>     the ECS event type
     * @return the generated entity event system
     */
    private <ECS_TYPE, EventType extends EcsEvent> EntityEventSystem<ECS_TYPE, EventType> createEntityEventSystem(final SystemListener systemListener, final Method method, final Class<EventType> eventClass, final Query<ECS_TYPE> query, @Nullable final SystemExtension<ECS_TYPE> systemExtension) {
        return new EntityEventSystem<>(eventClass) {

            @Nullable
            @Override
            public SystemGroup<ECS_TYPE> getGroup() {
                return systemExtension != null ? systemExtension.getGroup() : null;
            }

            @Nonnull
            @Override
            public Set<Dependency<ECS_TYPE>> getDependencies() {
                return systemExtension != null ? systemExtension.getDependencies() : Collections.emptySet();
            }

            @Nullable
            @Override
            public Query<ECS_TYPE> getQuery() {
                return query;
            }

            @Override
            public void handle(final int index, @Nonnull final ArchetypeChunk<ECS_TYPE> archetypeChunk, @Nonnull final Store<ECS_TYPE> store, @Nonnull final CommandBuffer<ECS_TYPE> commandBuffer, @Nonnull final EventType event) {
                final EventSystemContext<ECS_TYPE, EventType> eventSystemContext = new EventSystemContext<>(
                        event,
                        index,
                        archetypeChunk,
                        store,
                        commandBuffer
                );

                try {
                    UtilMethod.invoke(method, systemListener, eventSystemContext);
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    /**
     * Creates a {@link RefChangeSystem} with the given generic parameters, wiring
     * the added, set, and removed methods to their corresponding callbacks.
     *
     * <p>Callbacks for methods that are {@code null} are no-ops.</p>
     *
     * @param systemListener  the listener instance that owns the methods
     * @param addedMethod     the method for {@link RefChangeType#ADDED}, or {@code null}
     * @param setMethod       the method for {@link RefChangeType#SET}, or {@code null}
     * @param removedMethod   the method for {@link RefChangeType#REMOVED}, or {@code null}
     * @param componentType   the component type this system watches
     * @param query           the query for this system
     * @param systemExtension the system extension for group and dependencies, or {@code null}
     * @param <ECS_TYPE>      the ECS store type
     * @param <T>             the component type
     * @return the generated ref change system
     */
    private <ECS_TYPE, T extends Component<ECS_TYPE>> RefChangeSystem<ECS_TYPE, T> createRefChangeSystem(final SystemListener systemListener, @Nullable final Method addedMethod, @Nullable final Method setMethod, @Nullable final Method removedMethod, final ComponentType<ECS_TYPE, T> componentType, final Query<ECS_TYPE> query, @Nullable final SystemExtension<ECS_TYPE> systemExtension) {
        return new RefChangeSystem<>() {

            @Nullable
            @Override
            public SystemGroup<ECS_TYPE> getGroup() {
                return systemExtension != null ? systemExtension.getGroup() : null;
            }

            @Nonnull
            @Override
            public Set<Dependency<ECS_TYPE>> getDependencies() {
                return systemExtension != null ? systemExtension.getDependencies() : Collections.emptySet();
            }

            @Nonnull
            @Override
            public ComponentType<ECS_TYPE, T> componentType() {
                return componentType;
            }

            @Nullable
            @Override
            public Query<ECS_TYPE> getQuery() {
                return query;
            }

            @Override
            public void onComponentAdded(@Nonnull final Ref<ECS_TYPE> ref, @Nonnull final T component, @Nonnull final Store<ECS_TYPE> store, @Nonnull final CommandBuffer<ECS_TYPE> commandBuffer) {
                if (addedMethod == null) {
                    return;
                }

                final RefChangeSystemContext<ECS_TYPE, T> refChangeSystemContext = new RefChangeSystemContext<>(ref, component, store, commandBuffer);

                try {
                    UtilMethod.invoke(addedMethod, systemListener, refChangeSystemContext);
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onComponentSet(@Nonnull final Ref<ECS_TYPE> ref, @Nullable final T oldComponent, @Nonnull final T newComponent, @Nonnull final Store<ECS_TYPE> store, @Nonnull final CommandBuffer<ECS_TYPE> commandBuffer) {
                if (setMethod == null) {
                    return;
                }

                final RefChangeSystemContext<ECS_TYPE, T> refChangeSystemContext = new RefChangeSystemContext<>(ref, oldComponent, newComponent, store, commandBuffer);

                try {
                    UtilMethod.invoke(setMethod, systemListener, refChangeSystemContext);
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onComponentRemoved(@Nonnull final Ref<ECS_TYPE> ref, @Nonnull final T component, @Nonnull final Store<ECS_TYPE> store, @Nonnull final CommandBuffer<ECS_TYPE> commandBuffer) {
                if (removedMethod == null) {
                    return;
                }

                final RefChangeSystemContext<ECS_TYPE, T> refChangeSystemContext = new RefChangeSystemContext<>(ref, component, store, commandBuffer);

                try {
                    UtilMethod.invoke(removedMethod, systemListener, refChangeSystemContext);
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    /**
     * Creates an {@link EntityTickingSystem} with the given generic parameters.
     *
     * <p>The generated system delegates {@code tick} calls to the annotated method
     * via {@link UtilMethod#invoke(Method, Object, Object...)}, wrapping all ECS data
     * into a {@link TickingSystemContext}.</p>
     *
     * @param systemListener  the listener instance that owns the method
     * @param method          the annotated handler method
     * @param query           the query for this system
     * @param systemExtension the system extension for group and dependencies, or {@code null}
     * @param <ECS_TYPE>      the ECS store type
     * @return the generated entity ticking system
     */
    private <ECS_TYPE> EntityTickingSystem<ECS_TYPE> createEntityTickingSystem(final SystemListener systemListener, final Method method, final Query<ECS_TYPE> query, @Nullable final SystemExtension<ECS_TYPE> systemExtension) {
        return new EntityTickingSystem<>() {

            @Nullable
            @Override
            public SystemGroup<ECS_TYPE> getGroup() {
                return systemExtension != null ? systemExtension.getGroup() : null;
            }

            @Nonnull
            @Override
            public Set<Dependency<ECS_TYPE>> getDependencies() {
                return systemExtension != null ? systemExtension.getDependencies() : Collections.emptySet();
            }

            @Nullable
            @Override
            public Query<ECS_TYPE> getQuery() {
                return query;
            }

            @Override
            public void tick(final float deltaTime, final int index, @Nonnull final ArchetypeChunk<ECS_TYPE> archetypeChunk, @Nonnull final Store<ECS_TYPE> store, @Nonnull final CommandBuffer<ECS_TYPE> commandBuffer) {
                final TickingSystemContext<ECS_TYPE> tickingSystemContext = new TickingSystemContext<>(
                        deltaTime,
                        index,
                        archetypeChunk,
                        store,
                        commandBuffer
                );

                try {
                    UtilMethod.invoke(method, systemListener, tickingSystemContext);
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    /**
     * Resolves the ECS store class from the first generic type argument of the
     * method's context parameter.
     *
     * @param method the annotated method
     * @return the store class, or {@code null} if the generic type cannot be resolved
     */
    @Nullable
    private Class<?> resolveStoreClass(final Method method) {
        final Type genericType = method.getGenericParameterTypes()[0];

        if (!(genericType instanceof final ParameterizedType parameterizedType)) {
            return null;
        }

        final Type[] typeArguments = parameterizedType.getActualTypeArguments();
        if (typeArguments.length == 0) {
            return null;
        }

        final Type firstArgument = typeArguments[0];
        if (firstArgument instanceof final Class<?> storeClass) {
            return storeClass;
        }

        return null;
    }

    /**
     * Resolves a {@link SystemExtension} for the given method by looking up
     * the method name in the listener's {@link SystemExtensionImpl#getExtensions()} map.
     *
     * @param systemListener the listener instance
     * @param method         the annotated method
     * @param <ECS_TYPE>     the ECS store type
     * @return the system extension, or {@code null} if not found or not implemented
     */
    @Nullable
    @SuppressWarnings("unchecked")
    private <ECS_TYPE> SystemExtension<ECS_TYPE> resolveExtension(final SystemListener systemListener, final Method method) {
        if (!(systemListener instanceof final SystemExtensionImpl systemExtensionImpl)) {
            return null;
        }

        final Map<String, SystemExtension<?>> extensionMap = systemExtensionImpl.getExtensions();
        if (extensionMap == null) {
            return null;
        }

        return (SystemExtension<ECS_TYPE>) extensionMap.get(method.getName());
    }

    /**
     * Resolves the ECS event class from the last generic type argument of an
     * {@link EventSystemContext} method parameter.
     *
     * @param method the annotated method
     * @return the event class, or {@code null} if the generic type cannot be resolved
     */
    @Nullable
    @SuppressWarnings("unchecked")
    private Class<? extends EcsEvent> resolveEventClass(final Method method) {
        final Type genericType = method.getGenericParameterTypes()[0];

        if (!(genericType instanceof final ParameterizedType parameterizedType)) {
            return null;
        }

        final Type[] typeArguments = parameterizedType.getActualTypeArguments();
        if (typeArguments.length == 0) {
            return null;
        }

        final Type eventTypeArgument = typeArguments[typeArguments.length - 1];
        if (!(eventTypeArgument instanceof final Class<?> eventClass)) {
            return null;
        }

        if (!(EcsEvent.class.isAssignableFrom(eventClass))) {
            return null;
        }

        return (Class<? extends EcsEvent>) eventClass;
    }

    /**
     * Extracts the component class from a {@link RefChangeHandler}-annotated method's
     * last generic type argument on its {@link RefChangeSystemContext} parameter.
     *
     * @param method the annotated method
     * @return the component class, or {@code null} if the signature is invalid
     */
    @Nullable
    private Class<?> resolveRefChangeComponentClass(final Method method) {
        if (method.getParameterCount() != 1) {
            return null;
        }

        if (!(RefChangeSystemContext.class.isAssignableFrom(method.getParameterTypes()[0]))) {
            return null;
        }

        final Type genericType = method.getGenericParameterTypes()[0];

        if (!(genericType instanceof final ParameterizedType parameterizedType)) {
            return null;
        }

        final Type[] typeArguments = parameterizedType.getActualTypeArguments();
        if (typeArguments.length == 0) {
            return null;
        }

        final Type componentTypeArgument = typeArguments[typeArguments.length - 1];
        if (!(componentTypeArgument instanceof final Class<?> componentClass)) {
            return null;
        }

        return componentClass;
    }

    /**
     * Resolves a {@link Query} from an array of entity classes.
     *
     * <p>An empty array resolves to {@link Query#any()}. A single class resolves
     * to a direct component type query via its {@code getComponentType()} method.
     * Multiple classes are combined via {@link Query#and(Query[])}.</p>
     *
     * @param queryClasses the entity classes, or an empty array for no filter
     * @param <ECS_TYPE>   the ECS store type
     * @return the resolved query
     */
    @SuppressWarnings("unchecked")
    private <ECS_TYPE> Query<ECS_TYPE> resolveQuery(final Class<?>[] queryClasses) {
        if (queryClasses.length == 0) {
            return Query.any();
        }

        if (queryClasses.length == 1) {
            try {
                final Method getComponentTypeMethod = queryClasses[0].getMethod("getComponentType");
                return (Query<ECS_TYPE>) getComponentTypeMethod.invoke(null);
            } catch (final Exception e) {
                throw new RuntimeException("Failed to resolve query from class: " + queryClasses[0].getName(), e);
            }
        }

        final Query<ECS_TYPE>[] queries = new Query[queryClasses.length];
        for (int i = 0; i < queryClasses.length; i++) {
            try {
                final Method getComponentTypeMethod = queryClasses[i].getMethod("getComponentType");
                queries[i] = (Query<ECS_TYPE>) getComponentTypeMethod.invoke(null);
            } catch (final Exception e) {
                throw new RuntimeException("Failed to resolve query from class: " + queryClasses[i].getName(), e);
            }
        }

        return Query.and(queries);
    }

    /**
     * Resolves a {@link ComponentType} from a component class by reflectively invoking its
     * {@code getComponentType()} static method.
     *
     * @param componentClass the component class
     * @param <ECS_TYPE>     the ECS store type
     * @param <T>            the component type
     * @return the resolved component type
     */
    @SuppressWarnings("unchecked")
    private <ECS_TYPE, T extends Component<ECS_TYPE>> ComponentType<ECS_TYPE, T> resolveComponentType(final Class<?> componentClass) {
        try {
            final Method getComponentTypeMethod = componentClass.getMethod("getComponentType");
            return (ComponentType<ECS_TYPE, T>) getComponentTypeMethod.invoke(null);
        } catch (final Exception e) {
            throw new RuntimeException("Failed to resolve component type from class: " + componentClass.getName(), e);
        }
    }
}