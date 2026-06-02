package io.github.trae.hytale.framework;

import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.io.adapter.PacketFilter;
import com.hypixel.hytale.server.core.io.adapter.PacketWatcher;
import com.hypixel.hytale.server.core.io.adapter.PlayerPacketFilter;
import com.hypixel.hytale.server.core.io.adapter.PlayerPacketWatcher;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import io.github.trae.di.InjectorApi;
import io.github.trae.hf.Plugin;
import io.github.trae.hytale.framework.command.impl.SharedBaseCommand;
import io.github.trae.hytale.framework.event.EventListener;
import io.github.trae.hytale.framework.helper.*;
import io.github.trae.hytale.framework.plugin.events.PluginInitializeEvent;
import io.github.trae.hytale.framework.plugin.events.PluginShutdownEvent;
import io.github.trae.hytale.framework.system.SystemListener;
import io.github.trae.hytale.framework.utility.UtilEvent;
import io.github.trae.hytale.framework.utility.UtilPlugin;
import io.github.trae.hytale.framework.utility.UtilTask;
import io.github.trae.utilities.UtilLogger;
import lombok.Getter;

import javax.annotation.Nonnull;

/**
 * Base plugin class for the Hytale Plugin Framework.
 *
 * <p>Extends Hytale's {@link JavaPlugin} and implements the
 * {@link io.github.trae.hf.Plugin Hierarchy-Framework Plugin} contract,
 * bridging the Hytale plugin lifecycle with the component-based hierarchy
 * architecture.</p>
 *
 * <p>Automatically handles registration and teardown of framework components
 * through the {@link #onComponentInitialize(Object)} and
 * {@link #onComponentShutdown(Object)} lifecycle callbacks:</p>
 * <ul>
 *   <li>{@link EventListener} — registered with the event bus via {@link EventHelper}</li>
 *   <li>{@link SystemListener} — registered with the ECS store registry via {@link SystemHelper}</li>
 *   <li>{@link PacketWatcher} — registered with the packet pipeline via {@link PacketWatcherHelper}</li>
 *   <li>{@link PlayerPacketWatcher} — registered with the packet pipeline via {@link PlayerPacketWatcherHelper}</li>
 *   <li>{@link PacketFilter} — registered with the packet pipeline via {@link PacketFilterHelper}</li>
 *   <li>{@link PlayerPacketFilter} — registered with the packet pipeline via {@link PlayerPacketFilterHelper}</li>
 *   <li>{@link SharedBaseCommand} as {@link io.github.trae.hytale.framework.command.BaseCommand} —
 *       queued for registration with the command system via {@link CommandHelper}</li>
 *   <li>{@link SharedBaseCommand} as {@link io.github.trae.hytale.framework.command.BaseSubCommand} —
 *       attached to its parent command as a subcommand via {@link CommandHelper}</li>
 * </ul>
 *
 * <p>Commands use a two-phase registration: they are queued during component
 * initialization and bulk-registered when {@link #initializePlugin()} calls
 * {@link CommandHelper#process()}.</p>
 *
 * @see EventHelper
 * @see SystemHelper
 * @see PacketWatcherHelper
 * @see PlayerPacketWatcherHelper
 * @see PacketFilterHelper
 * @see PlayerPacketFilterHelper
 * @see CommandHelper
 */
@Getter
public class HytalePlugin extends JavaPlugin implements Plugin {

    /**
     * Helper managing command registrations for this plugin.
     */
    private final CommandHelper commandHelper;

    /**
     * Helper managing ECS system registrations for this plugin.
     */
    private final SystemHelper systemHelper;

    /**
     * Helper managing event listener registrations for this plugin.
     */
    private final EventHelper eventHelper;

    /**
     * Helper managing packet watcher registrations for this plugin.
     */
    private final PacketWatcherHelper packetWatcherHelper;

    /**
     * Helper managing player packet watcher registrations for this plugin.
     */
    private final PlayerPacketWatcherHelper playerPacketWatcherHelper;

    /**
     * Helper managing packet filter registrations for this plugin.
     */
    private final PacketFilterHelper packetFilterHelper;

    /**
     * Helper managing player packet filter registrations for this plugin.
     */
    private final PlayerPacketFilterHelper playerPacketFilterHelper;

    /**
     * Creates a new {@link HytalePlugin} and initializes all framework helpers.
     *
     * @param javaPluginInit the Hytale-provided plugin initialization context
     */
    public HytalePlugin(@Nonnull final JavaPluginInit javaPluginInit) {
        super(javaPluginInit);

        UtilLogger.setLogger(this.getLogger());

        InjectorApi.setConfigurationDirectory(this.getClass(), this.getDataDirectory());

        if (InjectorApi.getScheduledExecutorService() == null) {
            InjectorApi.setScheduledExecutorService(HytaleServer.SCHEDULED_EXECUTOR);
        }

        InjectorApi.setSynchronousExecutor(this.getClass(), UtilTask::executeSynchronous);
        InjectorApi.setAsynchronousExecutor(this.getClass(), UtilTask::executeAsynchronous);

        this.eventHelper = new EventHelper(this);
        this.systemHelper = new SystemHelper(this);
        this.commandHelper = new CommandHelper(this);
        this.packetWatcherHelper = new PacketWatcherHelper(this);
        this.playerPacketWatcherHelper = new PlayerPacketWatcherHelper(this);
        this.packetFilterHelper = new PacketFilterHelper(this);
        this.playerPacketFilterHelper = new PlayerPacketFilterHelper(this);
    }

