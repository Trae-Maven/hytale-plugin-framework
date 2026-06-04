package io.github.trae.hytale.framework.command.interfaces;

import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.console.ConsoleSender;
import io.github.trae.hytale.framework.command.impl.Confirmable;
import io.github.trae.hytale.framework.utility.UtilMessage;
import io.github.trae.utilities.UtilGeneric;
import io.github.trae.utilities.UtilJava;
import io.github.trae.utilities.UtilString;

import java.util.List;

/**
 * Shared contract for all framework commands, defining behavior common to both
 * top-level commands and sub-commands.
 *
 * <p>This interface centralizes sender-type resolution, permission handling, alias
 * management, and the execution pipeline so that {@link io.github.trae.hytale.framework.command.BaseCommand}
 * and {@link io.github.trae.hytale.framework.command.BaseSubCommand} can share a single
 * implementation surface. Concrete commands supply their typed {@link #execute(CommandSender, String[])}
 * logic while the framework drives dispatch through {@link #_Execute(CommandSender, String[])}.</p>
 *
 * @param <Sender> the expected {@link CommandSender} subtype this command accepts
 */
public interface SharedBaseCommand<Sender extends CommandSender> {

    /**
     * Resolves the runtime {@link Class} of the {@link Sender} type argument declared
     * on this command's implementation of {@link SharedBaseCommand}.
     *
     * @return the class of the expected command sender type
     * @throws IllegalStateException if the sender type argument cannot be resolved
     */
    @SuppressWarnings("unchecked")
    default Class<Sender> getClassOfCommandSender() {
        final Class<?> commandSenderClass = UtilGeneric.getGenericParameter(this.getClass(), SharedBaseCommand.class, 0);
        if (commandSenderClass == null) {
            throw new IllegalStateException("Could not resolve command sender type for %s".formatted(this.getClass().getName()));
        }

        return (Class<Sender>) commandSenderClass;
    }

    /**
     * Returns the underlying engine command wrapper backing this command.
     *
     * @return the {@link AbstractCommand} this command is bound to
     */
    AbstractCommand getAbstractCommand();

    /**
     * Indicates whether this command should be executed asynchronously.
     *
     * <p>Defaults to {@code false}; override to opt into asynchronous dispatch.</p>
     *
     * @return {@code true} if the command runs asynchronously, {@code false} otherwise
     */
    default boolean isAsynchronous() {
        return false;
    }

    /**
     * Returns the primary label (name) used to invoke this command.
     *
     * @return the command label
     */
    String getLabel();

    /**
     * Returns the human-readable description of this command.
     *
     * @return the command description
     */
    String getDescription();

    /**
     * Returns the permission node required to execute this command, or {@code null}/empty
     * if no permission is required.
     *
     * @return the permission node, or {@code null} if unrestricted
     */
    String getPermission();

    /**
     * Determines whether the given sender is permitted to execute this command.
     *
     * <p>Returns {@code true} when no permission is configured, when the sender is a
     * {@link ConsoleSender}, or when the sender holds the configured permission node.</p>
     *
     * @param sender the sender attempting to execute the command
     * @return {@code true} if the sender may execute, {@code false} otherwise
     */
    default boolean hasPermission(final CommandSender sender) {
        if (UtilString.isEmpty(this.getPermission())) {
            return true;
        }

        if (sender instanceof ConsoleSender) {
            return true;
        }

        return sender.hasPermission(this.getPermission());
    }

    /**
     * Returns the mutable list of aliases registered for this command.
     *
     * @return the command's alias list
     */
    List<String> getAliases();

    /**
     * Adds one or more aliases to this command.
     *
     * @param aliases the aliases to add
     */
    default void addAliases(final String... aliases) {
        this.getAliases().addAll(List.of(aliases));
    }

    /**
     * Performs pre-execution validation for the given sender.
     *
     * <p>Verifies that the sender matches the expected {@link Sender} type and holds the
     * required permission, messaging the sender on failure.</p>
     *
     * @param sender the sender attempting to execute the command
     * @return {@code true} if execution may proceed, {@code false} otherwise
     */
    default boolean canExecute(final CommandSender sender) {
        if (!(this.getClassOfCommandSender().isInstance(sender))) {
            UtilMessage.message(sender, "Command", "Invalid Command Sender!");
            return false;
        }

        if (!(this.hasPermission(sender))) {
            UtilMessage.message(sender, "Command", "You do not have permission to execute this command!");
            return false;
        }

        return true;
    }

    /**
     * Executes the command logic for a validated, correctly-typed sender.
     *
     * @param sender the command sender, guaranteed to be of type {@link Sender}
     * @param args   the command arguments
     */
    void execute(final Sender sender, final String[] args);

    /**
     * Internal dispatch entry point invoked by the engine command wrappers.
     *
     * <p>Runs {@link #canExecute(CommandSender)} validation and, on success,
     * casts the sender to the expected {@link Sender} type and delegates to
     * {@link #execute(CommandSender, String[])}.</p>
     *
     * <p>If this command is {@link Confirmable} and not configured to override
     * confirmation via {@link Confirmable#isPreExecuteConfirmCheck()}, execution is gated on
     * {@link Confirmable#hasConfirmed(CommandSender)} — the first invocation
     * prompts for confirmation and is suppressed, and a subsequent invocation
     * within the confirmation window proceeds.</p>
     *
     * @param commandSender the raw sender supplied by the engine
     * @param args          the command arguments
     */
    default void _Execute(final CommandSender commandSender, final String[] args) {
        if (this.canExecute(commandSender)) {
            if (this instanceof final Confirmable confirmable && confirmable.isPreExecuteConfirmCheck() && !(confirmable.hasConfirmed(commandSender))) {
                return;
            }

            this.execute(UtilJava.cast(this.getClassOfCommandSender(), commandSender), args);
        }
    }
}