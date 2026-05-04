package io.github.trae.hytale.framework.sidebar;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import io.github.trae.hf.Manager;
import io.github.trae.hytale.framework.HytalePlugin;
import io.github.trae.hytale.framework.sidebar.events.SidebarCreateEvent;
import io.github.trae.hytale.framework.sidebar.events.SidebarUpdateEvent;
import io.github.trae.hytale.framework.sidebar.interfaces.IAbstractSidebarManager;
import io.github.trae.hytale.framework.sidebar.settings.SidebarSettings;
import io.github.trae.hytale.framework.utility.UtilEvent;
import io.github.trae.hytale.framework.utility.UtilPlayer;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Manages per-player sidebar state and rendering via Hytale's {@link CustomUIHud} system.
 *
 * <p>This manager stores the most recently rendered {@link Sidebar} for each player
 * and uses it to diff against incoming updates. When a player receives their first
 * sidebar, the full inline UI markup is built and sent. On subsequent updates, only
 * changed title and line values are sent to the client, minimising bandwidth and
 * preventing visual flicker.</p>
 *
 * <p>Event handling is built directly into the manager via
 * {@link #onSidebarCreateEvent(SidebarCreateEvent)} and
 * {@link #onSidebarUpdateEvent(SidebarUpdateEvent)}. Plugin listeners populate
 * the {@link SidebarCreateEvent} with title and lines — this manager then
 * finalises the event by creating or updating the sidebar. A
 * {@link SidebarUpdateEvent} triggers a fresh {@link SidebarCreateEvent},
 * allowing plugin listeners to rebuild with current data.</p>
 *
 * <p>All HUD operations are executed on the player's world thread via
 * {@link World#execute(Runnable)}.</p>
 *
 * @param <Plugin> the plugin type extending {@link HytalePlugin}
 */
public class AbstractSidebarManager<Plugin extends HytalePlugin> implements Manager<Plugin>, IAbstractSidebarManager {

    /**
     * Per-player sidebar state, keyed by player UUID.
     */
    private final ConcurrentHashMap<UUID, Sidebar> sidebarMap = new ConcurrentHashMap<>();

    /**
     * Updates or creates the sidebar for the given player.
     *
     * <p>If no previous sidebar exists for the player, a new {@link CustomUIHud}
     * is built with the full inline markup and sent. If a previous sidebar exists,
     * only the title and lines that differ from the previous state are sent as
     * incremental updates.</p>
     *
     * @param playerRef the player to update the sidebar for
     * @param sidebar   the new sidebar content
     */
    @Override
    public void update(final PlayerRef playerRef, final Sidebar sidebar) {
        if (playerRef == null || sidebar == null) {
            return;
        }

        final Sidebar previousSidebar = this.sidebarMap.get(playerRef.getUuid());

        this.sidebarMap.put(playerRef.getUuid(), sidebar);

        if (previousSidebar == null) {
            this.createSidebar(playerRef, sidebar);
        } else {
            this.updateSidebar(playerRef, previousSidebar, sidebar);
        }
    }

    /**
     * Removes the sidebar for the given player.
     *
     * <p>Clears the stored sidebar state and sends an empty update to the
     * player's active HUD, clearing all visible content.</p>
     *
     * @param playerRef the player to remove the sidebar from
     */
    @Override
    public void remove(final PlayerRef playerRef) {
        if (playerRef == null) {
            return;
        }

        this.sidebarMap.remove(playerRef.getUuid());

        this.execute(playerRef, player -> {
            final CustomUIHud customHud = player.getHudManager().getCustomHud();
            if (customHud != null) {
                customHud.update(true, new UICommandBuilder());
            }
        });
    }

    /**
     * Handles a {@link SidebarCreateEvent} after all plugin listeners have
     * populated it.
     *
     * <p>If the event is cancelled, or if the title or lines are {@code null}
     * or empty, the player's sidebar is removed. Otherwise, a new {@link Sidebar}
     * is constructed from the event data and passed to {@link #update}.</p>
     *
     * @param event the sidebar create event
     */
    @Override
    public void onSidebarCreateEvent(final SidebarCreateEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!(event.isCreated())) {
            this.remove(event.getPlayerRef());
            return;
        }

        this.update(event.getPlayerRef(), new Sidebar(event.getTitle(), event.getLines()));
    }

    /**
     * Handles a {@link SidebarUpdateEvent} by dispatching a fresh
     * {@link SidebarCreateEvent} for the same player.
     *
     * <p>This allows plugin listeners to use a single {@link SidebarCreateEvent}
     * handler for both initial creation and subsequent refreshes — the same
     * listener populates the title and lines with current data each time.</p>
     *
     * @param event the sidebar update event
     */
    @Override
    public void onSidebarUpdateEvent(final SidebarUpdateEvent event) {
        UtilEvent.dispatch(new SidebarCreateEvent(event.getPlayerRef()));
    }

    /**
     * Builds and sends a full sidebar HUD to the player.
     *
     * <p>Creates an anonymous {@link CustomUIHud} that appends the inline markup
     * from {@link SidebarSettings#getMarkup()} and sets all title and line values.
     * Used when the player has no existing sidebar.</p>
     *
     * @param playerRef the player to send the sidebar to
     * @param sidebar   the sidebar content
     */
    private void createSidebar(final PlayerRef playerRef, final Sidebar sidebar) {
        final CustomUIHud hud = new CustomUIHud(playerRef) {
            @Override
            protected void build(@Nonnull final UICommandBuilder uiCommandBuilder) {
                uiCommandBuilder.appendInline("", SidebarSettings.getMarkup().get());

                uiCommandBuilder.set("#sidebar-title", sidebar.getTitle());

                final List<Message> lines = sidebar.getLines();

                for (int index = 0; index < SidebarSettings.getMaxLines(); index++) {
                    uiCommandBuilder.set("#line-%s".formatted(index), index < lines.size() ? lines.get(index) : Message.empty());
                }
            }
        };

        this.execute(playerRef, player -> player.getHudManager().setCustomHud(playerRef, hud));
    }

    /**
     * Diffs the new sidebar against the previous one and sends only changed values.
     *
     * <p>Compares the title and each line index up to {@link SidebarSettings#getMaxLines()}.
     * Lines beyond the current list size are treated as {@link Message#empty()}.
     * If no values have changed, no update is sent.</p>
     *
     * @param playerRef       the player to update the sidebar for
     * @param previousSidebar the previously rendered sidebar
     * @param sidebar         the new sidebar content
     */
    private void updateSidebar(final PlayerRef playerRef, final Sidebar previousSidebar, final Sidebar sidebar) {
        final UICommandBuilder uiCommandBuilder = new UICommandBuilder();

        boolean dirty = false;

        if (!(sidebar.getTitle().equals(previousSidebar.getTitle()))) {
            uiCommandBuilder.set("#sidebar-title", sidebar.getTitle());
            dirty = true;
        }

        final List<Message> previousLines = previousSidebar.getLines();
        final List<Message> currentLines = sidebar.getLines();

        for (int index = 0; index < SidebarSettings.getMaxLines(); index++) {
            final Message previous = index < previousLines.size() ? previousLines.get(index) : Message.empty();
            final Message current = index < currentLines.size() ? currentLines.get(index) : Message.empty();

            if (!(current.equals(previous))) {
                uiCommandBuilder.set("#line-%s".formatted(index), current);
                dirty = true;
            }
        }

        if (dirty) {
            this.execute(playerRef, player -> {
                final CustomUIHud customHud = player.getHudManager().getCustomHud();
                if (customHud != null) {
                    customHud.update(false, uiCommandBuilder);
                }
            });
        }
    }

    /**
     * Executes a player-dependent action on the player's world thread.
     *
     * <p>Resolves the {@link Player} from the {@link PlayerRef}, and if
     * the player is online and has a valid world, runs the consumer on
     * that world's thread via {@link World#execute(Runnable)}.</p>
     *
     * @param playerRef the player reference
     * @param consumer  the action to execute with the resolved player
     */
    private void execute(final PlayerRef playerRef, final Consumer<Player> consumer) {
        UtilPlayer.getPlayer(playerRef).ifPresent(player -> {
            final World world = player.getWorld();
            if (world != null) {
                world.execute(() -> consumer.accept(player));
            }
        });
    }
}