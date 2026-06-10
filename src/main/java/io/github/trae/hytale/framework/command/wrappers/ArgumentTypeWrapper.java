package io.github.trae.hytale.framework.command.wrappers;

import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.ParseResult;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgumentType;
import com.hypixel.hytale.server.core.command.system.suggestion.SuggestionResult;
import io.github.trae.hytale.framework.command.events.CommandRequestSuggestionsEvent;
import io.github.trae.hytale.framework.command.interfaces.SharedBaseCommand;
import io.github.trae.hytale.framework.command.suggestion.Suggestion;
import io.github.trae.hytale.framework.utility.UtilEvent;
import lombok.Getter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Locale;

/**
 * Engine {@link ArgumentType} adapting a framework {@link Suggestion} into a single-token
 * string argument with live, sender-aware tab-completion.
 *
 * <p>Parsing returns the raw entered token verbatim — the framework reads the raw
 * {@code String[]} in its own execution logic, so the parsed value is never consumed.
 * Completions are produced by applying the suggestion's content function to the
 * requesting sender, filtered case-insensitively by the partial text already entered.</p>
 *
 * <p>This {@code suggest} method is invoked server-side by the engine when the client
 * sends an {@code ArgValuesRequest} for this argument's suggestion type id. A single
 * instance is shared across every usage variant that declares its slot, so the slot is
 * registered under one stable suggestion type id rather than a fresh one per variant.</p>
 */
@Getter
public class ArgumentTypeWrapper extends ArgumentType<String> {

    private final SharedBaseCommand<?> sharedBaseCommand;
    private final Suggestion suggestion;

    /**
     * Creates a wrapper around the given suggestion, consuming a single token.
     *
     * @param sharedBaseCommand the command this argument belongs to
     * @param suggestion        the suggestion backing this argument type
     */
    public ArgumentTypeWrapper(final SharedBaseCommand<?> sharedBaseCommand, final Suggestion suggestion) {
        super(suggestion.getName(), suggestion.getUsage(), 1);

        this.sharedBaseCommand = sharedBaseCommand;
        this.suggestion = suggestion;
    }

    /**
     * Returns the first (and only) input token unchanged.
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
     * requesting sender, keeping only candidates whose prefix matches the entered text
     * case-insensitively.
     *
     * @param sender             the sender requesting suggestions
     * @param textAlreadyEntered the partial text typed so far for this argument
     * @param numParametersTyped the number of parameters typed so far
     * @param result             the suggestion result accumulator to populate
     */
    @Override
    public void suggest(@Nonnull final CommandSender sender, @Nonnull final String textAlreadyEntered, final int numParametersTyped, @Nonnull final SuggestionResult result) {
        if (!(this.sharedBaseCommand.getClassOfCommandSender().isInstance(sender))) {
            return;
        }

        if (!(this.sharedBaseCommand.hasPermission(sender))) {
            return;
        }

        if (UtilEvent.supply(new CommandRequestSuggestionsEvent(this.sharedBaseCommand, sender)).isCancelled()) {
            return;
        }

        final String prefix = textAlreadyEntered.toLowerCase(Locale.ROOT);

        for (final String candidate : this.suggestion.getContentFunction().apply(sender)) {
            if (candidate == null) {
                continue;
            }

            if (!(prefix.isEmpty()) && !(candidate.toLowerCase(Locale.ROOT).startsWith(prefix))) {
                continue;
            }

            result.suggest(candidate);
        }
    }
}