package io.github.trae.hytale.framework.helper;

import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.command.system.CommandRegistration;
import io.github.trae.hytale.framework.HytalePlugin;
import io.github.trae.hytale.framework.command.BaseCommand;
import io.github.trae.hytale.framework.command.BaseSubCommand;
import io.github.trae.hytale.framework.command.interfaces.SharedBaseCommand;
import io.github.trae.hytale.framework.helper.abstracts.AbstractHelper;
import io.github.trae.hytale.framework.helper.interfaces.Processable;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
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
 *
 * <p>Registering a command first strips any built-in system command sharing its label
 * or one of its aliases, allowing framework commands to transparently override Hytale's
 * defaults. When the framework command is later unregistered, the displaced built-in is
 * restored under the same name.</p>
 */
public class CommandHelper extends AbstractHelper<SharedBaseCommand<?>> implements Processable {

    /**
     * Reflective handle to the private {@code aliases} map on {@link CommandManager}.
     *
     * <p>The SDK exposes no public means of clearing or seeding alias entries for an
     * externally managed command, so the backing map is accessed reflectively. Resolved
     * once at class load to avoid repeating the lookup on every registration.</p>
     */
    private static final Field ALIASES_FIELD;

    static {
        try {
            final Field field = CommandManager.class.getDeclaredField("aliases");
            field.setAccessible(true);

            ALIASES_FIELD = field;
        } catch (final ReflectiveOperationException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    /**
     * Map of parent commands to their registration handles.
     *
     * <p>A {@code null} value indicates the command has been queued but not yet
     * registered via {@link #process()}.</p>
     */
    private final LinkedHashMap<BaseCommand<?, ?, ?>, CommandRegistration> REGISTRATIONS = new LinkedHashMap<>();

    /**
     * Built-in system commands displaced by framework registrations, keyed by the
     * lowercased name they were removed under.
     *
     * <p>Captured on strip so the matching built-in can be restored when the framework
     * command occupying that name is unregistered.</p>
     */
    private final LinkedHashMap<String, AbstractCommand> DISPLACED = new LinkedHashMap<>();

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
     * <p>Any built-in system command sharing this command's label or one of its aliases
     * is stripped first, so the framework command takes its place.</p>
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
        this.unregisterSystemCommand(sharedBaseCommand.getLabel());

        for (final String alias : sharedBaseCommand.getAliases()) {
            this.unregisterSystemCommand(alias);
        }

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
     * <p>Any built-in system command displaced by this command's label or aliases is
     * restored, returning the manager to its pre-registration state for those names.</p>
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
            final AbstractCommand parent = baseSubCommand.getModule().getAbstractCommand();

            if (parent != null) {
                parent.getSubCommands().remove(baseSubCommand.getAbstractCommand().getName());
            }
        }

        this.restoreSystemCommand(sharedBaseCommand.getLabel());

        for (final String alias : sharedBaseCommand.getAliases()) {
            this.restoreSystemCommand(alias);
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

    /**
     * Removes a built-in system command, along with its aliases, from the {@link CommandManager}.
     *
     * <p>Resolves and removes the command from the manager's live registration map by its
     * lowercased name, capturing the removed instance for later restoration. If the command
     * declares aliases, the manager's private alias map is updated reflectively to drop the
     * now-dangling entries.</p>
     *
     * @param name the command label or alias to strip
     */
    private void unregisterSystemCommand(final String name) {
        final CommandManager commandManager = CommandManager.get();

        final Map<String, AbstractCommand> commandRegistration = commandManager.getCommandRegistration();

        final String key = name.toLowerCase(Locale.ROOT);

        final AbstractCommand abstractCommand = commandRegistration.remove(key);

        if (abstractCommand == null) {
            return;
        }

        this.DISPLACED.putIfAbsent(key, abstractCommand);

        if (abstractCommand.getAliases().isEmpty()) {
            return;
        }

        try {
            @SuppressWarnings("unchecked") final Map<String, String> aliases = (Map<String, String>) ALIASES_FIELD.get(commandManager);

            for (final String alias : abstractCommand.getAliases()) {
                aliases.remove(alias);
            }
        } catch (final ReflectiveOperationException exception) {
            throw new RuntimeException("Failed to clear aliases for command: " + name, exception);
        }
    }

    /**
     * Restores a previously displaced built-in system command under the given name.
     *
     * <p>If a built-in was captured for the lowercased name, it is returned to the
     * manager's registration map and its aliases are re-seeded. Existing entries are
     * never overwritten, so a name still occupied by a live framework command is left
     * untouched.</p>
     *
     * @param name the command label or alias to restore
     */
    private void restoreSystemCommand(final String name) {
        final String key = name.toLowerCase(Locale.ROOT);

        final AbstractCommand abstractCommand = this.DISPLACED.remove(key);

        if (abstractCommand == null) {
            return;
        }

        final CommandManager commandManager = CommandManager.get();

        final Map<String, AbstractCommand> commandRegistration = commandManager.getCommandRegistration();

        commandRegistration.putIfAbsent(key, abstractCommand);

        if (abstractCommand.getAliases().isEmpty()) {
            return;
        }

        try {
            @SuppressWarnings("unchecked") final Map<String, String> aliases = (Map<String, String>) ALIASES_FIELD.get(commandManager);

            for (final String alias : abstractCommand.getAliases()) {
                aliases.putIfAbsent(alias, abstractCommand.getName());
            }
        } catch (final ReflectiveOperationException exception) {
            throw new RuntimeException("Failed to restore aliases for command: " + name, exception);
        }
    }
}