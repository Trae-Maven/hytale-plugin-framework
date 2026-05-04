package io.github.trae.hytale.framework.sidebar.constants;

/**
 * Constants used across the sidebar system.
 */
public class SidebarConstants {

    /**
     * The default sidebar identifier used for generic updates.
     *
     * <p>When a {@link io.github.trae.hytale.framework.sidebar.events.SidebarUpdateEvent}
     * is dispatched with this identifier, all sidebar create listeners
     * are eligible to respond. Listeners accept both their own identifier
     * and this default.</p>
     */
    public static final String DEFAULT_IDENTIFIER = "DEFAULT";
}