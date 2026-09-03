package io.github.trae.hytale.framework.utility;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import lombok.experimental.UtilityClass;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Optional;

/**
 * Utility class for player-related helper methods.
 *
 * <p>Converts between the two representations of a player, the {@link PlayerRef} session handle and
 * the {@link Player} entity component, and exposes connection details that hang off the session. A
 * player between worlds holds no entity reference, so the conversions return an empty result rather
 * than failing.</p>
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
     * @param playerRef the player reference to resolve the IP address for
     * @return an {@link Optional} containing the IP address string,
     * or empty if the reference is null or the address cannot be resolved
     */
    public static Optional<String> getIpAddress(final PlayerRef playerRef) {
        if (playerRef == null) {
            return Optional.empty();
        }

        final SocketAddress socketAddress = playerRef.getPacketHandler().getChannel().remoteAddress();

        if (!(socketAddress instanceof final InetSocketAddress inetSocketAddress)) {
            return Optional.empty();
        }

        final InetAddress inetAddress = inetSocketAddress.getAddress();

        return inetAddress == null ? Optional.empty() : Optional.of(inetAddress.getHostAddress());
    }
}