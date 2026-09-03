package io.github.trae.hytale.framework.utility.search.types;

import io.github.trae.hytale.framework.HytalePlugin;
import io.github.trae.hytale.framework.utility.UtilColor;
import io.github.trae.hytale.framework.utility.UtilPlugin;
import io.github.trae.hytale.framework.utility.enums.ChatColor;
import io.github.trae.hytale.framework.utility.search.HytaleSearchEngine;

import java.util.Locale;

/**
 * Search engine resolving framework plugins registered with {@link UtilPlugin}.
 *
 * <p>Candidates are read live from the internal plugin registry on every search, and matched on
 * {@link HytalePlugin#getPluginName()}: case-insensitive equality for an exact hit, case-insensitive
 * substring for a partial one. Note that the registry itself is keyed by class simple name, so this
 * matches on a different string than {@link UtilPlugin#getInternalPluginByName(String)}.</p>
 */
public class InternalPluginSearchEngine extends HytaleSearchEngine<HytalePlugin> {

    /**
     * Creates a search engine over the internal plugin registry.
     */
    public InternalPluginSearchEngine() {
        super("Internal Plugin Search", UtilPlugin::getInternalPlugins);
    }

    /**
     * {@inheritDoc}
     *
     * @return the plugin name serialized in yellow
     */
    @Override
    protected String getTypeFormat(final HytalePlugin hytalePlugin) {
        return UtilColor.serialize(ChatColor.YELLOW.getColor(), hytalePlugin.getPluginName());
    }

    /**
     * {@inheritDoc}
     *
     * <p>Compares the plugin name to the input, ignoring case.
     */
    @Override
    protected boolean isExact(final HytalePlugin hytalePlugin, final String result) {
        return hytalePlugin.getPluginName().equalsIgnoreCase(result);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Tests whether the plugin name contains the input, ignoring case.
     */
    @Override
    protected boolean isMatching(final HytalePlugin hytalePlugin, final String result) {
        return hytalePlugin.getPluginName().toLowerCase(Locale.ROOT).contains(result.toLowerCase(Locale.ROOT));
    }
}