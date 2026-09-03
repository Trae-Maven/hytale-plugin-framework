package io.github.trae.hytale.framework.utility.search.types;

import com.hypixel.hytale.server.core.universe.PlayerRef;
import io.github.trae.hytale.framework.utility.UtilColor;
import io.github.trae.hytale.framework.utility.UtilServer;
import io.github.trae.hytale.framework.utility.enums.ChatColor;
import io.github.trae.hytale.framework.utility.search.HytaleSearchEngine;

import java.util.Locale;

/**
 * Search engine resolving currently online players.
 *
 * <p>Candidates are read live from {@link UtilServer#getOnlinePlayers()} on every search, and matched
 * on username: case-insensitive equality for an exact hit, case-insensitive substring for a partial
 * one.</p>
 */
public class OnlinePlayerSearchEngine extends HytaleSearchEngine<PlayerRef> {

    /**
     * Creates a search engine over the online player set.
     */
    public OnlinePlayerSearchEngine() {
        super("Online Player Search", UtilServer::getOnlinePlayers);
    }

    /**
     * {@inheritDoc}
     *
     * @return the player's username serialized in yellow
     */
    @Override
    protected String getTypeFormat(final PlayerRef playerRef) {
        return UtilColor.serialize(ChatColor.YELLOW.getColor(), playerRef.getUsername());
    }

    /**
     * {@inheritDoc}
     *
     * <p>Compares the player's username to the input, ignoring case.
     */
    @Override
    protected boolean isExact(final PlayerRef playerRef, final String result) {
        return playerRef.getUsername().equalsIgnoreCase(result);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Tests whether the player's username contains the input, ignoring case.
     */
    @Override
    protected boolean isMatching(final PlayerRef playerRef, final String result) {
        return playerRef.getUsername().toLowerCase(Locale.ROOT).contains(result.toLowerCase(Locale.ROOT));
    }
}