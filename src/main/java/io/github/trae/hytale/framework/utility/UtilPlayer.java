package io.github.trae.hytale.framework.utility;

import com.hypixel.hytale.server.core.universe.PlayerRef;
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