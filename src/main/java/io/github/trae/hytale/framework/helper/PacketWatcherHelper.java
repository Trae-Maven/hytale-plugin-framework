package io.github.trae.hytale.framework.helper;

import com.hypixel.hytale.server.core.io.adapter.PacketAdapters;
import com.hypixel.hytale.server.core.io.adapter.PacketFilter;
import com.hypixel.hytale.server.core.io.adapter.PacketWatcher;
import io.github.trae.hytale.framework.HytalePlugin;
import io.github.trae.hytale.framework.helper.abstracts.AbstractHelper;
import io.github.trae.hytale.framework.packet.InboundPacketWatcher;
import io.github.trae.hytale.framework.packet.OutboundPacketWatcher;

import java.util.LinkedHashMap;

/**
 * Helper responsible for managing packet watcher registrations within a {@link HytalePlugin}.
 *
 * <p>Registers {@link PacketWatcher} instances with the server's
 * {@link PacketAdapters} pipeline. The direction (inbound or outbound) is
 * determined by whether the watcher implements {@link InboundPacketWatcher} or
 * {@link OutboundPacketWatcher}. Each watcher is tracked alongside its corresponding
 * {@link PacketFilter} handle, enabling clean deregistration via
 * {@link #unregister(PacketWatcher)}.</p>
 *
 * <p>Packet watchers observe packets on the network thread and cannot
 * block or modify them. For ECS component access within a watcher, operations
 * must be scheduled onto the world thread via {@code world.execute()}.</p>
 */
public class PacketWatcherHelper extends AbstractHelper<PacketWatcher> {

    /**
     * Map of packet watchers to their associated {@link PacketFilter} handles
     * returned by {@link PacketAdapters}.
     */
    private final LinkedHashMap<PacketWatcher, PacketFilter> REGISTRATIONS = new LinkedHashMap<>();

    /**
     * Creates a new {@link PacketWatcherHelper} bound to the given plugin.
     *
     * @param plugin the owning plugin instance
     */
    public PacketWatcherHelper(final HytalePlugin plugin) {
        super(plugin);
    }

    /**
     * Registers a packet watcher with its corresponding packet pipeline.
     *
     * <p>The direction is determined by whether the watcher implements
     * {@link InboundPacketWatcher} or {@link OutboundPacketWatcher}. The {@link PacketFilter}
     * handle returned by {@link PacketAdapters} is stored for later deregistration.</p>
     *
     * @param packetWatcher the packet watcher instance to register
     * @throws IllegalStateException if the watcher implements neither {@link InboundPacketWatcher} nor {@link OutboundPacketWatcher}
     */
    @Override
    public void register(final PacketWatcher packetWatcher) {
        if (packetWatcher instanceof InboundPacketWatcher) {
            this.REGISTRATIONS.put(packetWatcher, PacketAdapters.registerInbound(packetWatcher));
        } else if (packetWatcher instanceof OutboundPacketWatcher) {
            this.REGISTRATIONS.put(packetWatcher, PacketAdapters.registerOutbound(packetWatcher));
        } else {
            throw new IllegalStateException("PacketWatcher must implement either %s or %s: %s".formatted(InboundPacketWatcher.class.getSimpleName(), OutboundPacketWatcher.class.getSimpleName(), packetWatcher.getClass().getName()));
        }
    }

    /**
     * Unregisters a packet watcher from its corresponding packet pipeline.
     *
     * <p>Uses the stored {@link PacketFilter} handle to deregister from
     * {@link PacketAdapters}. The pipeline direction is determined by whether
     * the watcher implements {@link InboundPacketWatcher} or {@link OutboundPacketWatcher}.
     * If the watcher was never registered, this call is a no-op.</p>
     *
     * @param packetWatcher the packet watcher instance to unregister
     * @throws IllegalStateException if the watcher implements neither {@link InboundPacketWatcher} nor {@link OutboundPacketWatcher}
     */
    @Override
    public void unregister(final PacketWatcher packetWatcher) {
        final PacketFilter packetFilter = this.REGISTRATIONS.remove(packetWatcher);
        if (packetFilter == null) {
            return;
        }

        if (packetWatcher instanceof InboundPacketWatcher) {
            PacketAdapters.deregisterInbound(packetFilter);
        } else if (packetWatcher instanceof OutboundPacketWatcher) {
            PacketAdapters.deregisterOutbound(packetFilter);
        } else {
            throw new IllegalStateException("PacketWatcher must implement either %s or %s: %s".formatted(InboundPacketWatcher.class.getSimpleName(), OutboundPacketWatcher.class.getSimpleName(), packetWatcher.getClass().getName()));
        }
    }
}