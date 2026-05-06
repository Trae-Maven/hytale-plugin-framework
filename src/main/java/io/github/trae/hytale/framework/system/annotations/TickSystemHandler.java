package io.github.trae.hytale.framework.system.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as an ECS entity ticking system handler.
 *
 * <p>The annotated method must have exactly one parameter of type
 * {@link io.github.trae.hytale.framework.system.data.TickingSystemContext TickingSystemContext&lt;ECS_TYPE&gt;}.
 * The ECS store type is inferred from the generic type argument of the context parameter.</p>
 *
 * <p>The query is resolved by reflectively invoking {@code getComponentType()} on each
 * specified {@link #query()} class. An empty array (the default) results in
 * {@code Query.any()}, matching all entities. A single class resolves to a direct
 * component type query. Multiple classes are combined via {@code Query.and(...)}.</p>
 *
 * <p>The ECS store registry (entity or chunk) is automatically determined from the
 * generic type argument of the context parameter.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * // Single query
 * @TickSystemHandler(query = Player.class)
 * public void onTick(final TickingSystemContext<EntityStore> context) {
 *     final float dt = context.getDeltaTime();
 *     final Player player = context.getComponent(Player.getComponentType());
 * }
 *
 * // Compound query
 * @TickSystemHandler(query = {Player.class, Health.class})
 * public void onTick(final TickingSystemContext<EntityStore> context) {
 * }
 * }</pre>
 *
 * @see EventSystemHandler
 * @see RefChangeHandler
 * @see io.github.trae.hytale.framework.system.data.TickingSystemContext
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TickSystemHandler {

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