package io.github.trae.hytale.framework.sidebar.events;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import io.github.trae.hytale.framework.event.types.CustomCancellableEvent;
import io.github.trae.hytale.framework.sidebar.constants.SidebarConstants;
import io.github.trae.hytale.framework.sidebar.events.interfaces.ISidebarCreateEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Dispatched when a sidebar needs to be created or rebuilt for a player.
 *
 * <p>Plugin listeners handle this event to populate the sidebar content by
 * setting the {@link #title} and {@link #lines}. The
 * {@link io.github.trae.hytale.framework.sidebar.AbstractSidebarManager}
 * finalises the event — if the title or lines are {@code null} or empty,
 * or the event is cancelled, the player's sidebar is removed. Otherwise,
 * a new {@link io.github.trae.hytale.framework.sidebar.Sidebar} is
 * constructed and sent to the player.</p>
 *
 * <p>This event is also re-dispatched by the manager when a
 * {@link SidebarUpdateEvent} is fired, allowing the same listener to
 * handle both initial creation and subsequent updates.</p>
 *
 * <p>Example:</p>
 * <pre>{@code
 * @EventHandler
 * public void onSidebarCreate(final SidebarCreateEvent event) {
 *     if (event.isCancelled()) {
 *         return;
 *     }
 *
 *     if (event.isCreated()) {
 *         return;
 *     }
 *
 *     if (!event.isIdentifier("FACTIONS")) {
 *         return;
 *     }
 *
 *     event.setTitle(Message.raw("My Server").bold(true));
 *     event.setLines(List.of(
 *         Message.raw("Welcome!"),
 *         Message.empty(),
 *         Message.raw("Online: " + getOnlineCount())
 *     ));
 * }
 * }</pre>
 */
@RequiredArgsConstructor
@Getter
@Setter
public class SidebarCreateEvent extends CustomCancellableEvent implements ISidebarCreateEvent {

    /**
     * The identifier of the sidebar source.
     *
     * <p>Used to distinguish which plugin or module is creating the
     * sidebar, allowing listeners to filter or respond based on the
     * source.</p>
     */
    private final String identifier;

    /**
     * The player the sidebar is being created for.
     */
    private final PlayerRef playerRef;

    /**
     * The sidebar title. Set by the plugin listener.
     */
    private Message title;

    /**
     * The sidebar lines. Set by the plugin listener.
     */
    private List<Message> lines;

    @Override
    public boolean isIdentifier(final String identifier) {
        return this.getIdentifier().equals(SidebarConstants.DEFAULT_IDENTIFIER) || this.getIdentifier().equals(identifier);
    }

    /**
     * Returns whether the sidebar content has been fully populated by a listener.
     *
     * <p>A sidebar is considered created when both the {@link #title} and
     * {@link #lines} are non-null and the lines list is not empty.</p>
     *
     * @return {@code true} if title and lines are set, {@code false} otherwise
     */
    @Override
    public boolean isCreated() {
        return this.getTitle() != null && this.getLines() != null && !(this.getLines().isEmpty());
    }
}
