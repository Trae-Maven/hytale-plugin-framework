package io.github.trae.hytale.framework.command.wrappers;

import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.ParseResult;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgumentType;
import com.hypixel.hytale.server.core.command.system.suggestion.SuggestionResult;
import io.github.trae.hytale.framework.command.suggestion.Suggestion;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Engine {@link ArgumentType} that adapts a framework {@link Suggestion} into a
 * single-token string argument with dynamic tab-completion.
 *
 * <p>Parsing returns the raw entered token verbatim; live suggestions are produced by
 * applying the suggestion's {@link Suggestion#getContentFunction() content function}
 * to the requesting sender, then filtered by a case-insensitive prefix match against
 * the text already entered.</p>
 */
public class ArgumentTypeWrapper extends ArgumentType<String> {

    private final Suggestion suggestion;

    /**
     * Creates a wrapper around the given suggestion.
     *
     * <p>The argument's name and usage are taken from the suggestion and it consumes a
     * single token.</p>
     *
     * @param suggestion the suggestion backing this argument type
     */
    public ArgumentTypeWrapper(final Suggestion suggestion) {
        super(suggestion.getName(), suggestion.getUsage(), 1);

        this.suggestion = suggestion;
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
     * Offers tab-completions by applying the suggestion's content function to the
     * requesting sender, keeping only candidates whose prefix matches the already-entered
     * text case-insensitively.
     *
     * @param sender             the sender requesting suggestions, passed to the content function
     * @param textAlreadyEntered the partial text typed so far for this argument
     * @param numParametersTyped the number of parameters typed so far
     * @param result             the suggestion result accumulator to populate
     */
    @Override
    public void suggest(@Nonnull final CommandSender sender, @Nonnull final String textAlreadyEntered, final int numParametersTyped, @Nonnull final SuggestionResult result) {
        for (final String string : this.suggestion.getContentFunction().apply(sender)) {
            if (!(string.regionMatches(true, 0, textAlreadyEntered, 0, Math.min(string.length(), textAlreadyEntered.length())))) {
                continue;
            }

            result.suggest(string);
        }
    }
}