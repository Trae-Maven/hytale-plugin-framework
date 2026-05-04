package io.github.trae.hytale.framework.utility;

import com.hypixel.hytale.server.core.receiver.IMessageReceiver;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import io.github.trae.hytale.framework.utility.enums.ChatColor;
import lombok.experimental.UtilityClass;

import java.util.Locale;
import java.util.Optional;

/**
 * Utility class for world-related helper methods.
 */
@UtilityClass
public class UtilWorld {

    /**
     * Searches for a world by name with fuzzy matching.
     *
     * <p>First attempts an exact case-insensitive match on the world's
     * name. If no exact match is found, falls back to a case-insensitive
     * contains check. If {@code inform} is {@code true} and no match is
     * found or multiple matches are ambiguous, a formatted message is
     * sent to the receiver.</p>
     *
     * @param messageReceiver the receiver to send search feedback to
     * @param name            the world name or partial name to search for
     * @param inform          whether to send feedback messages on failure
     * @return an {@link Optional} containing the matched world, or empty
     */
    public static Optional<World> searchWorld(final IMessageReceiver messageReceiver, final String name, final boolean inform) {
        return UtilSearch.search(
                Universe.get().getWorlds().values(),
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
}