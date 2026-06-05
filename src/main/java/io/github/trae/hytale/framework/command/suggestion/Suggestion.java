package io.github.trae.hytale.framework.command.suggestion;

import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import io.github.trae.hytale.framework.command.interfaces.SharedBaseCommand;
import io.github.trae.hytale.framework.command.wrappers.ArgumentTypeWrapper;
import io.github.trae.utilities.objects.consumer.BiConsumer;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.function.Function;

/**
 * Describes a single command argument suggestion, pairing display metadata with a
 * sender-aware supplier of candidate completion values.
 *
 * <p>Each suggestion declares its {@link #usage} placeholder, a human-readable
 * {@link #name} and {@link #description}, and a {@link #contentFunction} that produces
 * the current list of completion candidates for a given {@link CommandSender} on
 * demand.</p>
 */
@AllArgsConstructor
@Getter
public class Suggestion {

    /**
     * Registers every suggestion declared by a {@link SharedBaseCommand} onto its backing
     * {@link AbstractCommand}, wiring each through an {@link ArgumentTypeWrapper}.
     *
     * <p>Each suggestion is added as an optional argument via
     * {@link AbstractCommand#withOptionalArg(String, String, com.hypixel.hytale.server.core.command.system.arguments.types.ArgumentType)},
     * using its {@link #getUsage() usage} placeholder and {@link #getDescription() description}.</p>
     */
    public static final BiConsumer<SharedBaseCommand<?>, AbstractCommand> CONSUMER = (sharedBaseCommand, abstractCommand) -> {
        for (final Suggestion suggestion : sharedBaseCommand.getSuggestions()) {
            abstractCommand.withOptionalArg(suggestion.getUsage(), suggestion.getDescription(), new ArgumentTypeWrapper(suggestion));
        }
    };

    /**
     * The human-readable display name of the argument (e.g. {@code "Player Name"}).
     */
    private final String name;

    /**
     * The usage placeholder shown in command syntax (e.g. {@code "playerName"}).
     */
    private final String usage;

    /**
     * The human-readable description of the argument.
     */
    private final String description;

    /**
     * Produces the list of completion candidates for the requesting {@link CommandSender}.
     */
    private final Function<CommandSender, List<String>> contentFunction;
}