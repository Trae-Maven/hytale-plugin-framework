package io.github.trae.hytale.framework.utility;

import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import io.github.trae.hytale.framework.utility.search.types.OnlinePlayerSearchEngine;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Utility methods for querying the state of the server.
 *
 * <p>Covers lookups of connected players by user-supplied search input, plus filtered snapshots of
 * the online player set. Players are represented as {@link PlayerRef} session handles, so a player
 * between worlds is still included even though they hold no entity reference at that moment.</p>
 *
 * <p>Offline players are not covered here. Hytale persists them in a UUID-keyed asynchronous store
 * with no enumerable listing and no username index, so there is nothing to search without an index
 * the framework maintains itself.</p>
 */
@UtilityClass
public class UtilServer {

    /**
     * Search engine backing the {@code searchOnlinePlayer} helpers.
     */
    private static final OnlinePlayerSearchEngine ONLINE_PLAYER_SEARCH_ENGINE = new OnlinePlayerSearchEngine();

    /**
     * Returns the currently online players, optionally filtered by a predicate.
     *
     * @param predicate the filter to apply; players failing the test are excluded.
     *                  If {@code null}, all online players are returned.
     * @return a mutable {@link List} of matching online players
     */
    public static List<PlayerRef> getOnlinePlayers(final Predicate<PlayerRef> predicate) {
        final List<PlayerRef> playerList = new ArrayList<>(Universe.get().getPlayers());

        if (predicate != null) {
            playerList.removeIf(predicate.negate());
        }

        return playerList;
    }

    /**
     * Returns all currently online players.
     *
     * @return a mutable {@link List} of all online players
     */
    public static List<PlayerRef> getOnlinePlayers() {
        return getOnlinePlayers(null);
    }

    /**
     * Searches the online players for the player identified by the given input.
     *
     * <p>An exact username match wins immediately, otherwise a single partial match is returned. An
     * empty or ambiguous search yields an empty result and, when informing is enabled, messages the
     * sender with the outcome.</p>
     *
     * @param sender    the sender to inform of the search outcome
     * @param input     the search input
     * @param inform    whether to message the sender when the search fails to resolve
     * @param predicate an optional filter applied before matching, or null to consider every player
     * @return the resolved player, or {@link Optional#empty()} if the search was empty or ambiguous
     */
    public static Optional<PlayerRef> searchOnlinePlayer(final CommandSender sender, final String input, final boolean inform, final Predicate<PlayerRef> predicate) {
        return ONLINE_PLAYER_SEARCH_ENGINE.find(sender, input, inform, predicate);
    }

    /**
     * Searches the online players for the player identified by the given input, without filtering.
     *
     * @param sender the sender to inform of the search outcome
     * @param input  the search input
     * @param inform whether to message the sender when the search fails to resolve
     * @return the resolved player, or {@link Optional#empty()} if the search was empty or ambiguous
     * @see #searchOnlinePlayer(CommandSender, String, boolean, Predicate)
     */
    public static Optional<PlayerRef> searchOnlinePlayer(final CommandSender sender, final String input, final boolean inform) {
        return searchOnlinePlayer(sender, input, inform, null);
    }
}