package io.github.trae.hytale.framework.utility;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.receiver.IMessageReceiver;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.trae.hytale.framework.utility.enums.ChatColor;
import lombok.experimental.UtilityClass;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Utility class for player-related helper methods.
 */
@UtilityClass
public class UtilPlayer {

    /**
     * Resolves the {@link Player} component from the given {@link PlayerRef}.
     *
     * @param playerRef the player reference to resolve
     * @return an {@link Optional} containing the player component,
     * or empty if the reference is null or the player is not in a world
     */
    public static Optional<Player> getPlayer(final PlayerRef playerRef) {
        if (playerRef != null) {
            final Ref<EntityStore> playerReference = playerRef.getReference();
            if (playerReference != null) {
                final Player player = playerReference.getStore().getComponent(playerReference, Player.getComponentType());
                if (player != null) {
                    return Optional.of(player);
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Resolves the {@link PlayerRef} component from the given {@link Player}.
     *
     * @param player the player to resolve the reference for
     * @return an {@link Optional} containing the player reference,
     * or empty if the player is null or not in a world
     */
    public static Optional<PlayerRef> getPlayerRef(final Player player) {
        if (player != null) {
            final Ref<EntityStore> playerReference = player.getReference();
            final World playerWorld = player.getWorld();

            if (playerReference != null && playerWorld != null) {
                final PlayerRef playerRef = playerWorld.getEntityStore().getStore().getComponent(playerReference, PlayerRef.getComponentType());
                if (playerRef != null) {
                    return Optional.of(playerRef);
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Resolves the remote IP address of the given player by inspecting
     * the underlying channel connection.
     *
     * @param player the player to resolve the IP address for
     * @return an {@link Optional} containing the IP address string,
     *         or empty if the address cannot be resolved
     */
    public static Optional<String> getIpAddress(final PlayerRef player) {
        final SocketAddress socketAddress = player.getPacketHandler().getChannel().remoteAddress();

        if (!(socketAddress instanceof final InetSocketAddress inetSocketAddress)) {
            return Optional.empty();
        }

        return Optional.ofNullable(inetSocketAddress.getAddress().getHostAddress());
    }

    /**
     * Searches online players for a {@link PlayerRef} by username.
     *
     * <p>Delegates to {@link UtilSearch#search} with case-insensitive exact
     * and partial (contains) matching on {@link PlayerRef#getUsername()}.
     * An optional predicate can be used to filter candidates before matching.</p>
     *
     * @param messageReceiver the receiver for result or ambiguity messages
     * @param input           the username or partial username to search for
     * @param inform          whether to send a result message to the receiver
     * @param predicate       an optional predicate to filter candidates, or {@code null} for no filtering
     * @return an {@link Optional} containing the matched player reference,
     *         or empty if zero or multiple matches were found
     */
    public static Optional<PlayerRef> searchPlayerRef(final IMessageReceiver messageReceiver, final String input, final boolean inform, final Predicate<PlayerRef> predicate) {
        return UtilSearch.search(
                Universe.get().getPlayers(),
                predicate,
                playerRef -> playerRef.getUsername().equalsIgnoreCase(input),
                playerRef -> playerRef.getUsername().toLowerCase(Locale.ROOT).contains(input.toLowerCase(Locale.ROOT)),
                null,
                string -> UtilColor.serialize(ChatColor.YELLOW.getColor(), string),
                PlayerRef::getUsername,
                "Player Search",
                messageReceiver,
                input,
                inform
        );
    }

    /**
     * Searches online players for a {@link PlayerRef} by username.
     *
     * <p>Convenience overload of {@link #searchPlayerRef(IMessageReceiver, String, boolean, Predicate)}
     * with no predicate filtering applied.</p>
     *
     * @param messageReceiver the receiver for result or ambiguity messages
     * @param input           the username or partial username to search for
     * @param inform          whether to send a result message to the receiver
     * @return an {@link Optional} containing the matched player reference,
     *         or empty if zero or multiple matches were found
     */
    public static Optional<PlayerRef> searchPlayerRef(final IMessageReceiver messageReceiver, final String input, final boolean inform) {
        return searchPlayerRef(messageReceiver, input, inform, null);
    }
}