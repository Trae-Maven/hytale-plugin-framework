package io.github.trae.hytale.framework.command.suggestion.abstracts;

import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import io.github.trae.hytale.framework.command.interfaces.SharedBaseCommand;
import io.github.trae.hytale.framework.command.wrappers.ArgumentTypeWrapper;
import io.github.trae.utilities.objects.consumer.BiConsumer;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.function.Supplier;

/**
 * Describes a single command argument suggestion, pairing display metadata with a
 * dynamic supplier of candidate completion values.
 *
 * <p>Each suggestion declares whether it is {@link #required}, its {@link #usage}
 * placeholder, a human-readable {@link #name} and {@link #description}, and a
 * {@link #contentSupplier} that produces the current list of completion candidates on
 * demand. Concrete subtypes {@link io.github.trae.hytale.framework.command.suggestion.RequiredSuggestion}
 * and {@link io.github.trae.hytale.framework.command.suggestion.OptionalSuggestion} fix the
 * required flag.</p>
 */
@AllArgsConstructor
@Getter
public class AbstractSuggestion {

    /**
     * Registers every suggestion declared by a {@link SharedBaseCommand} onto its backing
     * {@link AbstractCommand}, wiring each through an {@link ArgumentTypeWrapper}.
     *
     * <p>Required suggestions are added via
     * {@link AbstractCommand#withRequiredArg(String, String, com.hypixel.hytale.server.core.command.system.arguments.types.ArgumentType)}
     * and optional ones via the corresponding optional-argument call.</p>
     */
    public static final BiConsumer<SharedBaseCommand<?>, AbstractCommand> CONSUMER = (sharedBaseCommand, abstractCommand) -> {
        for (final AbstractSuggestion suggestion : sharedBaseCommand.getSuggestions()) {
            if (suggestion.isRequired()) {
                abstractCommand.withRequiredArg(suggestion.getUsage(), suggestion.getDescription(), new ArgumentTypeWrapper(suggestion));
            } else {
                abstractCommand.withOptionalArg(suggestion.getUsage(), suggestion.getDescription(), new ArgumentTypeWrapper(suggestion));
            }
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
     * Whether this argument is required ({@code true}) or optional ({@code false}).
     */
    private boolean required;

    /**
     * Supplies the current list of completion candidates on demand.
     */
    private final Supplier<List<String>> contentSupplier;
}