package io.github.trae.hytale.framework.system.enums;

/**
 * Defines the type of component ref change that a {@link io.github.trae.hytale.framework.system.annotations.RefChangeHandler} responds to.
 */
public enum RefChangeType {

    /**
     * Fired when a component is added to an entity.
     */
    ADDED,

    /**
     * Fired when an existing component is replaced on an entity.
     */
    SET,

    /**
     * Fired when a component is removed from an entity.
     */
    REMOVED
}