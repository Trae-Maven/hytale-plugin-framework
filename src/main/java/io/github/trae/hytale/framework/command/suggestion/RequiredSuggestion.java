package io.github.trae.hytale.framework.command.suggestion;

import io.github.trae.hytale.framework.command.suggestion.abstracts.AbstractSuggestion;

import java.util.List;
import java.util.function.Supplier;

/**
 * An {@link AbstractSuggestion} that represents a required command argument.
 *
 * <p>Fixes the {@code required} flag to {@code true}, causing the argument to be
 * registered via the engine's required-argument path.</p>
 */
public class RequiredSuggestion extends AbstractSuggestion {

    /**
     * Creates a required suggestion.
     *
     * @param name            the human-readable display name (e.g. {@code "Player Name"})
     * @param usage           the usage placeholder shown in command syntax (e.g. {@code "playerName"})
     * @param description     the human-readable description of the argument
     * @param contentSupplier supplies the current list of completion candidates on demand
     */
    public RequiredSuggestion(final String name, final String usage, final String description, final Supplier<List<String>> contentSupplier) {
        super(name, usage, description, true, contentSupplier);
    }
}