    /**
     * Initializes the plugin by running the hierarchy lifecycle via
     * {@link Plugin#initializePlugin()}, bulk-registering queued commands
     * via {@link CommandHelper#process()}, registering this plugin with
     * {@link UtilPlugin}, and dispatching a {@link PluginInitializeEvent}.
     */
    @Override
    public void initializePlugin() {
        // Run hierarchy initialization — discovers and initializes all components
        Plugin.super.initializePlugin();

        // Bulk-register all queued commands with the command registry
        this.commandHelper.process();

        UtilPlugin.registerInternalPlugin(this);

        UtilEvent.dispatch(new PluginInitializeEvent(this));
    }

    /**
     * Dispatches a {@link PluginShutdownEvent} to notify listeners
     * that the plugin is about to shut down, then runs the hierarchy
     * teardown via {@link Plugin#shutdownPlugin()}.
     */
    @Override
    public void shutdownPlugin() {
        UtilEvent.dispatch(new PluginShutdownEvent(this));

        Plugin.super.shutdownPlugin();
    }

    /**
     * Lifecycle callback invoked when a hierarchy component is initialized.
     *
     * <p>Routes the component to the appropriate helper based on its type:</p>
     * <ul>
     *   <li>{@link EventListener} — registered with the event bus</li>
     *   <li>{@link SystemListener} — registered with the ECS store registry</li>
     *   <li>{@link PacketWatcher} — registered with the packet pipeline</li>
     *   <li>{@link PlayerPacketWatcher} — registered with the packet pipeline</li>
     *   <li>{@link PacketFilter} — registered with the packet pipeline</li>
     *   <li>{@link PlayerPacketFilter} — registered with the packet pipeline</li>
     *   <li>{@link SharedBaseCommand} (a {@code BaseCommand}) — queued as a root command</li>
     *   <li>{@link SharedBaseCommand} (a {@code BaseSubCommand}) — attached to the parent
     *       module's command as a subcommand</li>
     * </ul>
     *
     * @param instance the component instance being initialized
     */
    @Override
    public void onComponentInitialize(final Object instance) {
        Plugin.super.onComponentInitialize(instance);

        if (instance instanceof final EventListener listener) {
            this.eventHelper.register(listener);
        }

        if (instance instanceof final SystemListener systemListener) {
            this.systemHelper.register(systemListener);
        }

        if (instance instanceof final SharedBaseCommand<?> sharedBaseCommand) {
            this.commandHelper.register(sharedBaseCommand);
        }

        if (instance instanceof final PacketWatcher packetWatcher) {
            this.packetWatcherHelper.register(packetWatcher);
        }

        if (instance instanceof final PlayerPacketWatcher playerPacketWatcher) {
            this.playerPacketWatcherHelper.register(playerPacketWatcher);
        }

        if (instance instanceof final PacketFilter packetFilter) {
            this.packetFilterHelper.register(packetFilter);
        }

        if (instance instanceof final PlayerPacketFilter playerPacketFilter) {
            this.playerPacketFilterHelper.register(playerPacketFilter);
        }
    }

    /**
     * Lifecycle callback invoked when a hierarchy component is shut down.
     *
     * <p>Reverses the registrations performed in {@link #onComponentInitialize(Object)}:</p>
     * <ul>
     *   <li>{@link EventListener} — unregistered from the event bus</li>
     *   <li>{@link SystemListener} — unregistered from the ECS store registry</li>
     *   <li>{@link PacketWatcher} — unregistered from the packet pipeline</li>
     *   <li>{@link PlayerPacketWatcher} — unregistered from the packet pipeline</li>
     *   <li>{@link PacketFilter} — unregistered from the packet pipeline</li>
     *   <li>{@link PlayerPacketFilter} — unregistered from the packet pipeline</li>
     *   <li>{@link SharedBaseCommand} (a {@code BaseCommand}) — unregistered from the command system</li>
     *   <li>{@link SharedBaseCommand} (a {@code BaseSubCommand}) — removed from the parent command's
     *       subcommand map</li>
     * </ul>
     *
     * @param instance the component instance being shut down
     */
    @Override
    public void onComponentShutdown(final Object instance) {
        if (instance instanceof final EventListener listener) {
            this.eventHelper.unregister(listener);
        }

        if (instance instanceof final SystemListener systemListener) {
            this.systemHelper.unregister(systemListener);
        }

        if (instance instanceof final SharedBaseCommand<?> sharedBaseCommand) {
            this.commandHelper.unregister(sharedBaseCommand);
        }

        if (instance instanceof final PacketWatcher packetWatcher) {
            this.packetWatcherHelper.unregister(packetWatcher);
        }

        if (instance instanceof final PlayerPacketWatcher playerPacketWatcher) {
            this.playerPacketWatcherHelper.unregister(playerPacketWatcher);
        }

        if (instance instanceof final PacketFilter packetFilter) {
            this.packetFilterHelper.unregister(packetFilter);
        }

        if (instance instanceof final PlayerPacketFilter playerPacketFilter) {
            this.playerPacketFilterHelper.unregister(playerPacketFilter);
        }

        Plugin.super.onComponentShutdown(instance);
    }
}