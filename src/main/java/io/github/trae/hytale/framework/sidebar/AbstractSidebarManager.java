package io.github.trae.hytale.framework.sidebar;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.trae.hf.Manager;
import io.github.trae.hytale.framework.HytalePlugin;
import io.github.trae.hytale.framework.sidebar.events.SidebarCreateEvent;
import io.github.trae.hytale.framework.sidebar.events.SidebarUpdateEvent;
import io.github.trae.hytale.framework.sidebar.interfaces.IAbstractSidebarManager;
import io.github.trae.hytale.framework.utility.UtilEvent;
import lombok.Getter;
import lombok.Setter;

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
 * sidebar, the full {@code sidebar.ui} layout is loaded and all values are set. On
 * subsequent updates, only changed title and line values are sent to the client,
 * minimising bandwidth and preventing visual flicker.</p>
 *
 * <p>Each line in the {@code sidebar.ui} layout is wrapped in a row group
 * ({@code #lineNRow}) with {@code Visible: false} by default. Active lines have
 * their row group made visible and text set via {@code .TextSpans}. Inactive lines
 * remain hidden, allowing the sidebar to auto-size to its content.</p>
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
     * The maximum number of line rows available in the {@code sidebar.ui} layout.
     *
     * <p>Corresponds to the number of {@code #lineNRow} groups defined in the
     * {@code .ui} file. Defaults to {@code 16}.</p>
     */
    @Getter
    @Setter
    public static int maxLines = 16;

    /**
     * Per-player sidebar state, keyed by player UUID.
     */
    private final ConcurrentHashMap<UUID, Sidebar> sidebarMap = new ConcurrentHashMap<>();

    /**
     * Updates or creates the sidebar for the given player.
     *
     * <p>If no previous sidebar exists for the player, or if the line count
     * has changed, a new {@link CustomUIHud} is built with the full
     * {@code sidebar.ui} layout. Otherwise, only the title and lines that
     * differ from the previous state are sent as incremental updates.</p>
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

        if (previousSidebar == null || previousSidebar.getLines().size() != sidebar.getLines().size()) {
            this.createSidebar(playerRef, sidebar);
        } else {
            this.updateSidebar(playerRef, previousSidebar, sidebar);
        }
    }

    /**
     * Removes the sidebar for the given player.
     *
     * <p>Clears the stored sidebar state. The HUD is not explicitly removed
     * from the client — it will be replaced on the next sidebar creation or
     * cleared on disconnect.</p>
     *
     * @param playerRef the player to remove the sidebar from
     */
    @Override
    public void remove(final PlayerRef playerRef) {
        if (playerRef == null) {
            return;
        }

        this.sidebarMap.remove(playerRef.getUuid());
    }

    /**
     * Returns whether the given player has an active sidebar.
     *
     * @param playerRef the player to check
     * @return {@code true} if the player has a stored sidebar
     */
    @Override
    public boolean hasSidebar(final PlayerRef playerRef) {
        return this.sidebarMap.containsKey(playerRef.getUuid());
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
     * <p>Creates an anonymous {@link CustomUIHud} that loads the {@code sidebar.ui}
     * layout, sets the title via {@code #sidebarTitle.TextSpans}, and iterates all
     * line rows — making active rows visible with their text set, and keeping
     * inactive rows hidden.</p>
     *
     * @param playerRef the player to send the sidebar to
     * @param sidebar   the sidebar content
     */
    private void createSidebar(final PlayerRef playerRef, final Sidebar sidebar) {
        final CustomUIHud customHud = new CustomUIHud(playerRef) {
            @Override
            protected void build(@Nonnull final UICommandBuilder uiCommandBuilder) {
                uiCommandBuilder.append("sidebar.ui");

                uiCommandBuilder.set("#sidebarTitle.TextSpans", sidebar.getTitle());

                final List<Message> lines = sidebar.getLines();

                for (int index = 0; index < maxLines; index++) {
                    if (index < lines.size()) {
                        uiCommandBuilder.set("#line%sRow.Visible".formatted(index), true);
                        uiCommandBuilder.set("#line%s.TextSpans".formatted(index), lines.get(index));
                    } else {
                        uiCommandBuilder.set("#line%sRow.Visible".formatted(index), false);
                    }
                }
            }
        };

        this.execute(playerRef, player -> {
            player.getHudManager().setCustomHud(playerRef, customHud);

            this.sidebarMap.put(playerRef.getUuid(), sidebar);
        });
    }

    /**
     * Diffs the new sidebar against the previous one and sends only changed values.
     *
     * <p>Compares the title and each line up to the maximum of the previous and
     * current line counts. Row visibility is toggled when lines are added or
     * removed. Only lines whose text content has changed produce
     * {@code .TextSpans} update commands. If nothing has changed, no update
     * is sent.</p>
     *
     * @param playerRef       the player to update the sidebar for
     * @param previousSidebar the previously rendered sidebar
     * @param sidebar         the new sidebar content
     */
    private void updateSidebar(final PlayerRef playerRef, final Sidebar previousSidebar, final Sidebar sidebar) {
        final UICommandBuilder uiCommandBuilder = new UICommandBuilder();

        boolean dirty = false;

        if (!(sidebar.getTitle().equals(previousSidebar.getTitle()))) {
            uiCommandBuilder.set("#sidebarTitle.TextSpans", sidebar.getTitle());
            dirty = true;
        }

        final List<Message> previousLines = previousSidebar.getLines();
        final List<Message> currentLines = sidebar.getLines();

        final int max = Math.max(previousLines.size(), currentLines.size());

        for (int index = 0; index < max; index++) {
            final boolean wasActive = index < previousLines.size();
            final boolean isActive = index < currentLines.size();

            if (wasActive != isActive) {
                uiCommandBuilder.set("#line%sRow.Visible".formatted(index), isActive);
                dirty = true;
            }

            if (isActive) {
                final Message current = currentLines.get(index);
                final Message previous = wasActive ? previousLines.get(index) : Message.empty();

                if (!(current.equals(previous))) {
                    uiCommandBuilder.set("#line%s.TextSpans".formatted(index), current);
                    dirty = true;
                }
            }
        }

        if (dirty) {
            this.execute(playerRef, player -> {
                final CustomUIHud customHud = player.getHudManager().getCustomHud();
                if (customHud != null) {
                    customHud.update(false, uiCommandBuilder);
                    this.sidebarMap.put(playerRef.getUuid(), sidebar);
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
        final Ref<EntityStore> playerReference = playerRef.getReference();
        if (playerReference == null || !(playerReference.isValid())) {
            return;
        }

        final World world = playerReference.getStore().getExternalData().getWorld();

        world.execute(() -> {
            final Player player = playerReference.getStore().getComponent(playerReference, Player.getComponentType());
            if (player != null) {
                consumer.accept(player);
            }
        });
    }
}