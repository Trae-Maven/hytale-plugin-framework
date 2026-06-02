package io.github.trae.hytale.framework.helper;

import com.hypixel.hytale.server.core.command.system.CommandRegistration;
import io.github.trae.hytale.framework.HytalePlugin;
import io.github.trae.hytale.framework.command.BaseCommand;
import io.github.trae.hytale.framework.command.BaseSubCommand;
import io.github.trae.hytale.framework.command.impl.SharedBaseCommand;
import io.github.trae.hytale.framework.helper.abstracts.AbstractHelper;
import io.github.trae.hytale.framework.helper.interfaces.Processable;

import java.util.LinkedHashMap;
import java.util.Optional;

/**
 * Helper responsible for managing command registrations within a {@link HytalePlugin}.
 *
 * <p>Parent commands are queued via {@link #register(SharedBaseCommand)} and bulk-registered
 * against the plugin's {@link com.hypixel.hytale.server.core.command.system.CommandRegistry}
 * when {@link #process()} is invoked. This two-phase approach allows all commands to
 * be declared during plugin initialization and registered in a single pass.</p>
 *
 * <p>Sub-commands are attached directly to their parent module's command at
 * registration time and are not tracked for bulk processing.</p>
 *
 * <p>Each parent command is tracked alongside its {@link CommandRegistration}, enabling
 * clean unregistration via {@link #unregister(SharedBaseCommand)}.</p>
 */
public class CommandHelper extends AbstractHelper<SharedBaseCommand<?>> implements Processable {

    /**
     * Map of parent commands to their registration handles.
     *
     * <p>A {@code null} value indicates the command has been queued but not yet
     * registered via {@link #process()}.</p>
     */
    private final LinkedHashMap<BaseCommand<?, ?, ?>, CommandRegistration> REGISTRATIONS = new LinkedHashMap<>();

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
     * <p>If the command is a {@link BaseCommand}, it is added to the registration queue
     * with a {@code null} handle, to be processed later by {@link #process()}.</p>
     *
     * <p>If the command is a {@link BaseSubCommand}, it is immediately attached to its
     * parent module's command and is not queued.</p>
     *
     * @param sharedBaseCommand the command to register
     */
    @Override
    public void register(final SharedBaseCommand<?> sharedBaseCommand) {
        // Parent Command
        if (sharedBaseCommand instanceof final BaseCommand<?, ?, ?> baseCommand) {
            this.REGISTRATIONS.putIfAbsent(baseCommand, null);
        }

        // Sub Command
        if (sharedBaseCommand instanceof final BaseSubCommand<?, ?, ?> baseSubCommand) {
            baseSubCommand.getModule().getAbstractCommand().addSubCommand(baseSubCommand.getAbstractCommand());
        }
    }

    /**
     * Unregisters a previously registered command.
     *
     * <p>If the command is a {@link BaseCommand}, it is removed from the registration
     * queue and, if it had an active {@link CommandRegistration}, that registration is
     * released.</p>
     *
     * <p>If the command is a {@link BaseSubCommand}, it is detached from its parent
     * module's command.</p>
     *
     * @param sharedBaseCommand the command to unregister
     */
    @Override
    public void unregister(final SharedBaseCommand<?> sharedBaseCommand) {
        // Parent Command
        if (sharedBaseCommand instanceof final BaseCommand<?, ?, ?> baseCommand) {
            Optional.ofNullable(this.REGISTRATIONS.remove(baseCommand)).ifPresent(CommandRegistration::unregister);
        }

        // Sub Command
        if (sharedBaseCommand instanceof final BaseSubCommand<?, ?, ?> baseSubCommand) {
            baseSubCommand.getModule().getAbstractCommand().getSubCommands().remove(baseSubCommand.getAbstractCommand().getName());
        }
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
        this.REGISTRATIONS.replaceAll((baseCommand, commandRegistration) -> {
            // Skip commands that are already registered
            if (commandRegistration != null) {
                return commandRegistration;
            }

            return this.getPlugin().getCommandRegistry().registerCommand(baseCommand.getAbstractCommand());
        });
    }
}