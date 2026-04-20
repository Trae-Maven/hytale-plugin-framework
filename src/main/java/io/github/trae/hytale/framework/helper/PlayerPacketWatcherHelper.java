package io.github.trae.hytale.framework.helper;

import com.hypixel.hytale.server.core.io.adapter.PacketAdapters;
import com.hypixel.hytale.server.core.io.adapter.PacketFilter;
import com.hypixel.hytale.server.core.io.adapter.PlayerPacketWatcher;
import io.github.trae.hytale.framework.HytalePlugin;
import io.github.trae.hytale.framework.helper.abstracts.AbstractHelper;
import io.github.trae.hytale.framework.packet.InboundPacketWatcher;
import io.github.trae.hytale.framework.packet.OutboundPacketWatcher;

import java.util.LinkedHashMap;

/**
 * Helper responsible for managing inbound packet watcher registrations within a {@link HytalePlugin}.
 *
 * <p>Registers {@link PlayerPacketWatcher} instances with the server's
 * {@link PacketAdapters} inbound pipeline. Each watcher is tracked alongside
 * its corresponding {@link PacketFilter} handle, enabling clean deregistration
 * via {@link #unregister(PlayerPacketWatcher)}.</p>
 *
 * <p>Packet watchers observe inbound packets on the network thread and cannot
 * block or modify them. For ECS component access within a watcher, operations
 * must be scheduled onto the world thread via {@code world.execute()}.</p>
 */
public class PlayerPacketWatcherHelper extends AbstractHelper<PlayerPacketWatcher> {

    /**
     * Map of packet watchers to their associated {@link PacketFilter} handles
     * returned by {@link PacketAdapters#registerInbound(PlayerPacketWatcher)}.
     */
    private final LinkedHashMap<PlayerPacketWatcher, PacketFilter> REGISTRATIONS = new LinkedHashMap<>();

    /**
     * Creates a new {@link PlayerPacketWatcherHelper} bound to the given plugin.
     *
     * @param plugin the owning plugin instance
     */
    public PlayerPacketWatcherHelper(final HytalePlugin plugin) {
        super(plugin);
    }

    /**
     * Registers a packet watcher with the inbound packet pipeline.
     *
     * <p>The {@link PacketFilter} handle returned by {@link PacketAdapters#registerInbound(PlayerPacketWatcher)}
     * is stored for later deregistration.</p>
     *
     * @param playerPacketWatcher the packet watcher instance to register
     */
    @Override
    public void register(final PlayerPacketWatcher playerPacketWatcher) {
        if (playerPacketWatcher instanceof InboundPacketWatcher) {
            this.REGISTRATIONS.put(playerPacketWatcher, PacketAdapters.registerInbound(playerPacketWatcher));
        } else if (playerPacketWatcher instanceof OutboundPacketWatcher) {
            this.REGISTRATIONS.put(playerPacketWatcher, PacketAdapters.registerOutbound(playerPacketWatcher));
        } else {
            throw new IllegalStateException("PlayerPacketWatcher must implement either %s or %s: %s".formatted(InboundPacketWatcher.class.getSimpleName(), OutboundPacketWatcher.class.getSimpleName(), playerPacketWatcher.getClass().getName()));
        }
    }

    /**
     * Unregisters a packet watcher from the inbound packet pipeline.
     *
     * <p>Uses the stored {@link PacketFilter} handle to deregister from
     * {@link PacketAdapters}. If the watcher was never registered, this
     * call is a no-op.</p>
     *
     * @param playerPacketWatcher the packet watcher instance to unregister
     */
    @Override
    public void unregister(final PlayerPacketWatcher playerPacketWatcher) {
        final PacketFilter packetFilter = this.REGISTRATIONS.remove(playerPacketWatcher);
        if (packetFilter == null) {
            return;
        }

        if (playerPacketWatcher instanceof InboundPacketWatcher) {
            PacketAdapters.deregisterInbound(packetFilter);
        } else if (playerPacketWatcher instanceof OutboundPacketWatcher) {
            PacketAdapters.deregisterOutbound(packetFilter);
        } else {
            throw new IllegalStateException("PlayerPacketWatcher must implement either %s or %s: %s".formatted(InboundPacketWatcher.class.getSimpleName(), OutboundPacketWatcher.class.getSimpleName(), playerPacketWatcher.getClass().getName()));
        }
    }
}