package io.github.trae.hytale.framework.utility;

import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import io.github.trae.hytale.framework.utility.search.types.BlockTypeSearchEngine;
import lombok.experimental.UtilityClass;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * Utility helpers for resolving {@link BlockType} assets from user-supplied input.
 *
 * <p>Wraps a shared {@link BlockTypeSearchEngine} so commands can turn a partial or full block id
 * into a {@link BlockType} without handling the match and feedback logic themselves. For a known
 * exact id, {@link BlockType#fromString(String)} is the direct lookup and skips the search
 * entirely.</p>
 */
@UtilityClass
public class UtilBlock {

    /**
     * Search engine backing the {@code searchBlockType} helpers.
     */
    private static final BlockTypeSearchEngine BLOCK_TYPE_SEARCH_ENGINE = new BlockTypeSearchEngine();

    /**
     * Searches the loaded block types for the block identified by the given input.
     *
     * <p>An exact id match wins immediately, otherwise a single partial match is returned. An empty
     * or ambiguous search yields an empty result and, when informing is enabled, messages the sender
     * with the outcome.</p>
     *
     * @param sender    the sender to inform of the search outcome
     * @param input     the search input, matched against the block's asset id
     * @param inform    whether to message the sender when the search fails to resolve
     * @param predicate an optional filter applied before matching, or null to consider every block
     * @return the resolved block type, or {@link Optional#empty()} if the search was empty or
     * ambiguous
     */
    public static Optional<BlockType> searchBlockType(final CommandSender sender, final String input, final boolean inform, final Predicate<BlockType> predicate) {
        return BLOCK_TYPE_SEARCH_ENGINE.find(
                sender,
                input,
                inform,
                predicate
        );
    }

    /**
     * Searches the loaded block types for the block identified by the given input, without filtering.
     *
     * @param sender the sender to inform of the search outcome
     * @param input  the search input, matched against the block's asset id
     * @param inform whether to message the sender when the search fails to resolve
     * @return the resolved block type, or {@link Optional#empty()} if the search was empty or
     * ambiguous
     * @see #searchBlockType(CommandSender, String, boolean, Predicate)
     */
    public static Optional<BlockType> searchBlockType(final CommandSender sender, final String input, final boolean inform) {
        return searchBlockType(sender, input, inform, null);
    }
}