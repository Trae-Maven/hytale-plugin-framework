package io.github.trae.hytale.framework.command.suggestion;

import io.github.trae.hytale.framework.command.suggestion.abstracts.AbstractSuggestion;

import java.util.List;
import java.util.function.Supplier;

/**
 * An {@link AbstractSuggestion} that represents an optional command argument.
 *
 * <p>Fixes the {@code required} flag to {@code false}, causing the argument to be
 * registered via the engine's optional-argument path.</p>
 */
public class OptionalSuggestion extends AbstractSuggestion {

    /**
     * Creates an optional suggestion.
     *
     * @param name            the human-readable display name (e.g. {@code "Player Name"})
     * @param usage           the usage placeholder shown in command syntax (e.g. {@code "playerName"})
     * @param description     the human-readable description of the argument
     * @param contentSupplier supplies the current list of completion candidates on demand
     */
    public OptionalSuggestion(final String name, final String usage, final String description, final Supplier<List<String>> contentSupplier) {
        super(name, usage, description, false, contentSupplier);
    }
}