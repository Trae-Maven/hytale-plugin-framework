package io.github.trae.hytale.framework.system.data;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import io.github.trae.hytale.framework.system.data.abstracts.AbstractSystemContext;
import lombok.Getter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Context object for {@link io.github.trae.hytale.framework.system.annotations.RefChangeHandler}
 * methods, combining the ref change data into a single parameter.
 *
 * <p>Provides access to the entity reference, the component involved in the change,
 * the backing store, and the command buffer for deferred mutations.</p>
 *
 * <p>For {@link io.github.trae.hytale.framework.system.enums.RefChangeType#SET} handlers,
 * both {@link #getOldComponent()} and {@link #getNewComponent()} are populated.
 * For {@link io.github.trae.hytale.framework.system.enums.RefChangeType#ADDED} handlers,
 * only {@link #getComponent()} is populated (the added component).
 * For {@link io.github.trae.hytale.framework.system.enums.RefChangeType#REMOVED} handlers,
 * only {@link #getComponent()} is populated (the removed component).</p>
 *
 * @param <T> the component type
 */
@Getter
public class RefChangeSystemContext<ECS_TYPE, T extends Component<ECS_TYPE>> extends AbstractSystemContext<ECS_TYPE> {

    /**
     * The reference to the entity whose component changed.
     */
    @Nonnull
    private final Ref<ECS_TYPE> ref;

    /**
     * The component involved in the change.
     *
     * <p>For {@code ADDED}, this is the new component.
     * For {@code REMOVED}, this is the removed component.
     * For {@code SET}, this is the new component (same as {@link #getNewComponent()}).</p>
     */
    @Nonnull
    private final T component;

    /**
     * The old component value, only populated for {@code SET} changes.
     */
    @Nullable
    private final T oldComponent;

    /**
     * The new component value, only populated for {@code SET} changes.
     */
    @Nullable
    private final T newComponent;

    /**
     * Creates a context for {@code ADDED} or {@code REMOVED} ref changes.
     *
     * @param ref           the entity reference
     * @param component     the component that was added or removed
     * @param store         the backing entity store
     * @param commandBuffer the command buffer for deferred mutations
     */
    public RefChangeSystemContext(@Nonnull final Ref<ECS_TYPE> ref, @Nonnull final T component, @Nonnull final Store<ECS_TYPE> store, @Nonnull final CommandBuffer<ECS_TYPE> commandBuffer) {
        super(store, commandBuffer);

        this.ref = ref;
        this.component = component;
        this.oldComponent = null;
        this.newComponent = null;
    }

    /**
     * Creates a context for {@code SET} ref changes.
     *
     * @param ref           the entity reference
     * @param oldComponent  the previous component value
     * @param newComponent  the new component value
     * @param store         the backing entity store
     * @param commandBuffer the command buffer for deferred mutations
     */
    public RefChangeSystemContext(@Nonnull final Ref<ECS_TYPE> ref, @Nullable final T oldComponent, @Nonnull final T newComponent, @Nonnull final Store<ECS_TYPE> store, @Nonnull final CommandBuffer<ECS_TYPE> commandBuffer) {
        super(store, commandBuffer);

        this.ref = ref;
        this.component = newComponent;
        this.oldComponent = oldComponent;
        this.newComponent = newComponent;
    }
}