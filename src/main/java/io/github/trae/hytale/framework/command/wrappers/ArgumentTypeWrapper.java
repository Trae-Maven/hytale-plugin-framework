package io.github.trae.hytale.framework.command.wrappers;

import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.ParseResult;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgumentType;
import com.hypixel.hytale.server.core.command.system.suggestion.SuggestionResult;
import io.github.trae.hytale.framework.command.suggestion.abstracts.AbstractSuggestion;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Engine {@link ArgumentType} that adapts a framework {@link AbstractSuggestion} into a
 * single-token string argument with dynamic tab-completion.
 *
 * <p>Parsing returns the raw entered token verbatim; examples and live suggestions are
 * drawn from the suggestion's {@link AbstractSuggestion#getContentSupplier() content
 * supplier}, with suggestions filtered by a case-insensitive prefix match against the
 * text already entered.</p>
 */
public class ArgumentTypeWrapper extends ArgumentType<String> {

    private final AbstractSuggestion suggestion;

    /**
     * Creates a wrapper around the given suggestion.
     *
     * <p>The argument's name and usage are taken from the suggestion and it consumes a
     * single token.</p>
     *
     * @param suggestion the suggestion backing this argument type
     */
    public ArgumentTypeWrapper(final AbstractSuggestion suggestion) {
        super(suggestion.getName(), suggestion.getUsage(), 1);

        this.suggestion = suggestion;
    }

    /**
     * Returns up to five example values drawn from the suggestion's content supplier.
     *
     * @return an array of example completion values, capped at five
     */
    @Nonnull
    @Override
    public String[] getExamples() {
        return this.suggestion.getContentSupplier().get().stream().limit(5).toArray(String[]::new);
    }

    /**
     * Parses the argument by returning the first (and only) input token unchanged.
     *
     * @param input       the token(s) supplied for this argument
     * @param parseResult the engine parse result accumulator
     * @return the raw entered token
     */
    @Nullable
    @Override
    public String parse(@Nonnull final String[] input, @Nonnull final ParseResult parseResult) {
        return input[0];
    }

    /**
     * Offers tab-completions from the suggestion's content supplier, keeping only
     * candidates whose prefix matches the already-entered text case-insensitively.
     *
     * @param sender             the sender requesting suggestions
     * @param textAlreadyEntered the partial text typed so far for this argument
     * @param numParametersTyped the number of parameters typed so far
     * @param result             the suggestion result accumulator to populate
     */
    @Override
    public void suggest(@Nonnull final CommandSender sender, @Nonnull final String textAlreadyEntered, final int numParametersTyped, @Nonnull final SuggestionResult result) {
        for (final String string : this.suggestion.getContentSupplier().get()) {
            if (!(string.regionMatches(true, 0, textAlreadyEntered, 0, Math.min(string.length(), textAlreadyEntered.length())))) {
                continue;
            }

            result.suggest(string);
        }
    }
}