package io.github.trae.hytale.framework.helper;

import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandRegistration;
import io.github.trae.hytale.framework.HytalePlugin;
import io.github.trae.hytale.framework.helper.abstracts.AbstractHelper;
import io.github.trae.hytale.framework.helper.interfaces.Processable;

import java.util.LinkedHashMap;

/**
 * Helper responsible for managing command registrations within a {@link HytalePlugin}.
 *
 * <p>Commands are queued via {@link #register(AbstractCommand)} and bulk-registered
 * against the plugin's {@link com.hypixel.hytale.server.core.command.system.CommandRegistry}
 * when {@link #process()} is invoked. This two-phase approach allows all commands to
 * be declared during plugin initialization and registered in a single pass.</p>
 *
 * <p>Each command is tracked alongside its {@link CommandRegistration}, enabling
 * clean unregistration via {@link #unregister(AbstractCommand)}.</p>
 */
public class CommandHelper extends AbstractHelper<AbstractCommand> implements Processable {

    /**
     * Map of commands to their registration handles.
     * A {@code null} value indicates the command has been queued but not yet processed.
     */
    private final LinkedHashMap<AbstractCommand, CommandRegistration> REGISTRATIONS = new LinkedHashMap<>();

    /**
     * Creates a new {@link CommandHelper} bound to the given plugin.
     *
     * @param plugin the owning plugin instance
     */
    public CommandHelper(final HytalePlugin plugin) {
        super(plugin);
    }

    /**
     * Queues a command for registration.
     *
     * <p>The command is stored with a {@code null} registration handle, which
     * will be resolved to an active {@link CommandRegistration} when
     * {@link #process()} is called. If the command is already queued or
     * registered, this call is a no-op ({@code putIfAbsent}).</p>
     *
     * @param abstractCommand the command to register
     */
    @Override
    public void register(final AbstractCommand abstractCommand) {
        this.REGISTRATIONS.putIfAbsent(abstractCommand, null);
    }

    /**
     * Unregisters a command and removes it from the registry.
     *
     * <p>If the command has an active {@link CommandRegistration}, it is
     * unregistered from the command system. If the command was queued
     * but never processed, it is simply removed.</p>
     *
     * @param abstractCommand the command to unregister
     */
    @Override
    public void unregister(final AbstractCommand abstractCommand) {
        final CommandRegistration registration = this.REGISTRATIONS.remove(abstractCommand);
        if (registration == null) {
            return;
        }

        registration.unregister();
    }

    /**
     * Processes all queued commands by registering them with the plugin's command registry.
     *
     * <p>Iterates over all entries and registers any command that has a {@code null}
     * registration handle. Already-registered commands are left untouched, ensuring
     * idempotent behavior on repeated calls.</p>
     */
    @Override
    public void process() {
        this.REGISTRATIONS.replaceAll((abstractCommand, registration) -> {
            // Skip commands that are already registered
            if (registration != null) {
                return registration;
            }

            return this.getPlugin().getCommandRegistry().registerCommand(abstractCommand);
        });
    }
}