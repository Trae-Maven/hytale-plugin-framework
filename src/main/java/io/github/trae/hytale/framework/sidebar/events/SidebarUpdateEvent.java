package io.github.trae.hytale.framework.sidebar.events;

import com.hypixel.hytale.server.core.universe.PlayerRef;
import io.github.trae.hytale.framework.event.types.CustomEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Dispatched when a player's sidebar needs to be refreshed.
 *
 * <p>This event acts as a signal — it does not carry sidebar data. The
 * {@link io.github.trae.hytale.framework.sidebar.AbstractSidebarManager}
 * handles this event and re-dispatches a {@link SidebarCreateEvent} for
 * the same player, allowing plugin listeners to rebuild the sidebar
 * content with fresh data. The manager then diffs the new content against
 * the previously stored sidebar and only sends the values that have
 * changed.</p>
 *
 * <p>Example — triggering a sidebar refresh after a state change:</p>
 * <pre>{@code
 * UtilEvent.dispatch(new SidebarUpdateEvent(playerRef));
 * }</pre>
 */
@AllArgsConstructor
@Getter
public class SidebarUpdateEvent extends CustomEvent {

    /**
     * The player whose sidebar should be refreshed.
     */
    private final PlayerRef playerRef;
}
