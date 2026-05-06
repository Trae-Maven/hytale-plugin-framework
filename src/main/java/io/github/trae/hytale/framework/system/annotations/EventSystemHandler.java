package io.github.trae.hytale.framework.system.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as an ECS entity event system handler.
 *
 * <p>The annotated method must have exactly one parameter of type
 * {@link io.github.trae.hytale.framework.system.data.EventSystemContext EventSystemContext&lt;ECS_TYPE, EventType&gt;}.
 * The ECS store type and event type are inferred from the generic type arguments of the
 * context parameter.</p>
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
 * // Single query
 * @EventSystemHandler(query = Player.class)
 * public void onBreakBlock(final EventSystemContext<EntityStore, BreakBlockEvent> context) {
 *     final BreakBlockEvent event = context.getEvent();
 *     final Player player = context.getComponent(Player.getComponentType());
 * }
 *
 * // Compound query
 * @EventSystemHandler(query = {Player.class, Health.class})
 * public void onDamage(final EventSystemContext<EntityStore, Damage> context) {
 * }
 * }</pre>
 *
 * @see TickSystemHandler
 * @see RefChangeHandler
 * @see io.github.trae.hytale.framework.system.data.EventSystemContext
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EventSystemHandler {

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