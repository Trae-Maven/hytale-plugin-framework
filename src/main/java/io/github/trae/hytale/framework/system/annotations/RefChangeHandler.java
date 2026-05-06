package io.github.trae.hytale.framework.system.annotations;

import io.github.trae.hytale.framework.system.enums.RefChangeType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as an ECS entity ref change system handler.
 *
 * <p>The annotated method must have exactly one parameter of type
 * {@link io.github.trae.hytale.framework.system.data.RefChangeSystemContext RefChangeSystemContext&lt;ECS_TYPE, T&gt;}.
 * The ECS store type and component type are inferred from the generic type arguments
 * of the context parameter.</p>
 *
 * <p>The {@link #type()} element determines which callback the method is wired to:</p>
 * <ul>
 *     <li>{@link RefChangeType#ADDED} — fired when a component is added to an entity</li>
 *     <li>{@link RefChangeType#SET} — fired when an existing component is replaced on an entity.
 *         The context provides both {@code getOldComponent()} and {@code getNewComponent()}.</li>
 *     <li>{@link RefChangeType#REMOVED} — fired when a component is removed from an entity</li>
 * </ul>
 *
 * <p>Methods with the same component type (inferred from the last generic type argument
 * of the context parameter) are grouped into a single
 * {@link com.hypixel.hytale.component.system.RefChangeSystem}.</p>
 *
 * <p>The query is resolved by reflectively invoking {@code getComponentType()} on each
 * specified {@link #query()} class. An empty array (the default) results in
 * {@code Query.any()}, matching all entities. A single class resolves to a direct
 * component type query. Multiple classes are combined via {@code Query.and(...)}.</p>
 *
 * <p>The ECS store registry (entity or chunk) is automatically determined from the first
 * generic type argument of the context parameter.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * @RefChangeHandler(type = RefChangeType.ADDED, query = Player.class)
 * public void onAdded(final RefChangeSystemContext<EntityStore, SomeComponent> context) {
 *     final SomeComponent component = context.getComponent();
 * }
 *
 * @RefChangeHandler(type = RefChangeType.SET, query = {Player.class, Health.class})
 * public void onSet(final RefChangeSystemContext<EntityStore, SomeComponent> context) {
 *     final SomeComponent oldComponent = context.getOldComponent();
 *     final SomeComponent newComponent = context.getNewComponent();
 * }
 *
 * @RefChangeHandler(type = RefChangeType.REMOVED, query = Player.class)
 * public void onRemoved(final RefChangeSystemContext<EntityStore, SomeComponent> context) {
 *     final SomeComponent component = context.getComponent();
 * }
 * }</pre>
 *
 * @see RefChangeType
 * @see EventSystemHandler
 * @see TickSystemHandler
 * @see io.github.trae.hytale.framework.system.data.RefChangeSystemContext
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RefChangeHandler {

    /**
     * The type of ref change this handler responds to.
     *
     * @return the ref change type
     */
    RefChangeType type();

    /**
     * The entity classes whose {@code getComponentType()} methods provide the
     * {@link com.hypixel.hytale.component.query.Query} for this system.
     *
     * <p>An empty array (the default) resolves to {@code Query.any()}.
     * A single class resolves to a direct component type query.
     * Multiple classes are combined via {@code Query.and(...)}.</p>
     *
     * @return the entity classes to query, or an empty array for no filter
     */
    Class<?>[] query() default {};
}