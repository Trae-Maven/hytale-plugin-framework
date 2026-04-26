package io.github.trae.hytale.framework.utility;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import io.github.trae.utilities.UtilString;
import lombok.experimental.UtilityClass;

import java.util.Arrays;

/**
 * Utility for parsing command arguments from a {@link CommandContext}.
 */
@UtilityClass
public class UtilArgument {

    /**
     * Extracts arguments from the command input string, starting from the given index.
     * <p>
     * Splits the input on whitespace, collapsing consecutive spaces/tabs.
     * Returns an empty array if the input is empty or the start index exceeds
     * the number of available arguments.
     *
     * @param commandContext the command context containing the input string
     * @param startIndex     the index of the first argument to include (0-based)
     * @return the arguments from {@code startIndex} onwards, or an empty array
     */
    public static String[] getArguments(final CommandContext commandContext, final int startIndex) {
        final String inputString = commandContext.getInputString();
        if (UtilString.isEmpty(inputString)) {
            return new String[0];
        }

        final String[] parts = inputString.trim().split("\\s+");
        if (startIndex >= parts.length) {
            return new String[0];
        }

        return Arrays.copyOfRange(parts, startIndex, parts.length);
    }
}