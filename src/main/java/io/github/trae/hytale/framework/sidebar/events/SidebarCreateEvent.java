package io.github.trae.hytale.framework.sidebar.events;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import io.github.trae.hytale.framework.event.types.CustomCancellableEvent;
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
     * The player the sidebar is being created for.
     */
    private final PlayerRef playerRef;

    /**
     * An identifier for the sidebar source.
     *
     * <p>Allows plugin listeners to tag the sidebar with an identifier
     * so that other listeners or systems can determine which plugin
     * or module created it. Defaults to {@code "DEFAULT"}.</p>
     */
    private String id = "DEFAULT";

    /**
     * The sidebar title. Set by the plugin listener.
     */
    private Message title;

    /**
     * The sidebar lines. Set by the plugin listener.
     */
    private List<Message> lines;

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
