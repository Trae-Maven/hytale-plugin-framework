package io.github.trae.hytale.framework.utility;

import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import io.github.trae.hytale.framework.utility.search.types.WorldSearchEngine;
import lombok.experimental.UtilityClass;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Utility class for world-related helper methods.
 *
 * <p>Covers name-style searching over the loaded worlds through {@link WorldSearchEngine}, plus
 * resolving the world a connected player is currently in.</p>
 */
@UtilityClass
public class UtilWorld {

    /**
     * Search engine backing the {@code searchWorld} helpers.
     */
    private static final WorldSearchEngine WORLD_SEARCH_ENGINE = new WorldSearchEngine();

    /**
     * Searches the loaded worlds for the world identified by the given input.
     *
     * <p>An exact name match wins immediately, otherwise a single partial match is returned. An empty
     * or ambiguous search yields an empty result and, when informing is enabled, messages the sender
     * with the outcome.</p>
     *
     * @param sender    the sender to inform of the search outcome
     * @param name      the world name or partial name to search for
     * @param inform    whether to message the sender when the search fails to resolve
     * @param predicate an optional filter applied before matching, or null to consider every world
     * @return the resolved world, or {@link Optional#empty()} if the search was empty or ambiguous
     */
    public static Optional<World> searchWorld(final CommandSender sender, final String name, final boolean inform, final Predicate<World> predicate) {
        return WORLD_SEARCH_ENGINE.find(
                sender,
                name,
                inform,
                predicate
        );
    }

    /**
     * Searches the loaded worlds for the world identified by the given input, without filtering.
     *
     * @param sender the sender to inform of the search outcome
     * @param name   the world name or partial name to search for
     * @param inform whether to message the sender when the search fails to resolve
     * @return the resolved world, or {@link Optional#empty()} if the search was empty or ambiguous
     * @see #searchWorld(CommandSender, String, boolean, Predicate)
     */
    public static Optional<World> searchWorld(final CommandSender sender, final String name, final boolean inform) {
        return searchWorld(sender, name, inform, null);
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