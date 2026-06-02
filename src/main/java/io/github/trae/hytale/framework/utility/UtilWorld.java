package io.github.trae.hytale.framework.utility;

import com.hypixel.hytale.server.core.receiver.IMessageReceiver;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import io.github.trae.hytale.framework.utility.enums.ChatColor;
import lombok.experimental.UtilityClass;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Utility class for world-related helper methods.
 */
@UtilityClass
public class UtilWorld {

    /**
     * Searches for a world by name with fuzzy matching.
     *
     * <p>First attempts an exact case-insensitive match on the world name.
     * If no exact match is found, falls back to a case-insensitive contains
     * check. An optional predicate can be used to filter candidates before
     * matching. If {@code inform} is {@code true} and no match is found or
     * multiple matches are ambiguous, a formatted message is sent to the
     * receiver.</p>
     *
     * @param messageReceiver the receiver to send search feedback to
     * @param name            the world name or partial name to search for
     * @param inform          whether to send feedback messages on failure
     * @param predicate       an optional predicate to filter candidates, or {@code null} for no filtering
     * @return an {@link Optional} containing the matched world, or empty
     * if zero or multiple matches were found
     */
    public static Optional<World> searchWorld(final IMessageReceiver messageReceiver, final String name, final boolean inform, final Predicate<World> predicate) {
        return UtilSearch.search(
                Universe.get().getWorlds().values(),
                predicate,
                world -> world.getName().equalsIgnoreCase(name),
                world -> world.getName().toLowerCase(Locale.ROOT).contains(name.toLowerCase(Locale.ROOT)),
                null,
                string -> UtilColor.serialize(ChatColor.YELLOW.getColor(), string),
                World::getName,
                "World Search",
                messageReceiver,
                name,
                inform
        );
    }

    /**
     * Searches for a world by name with fuzzy matching.
     *
     * <p>Convenience overload of {@link #searchWorld(IMessageReceiver, String, boolean, Predicate)}
     * with no predicate filtering applied.</p>
     *
     * @param messageReceiver the receiver to send search feedback to
     * @param name            the world name or partial name to search for
     * @param inform          whether to send feedback messages on failure
     * @return an {@link Optional} containing the matched world, or empty
     * if zero or multiple matches were found
     */
    public static Optional<World> searchWorld(final IMessageReceiver messageReceiver, final String name, final boolean inform) {
        return searchWorld(messageReceiver, name, inform, null);
    }

    /**
     * Resolves the world a player is currently in from their {@link PlayerRef}.
     *
     * <p>Retrieves the player's world UUID and looks up the corresponding
     * {@link World} instance from the {@link Universe}. Returns empty if
     * the player has no world UUID (e.g. not yet spawned) or the world
     * is not loaded.</p>
     *
     * @param playerRef the player reference to resolve the world for
     * @return an {@link Optional} containing the player's world, or empty
     */
    public static Optional<World> getWorldByPlayerRef(final PlayerRef playerRef) {
        final UUID worldUuid = playerRef.getWorldUuid();
        if (worldUuid == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(Universe.get().getWorld(worldUuid));
    }
}