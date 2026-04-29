package io.github.trae.hytale.framework.utility;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.netty.channel.Channel;
import io.netty.handler.codec.quic.QuicStreamChannel;
import lombok.experimental.UtilityClass;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Optional;

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
     * the underlying Netty channel.
     *
     * <p>Handles both QUIC stream channels (where the remote address
     * is on the parent channel) and standard channels.</p>
     *
     * @param player the player to resolve the IP address for
     * @return an {@link Optional} containing the IP address string,
     * or empty if the address cannot be resolved
     */
    public static Optional<String> getIpAddress(final PlayerRef player) {
        final Channel channel = player.getPacketHandler().getChannel();

        final SocketAddress socketAddress;

        if (channel instanceof final QuicStreamChannel quicStreamChannel) {
            socketAddress = quicStreamChannel.parent().remoteSocketAddress();
        } else {
            socketAddress = channel.remoteAddress();
        }

        if (!(socketAddress instanceof final InetSocketAddress inetSocketAddress)) {
            return Optional.empty();
        }

        return Optional.ofNullable(inetSocketAddress.getAddress().getHostAddress());
    }
}