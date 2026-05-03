package io.github.trae.hytale.framework.helper;

import com.hypixel.hytale.server.core.io.adapter.PacketAdapters;
import com.hypixel.hytale.server.core.io.adapter.PacketFilter;
import io.github.trae.hytale.framework.HytalePlugin;
import io.github.trae.hytale.framework.helper.abstracts.AbstractHelper;
import io.github.trae.hytale.framework.packet.InboundPacketFilter;
import io.github.trae.hytale.framework.packet.OutboundPacketFilter;

/**
 * Helper responsible for managing packet filter registrations within a {@link HytalePlugin}.
 *
 * <p>Registers {@link PacketFilter} instances with the server's
 * {@link PacketAdapters} pipeline. The direction (inbound or outbound) is
 * determined by whether the filter implements {@link InboundPacketFilter} or
 * {@link OutboundPacketFilter}.</p>
 *
 * <p>Packet filters intercept packets on the network thread and can block them
 * by returning {@code true}. For ECS component access within a filter, operations
 * must be scheduled onto the world thread via {@code world.execute()}.</p>
 */
public class PacketFilterHelper extends AbstractHelper<PacketFilter> {

    /**
     * Creates a new {@link PacketFilterHelper} bound to the given plugin.
     *
     * @param plugin the owning plugin instance
     */
    public PacketFilterHelper(final HytalePlugin plugin) {
        super(plugin);
    }

    /**
     * Registers a packet filter with its corresponding packet pipeline.
     *
     * <p>The direction is determined by whether the filter implements
     * {@link InboundPacketFilter} or {@link OutboundPacketFilter}.</p>
     *
     * @param packetFilter the packet filter instance to register
     * @throws IllegalStateException if the filter implements neither {@link InboundPacketFilter} nor {@link OutboundPacketFilter}
     */
    @Override
    public void register(final PacketFilter packetFilter) {
        if (packetFilter instanceof InboundPacketFilter) {
            PacketAdapters.registerInbound(packetFilter);
        } else if (packetFilter instanceof OutboundPacketFilter) {
            PacketAdapters.registerOutbound(packetFilter);
        } else {
            throw new IllegalStateException("PacketFilter must implement either %s or %s: %s".formatted(InboundPacketFilter.class.getSimpleName(), OutboundPacketFilter.class.getSimpleName(), packetFilter.getClass().getName()));
        }
    }

    /**
     * Unregisters a packet filter from its corresponding packet pipeline.
     *
     * <p>The pipeline direction is determined by whether the filter implements
     * {@link InboundPacketFilter} or {@link OutboundPacketFilter}.</p>
     *
     * @param packetFilter the packet filter instance to unregister
     * @throws IllegalStateException if the filter implements neither {@link InboundPacketFilter} nor {@link OutboundPacketFilter}
     */
    @Override
    public void unregister(final PacketFilter packetFilter) {
        if (packetFilter instanceof InboundPacketFilter) {
            PacketAdapters.deregisterInbound(packetFilter);
        } else if (packetFilter instanceof OutboundPacketFilter) {
            PacketAdapters.deregisterOutbound(packetFilter);
        } else {
            throw new IllegalStateException("PacketFilter must implement either %s or %s: %s".formatted(InboundPacketFilter.class.getSimpleName(), OutboundPacketFilter.class.getSimpleName(), packetFilter.getClass().getName()));
        }
    }
}