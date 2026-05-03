package io.github.trae.hytale.framework.helper;

import com.hypixel.hytale.server.core.io.adapter.PacketAdapters;
import com.hypixel.hytale.server.core.io.adapter.PacketFilter;
import com.hypixel.hytale.server.core.io.adapter.PlayerPacketFilter;
import io.github.trae.hytale.framework.HytalePlugin;
import io.github.trae.hytale.framework.helper.abstracts.AbstractHelper;
import io.github.trae.hytale.framework.packet.InboundPacketFilter;
import io.github.trae.hytale.framework.packet.OutboundPacketFilter;

import java.util.LinkedHashMap;

/**
 * Helper responsible for managing player packet filter registrations within a {@link HytalePlugin}.
 *
 * <p>Registers {@link PlayerPacketFilter} instances with the server's
 * {@link PacketAdapters} pipeline. The direction (inbound or outbound) is
 * determined by whether the filter implements {@link InboundPacketFilter} or
 * {@link OutboundPacketFilter}. Each filter is tracked alongside its corresponding
 * {@link PacketFilter} handle, enabling clean deregistration via
 * {@link #unregister(PlayerPacketFilter)}.</p>
 *
 * <p>Player packet filters intercept packets on the network thread with access
 * to the associated {@link com.hypixel.hytale.server.core.universe.PlayerRef}.
 * Returning {@code true} from the filter blocks the packet. For ECS component
 * access within a filter, operations must be scheduled onto the world thread
 * via {@code world.execute()}.</p>
 */
public class PlayerPacketFilterHelper extends AbstractHelper<PlayerPacketFilter> {

    /**
     * Map of player packet filters to their associated {@link PacketFilter} handles
     * returned by {@link PacketAdapters}.
     */
    private final LinkedHashMap<PlayerPacketFilter, PacketFilter> REGISTRATIONS = new LinkedHashMap<>();

    /**
     * Creates a new {@link PlayerPacketFilterHelper} bound to the given plugin.
     *
     * @param plugin the owning plugin instance
     */
    public PlayerPacketFilterHelper(final HytalePlugin plugin) {
        super(plugin);
    }

    /**
     * Registers a player packet filter with its corresponding packet pipeline.
     *
     * <p>The direction is determined by whether the filter implements
     * {@link InboundPacketFilter} or {@link OutboundPacketFilter}. The {@link PacketFilter}
     * handle returned by {@link PacketAdapters} is stored for later deregistration.</p>
     *
     * @param playerPacketFilter the player packet filter instance to register
     * @throws IllegalStateException if the filter implements neither {@link InboundPacketFilter} nor {@link OutboundPacketFilter}
     */
    @Override
    public void register(final PlayerPacketFilter playerPacketFilter) {
        if (playerPacketFilter instanceof InboundPacketFilter) {
            this.REGISTRATIONS.put(playerPacketFilter, PacketAdapters.registerInbound(playerPacketFilter));
        } else if (playerPacketFilter instanceof OutboundPacketFilter) {
            this.REGISTRATIONS.put(playerPacketFilter, PacketAdapters.registerOutbound(playerPacketFilter));
        } else {
            throw new IllegalStateException("PlayerPacketFilter must implement either %s or %s: %s".formatted(InboundPacketFilter.class.getSimpleName(), OutboundPacketFilter.class.getSimpleName(), playerPacketFilter.getClass().getName()));
        }
    }

    /**
     * Unregisters a player packet filter from its corresponding packet pipeline.
     *
     * <p>Uses the stored {@link PacketFilter} handle to deregister from
     * {@link PacketAdapters}. The pipeline direction is determined by whether
     * the filter implements {@link InboundPacketFilter} or {@link OutboundPacketFilter}.
     * If the filter was never registered, this call is a no-op.</p>
     *
     * @param playerPacketFilter the player packet filter instance to unregister
     * @throws IllegalStateException if the filter implements neither {@link InboundPacketFilter} nor {@link OutboundPacketFilter}
     */
    @Override
    public void unregister(final PlayerPacketFilter playerPacketFilter) {
        final PacketFilter handle = this.REGISTRATIONS.remove(playerPacketFilter);
        if (handle == null) {
            return;
        }

        if (playerPacketFilter instanceof InboundPacketFilter) {
            PacketAdapters.deregisterInbound(handle);
        } else if (playerPacketFilter instanceof OutboundPacketFilter) {
            PacketAdapters.deregisterOutbound(handle);
        } else {
            throw new IllegalStateException("PlayerPacketFilter must implement either %s or %s: %s".formatted(InboundPacketFilter.class.getSimpleName(), OutboundPacketFilter.class.getSimpleName(), playerPacketFilter.getClass().getName()));
        }
    }
}