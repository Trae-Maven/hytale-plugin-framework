package io.github.trae.hytale.framework.utility.search.types;

import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import io.github.trae.hytale.framework.utility.UtilColor;
import io.github.trae.hytale.framework.utility.enums.ChatColor;
import io.github.trae.hytale.framework.utility.search.HytaleSearchEngine;

import java.util.List;
import java.util.Locale;

/**
 * Search engine resolving {@link BlockType} assets from the block asset map.
 *
 * <p>Blocks are data-driven assets rather than enum constants, so candidates are read live from
 * {@link BlockType#getAssetMap()} on every search and custom blocks contributed by other asset packs
 * are included. Matching runs against {@link BlockType#getId()}, the underscore-separated string key
 * the asset map is keyed on, such as {@code Rock_Stone}.</p>
 */
public class BlockTypeSearchEngine extends HytaleSearchEngine<BlockType> {

    /**
     * Creates a search engine over the loaded block types.
     */
    public BlockTypeSearchEngine() {
        super("Block Search", () -> List.copyOf(BlockType.getAssetMap().getAssetMap().values()));
    }

    /**
     * {@inheritDoc}
     *
     * @return the block's asset id serialized in yellow
     */
    @Override
    protected String getTypeFormat(final BlockType blockType) {
        return UtilColor.serialize(ChatColor.YELLOW.getColor(), blockType.getId());
    }

    /**
     * {@inheritDoc}
     *
     * <p>Compares the block's asset id to the input, ignoring case.
     */
    @Override
    protected boolean isExact(final BlockType blockType, final String result) {
        return blockType.getId().equalsIgnoreCase(result);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Tests whether the block's asset id contains the input, ignoring case.
     */
    @Override
    protected boolean isMatching(final BlockType blockType, final String result) {
        return blockType.getId().toLowerCase(Locale.ROOT).contains(result.toLowerCase(Locale.ROOT));
    }
}