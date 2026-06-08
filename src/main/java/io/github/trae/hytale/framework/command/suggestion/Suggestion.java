package io.github.trae.hytale.framework.command.suggestion;

import com.hypixel.hytale.server.core.command.system.CommandSender;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.function.Function;

/**
 * Declares a single positional argument slot's tab-completion behavior.
 *
 * <p>Each suggestion describes one positional slot: its {@link #usage} placeholder
 * (rendered as {@code <usage>} in command syntax), a human-readable {@link #name} and
 * {@link #description}, and a {@link #contentFunction} that produces the live completion
 * candidates for the requesting {@link CommandSender}.</p>
 *
 * <p>The framework reads a command's ordered suggestion list and auto-generates one
 * engine usage variant per argument count, declaring the corresponding slots as required
 * arguments so the client requests completions for them on TAB.</p>
 */
@AllArgsConstructor
@Getter
public class Suggestion {

    /**
     * The human-readable display name of the argument (e.g. {@code "Player Name"}).
     */
    private final String name;

    /**
     * The usage placeholder shown in command syntax (e.g. {@code "player"}).
     */
    private final String usage;

    /**
     * The human-readable description of the argument.
     */
    private final String description;

    /**
     * Produces the live list of completion candidates for the requesting sender.
     */
    private final Function<CommandSender, List<String>> contentFunction;
}