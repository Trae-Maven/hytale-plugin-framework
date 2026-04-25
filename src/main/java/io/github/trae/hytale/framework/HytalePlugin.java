package io.github.trae.hytale.framework;

import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.io.adapter.PacketWatcher;
import com.hypixel.hytale.server.core.io.adapter.PlayerPacketWatcher;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import io.github.trae.di.InjectorApi;
import io.github.trae.hf.Module;
import io.github.trae.hf.Plugin;
import io.github.trae.hf.SubModule;
import io.github.trae.hytale.framework.event.Listener;
import io.github.trae.hytale.framework.helper.*;
import io.github.trae.hytale.framework.plugin.events.PluginInitializeEvent;
import io.github.trae.hytale.framework.plugin.events.PluginShutdownEvent;
import io.github.trae.hytale.framework.utility.UtilEvent;
import io.github.trae.hytale.framework.utility.UtilTask;
import io.github.trae.utilities.UtilJava;
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
 *   <li>{@link Listener} — registered with the event bus via {@link ListenerHelper}</li>
 *   <li>{@link PacketWatcher} — registered with the packet pipeline via {@link PacketWatcherHelper}</li>
 *   <li>{@link PlayerPacketWatcher} — registered with the packet pipeline via {@link PlayerPacketWatcherHelper}</li>
 *   <li>{@link EntityEventSystem} — registered with the ECS registry via {@link SystemHelper}</li>
 *   <li>{@link AbstractCommand} as {@link Module} — registered with the command system via {@link CommandHelper}</li>
 *   <li>{@link AbstractCommand} as {@link SubModule} — attached to the parent command as a subcommand</li>
 * </ul>
 *
 * <p>Commands use a two-phase registration: they are queued during component
 * initialization and bulk-registered when {@link #initializePlugin()} calls
 * {@link CommandHelper#process()}.</p>
 *
 * @see ListenerHelper
 * @see PacketWatcherHelper
 * @see PlayerPacketWatcherHelper
 * @see SystemHelper
 * @see CommandHelper
 */
@Getter
public class HytalePlugin extends JavaPlugin implements Plugin {

    /**
     * Helper managing command registrations for this plugin.
     */
    private final CommandHelper commandHelper;

    /**
     * Helper managing ECS event system registrations for this plugin.
     */
    private final SystemHelper systemHelper;

    /**
     * Helper managing event listener registrations for this plugin.
     */
    private final ListenerHelper listenerHelper;

    /**
     * Helper managing packet watcher registrations for this plugin.
     */
    private final PacketWatcherHelper packetWatcherHelper;

    /**
     * Helper managing player packet watcher registrations for this plugin.
     */
    private final PlayerPacketWatcherHelper playerPacketWatcherHelper;

    /**
     * Creates a new {@link HytalePlugin} and initializes all framework helpers.
     *
     * @param javaPluginInit the Hytale-provided plugin initialization context
     */
    public HytalePlugin(@Nonnull final JavaPluginInit javaPluginInit) {
        super(javaPluginInit);

        UtilLogger.setLogger(this.getLogger());

        this.listenerHelper = new ListenerHelper(this);
        this.systemHelper = new SystemHelper(this);
        this.commandHelper = new CommandHelper(this);
        this.packetWatcherHelper = new PacketWatcherHelper(this);
        this.playerPacketWatcherHelper = new PlayerPacketWatcherHelper(this);
    }

    /**
     * Initializes the plugin by setting the configuration directory to the
     * plugin's data folder and then running the hierarchy lifecycle.
     *
     * <p>Sets the configuration directory via
     * {@link InjectorApi#setConfigurationDirectory(java.nio.file.Path)} so that
     * {@link io.github.trae.di.configuration.annotations.Configuration @Configuration}
     * files are stored under the plugin's data folder, then delegates to
     * {@link Plugin#initializePlugin()} to trigger component discovery and
     * initialization. Queued commands are bulk-registered via
     * {@link CommandHelper#process()} after the hierarchy is fully wired.</p>
     */
    @Override
    public void initializePlugin() {
        InjectorApi.setConfigurationDirectory(this.getDataDirectory());

        InjectorApi.setSynchronousExecutor(UtilTask::executeSynchronous);
        InjectorApi.setAsynchronousExecutor(UtilTask::executeAsynchronous);

        // Run hierarchy initialization — discovers and initializes all components
        Plugin.super.initializePlugin();

        // Bulk-register all queued commands with the command registry
        this.commandHelper.process();

        UtilEvent.dispatch(new PluginInitializeEvent(this));
    }

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
     *   <li>{@link Listener} — registered with the event bus</li>
     *   <li>{@link PacketWatcher} — registered with the packet pipeline</li>
     *   <li>{@link PlayerPacketWatcher} — registered with the packet pipeline</li>
     *   <li>{@link EntityEventSystem} — registered with the ECS store registry</li>
     *   <li>{@link AbstractCommand} + {@link Module} — queued as a root command</li>
     *   <li>{@link AbstractCommand} + {@link SubModule} — attached to the parent
     *       module's command as a subcommand</li>
     * </ul>
     *
     * @param instance the component instance being initialized
     */
    @Override
    public void onComponentInitialize(final Object instance) {
        if (instance instanceof final Listener listener) {
            this.listenerHelper.register(listener);
        }

        if (instance instanceof final PacketWatcher packetWatcher) {
            this.packetWatcherHelper.register(packetWatcher);
        }

        if (instance instanceof final PlayerPacketWatcher playerPacketWatcher) {
            this.playerPacketWatcherHelper.register(playerPacketWatcher);
        }

        if (instance instanceof final EntityEventSystem<?, ?> entityEventSystem) {
            this.systemHelper.register(entityEventSystem);
        }

        // Root commands — queued for bulk registration during initializePlugin()
        if (instance instanceof final AbstractCommand abstractCommand && instance instanceof Module<?, ?>) {
            this.commandHelper.register(abstractCommand);
        }

        // Subcommands — attached directly to their parent command
        if (instance instanceof final AbstractCommand abstractSubCommand && instance instanceof final SubModule<?, ?> subModule) {
            final AbstractCommand abstractCommand = UtilJava.cast(AbstractCommand.class, subModule.getModule());

            abstractCommand.addSubCommand(abstractSubCommand);
        }
    }

    /**
     * Lifecycle callback invoked when a hierarchy component is shut down.
     *
     * <p>Reverses the registrations performed in {@link #onComponentInitialize(Object)}:</p>
     * <ul>
     *   <li>{@link Listener} — unregistered from the event bus</li>
     *   <li>{@link PacketWatcher} — unregistered from the packet pipeline</li>
     *   <li>{@link PlayerPacketWatcher} — unregistered from the packet pipeline</li>
     *   <li>{@link EntityEventSystem} — unregistered from the ECS store registry</li>
     *   <li>{@link AbstractCommand} + {@link Module} — unregistered from the command system</li>
     *   <li>{@link AbstractCommand} + {@link SubModule} — removed from the parent command's
     *       subcommand map</li>
     * </ul>
     *
     * @param instance the component instance being shut down
     */
    @Override
    public void onComponentShutdown(final Object instance) {
        if (instance instanceof final Listener listener) {
            this.listenerHelper.unregister(listener);
        }

        if (instance instanceof final PacketWatcher packetWatcher) {
            this.packetWatcherHelper.unregister(packetWatcher);
        }

        if (instance instanceof final PlayerPacketWatcher playerPacketWatcher) {
            this.playerPacketWatcherHelper.unregister(playerPacketWatcher);
        }

        if (instance instanceof final EntityEventSystem<?, ?> entityEventSystem) {
            this.systemHelper.unregister(entityEventSystem);
        }

        // Root commands — unregistered from the command system
        if (instance instanceof final AbstractCommand abstractCommand && instance instanceof Module<?, ?>) {
            this.commandHelper.unregister(abstractCommand);
        }

        // Subcommands — removed from the parent command's subcommand map
        if (instance instanceof final AbstractCommand abstractSubCommand && instance instanceof final SubModule<?, ?> subModule) {
            final AbstractCommand abstractCommand = UtilJava.cast(AbstractCommand.class, subModule.getModule());

            abstractCommand.getSubCommands().remove(abstractSubCommand.getName());
        }
    }
}