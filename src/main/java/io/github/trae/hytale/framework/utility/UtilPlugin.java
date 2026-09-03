package io.github.trae.hytale.framework.utility;

import com.hypixel.hytale.common.plugin.PluginIdentifier;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.plugin.PluginBase;
import io.github.trae.hytale.framework.HytalePlugin;
import io.github.trae.hytale.framework.utility.search.types.InternalPluginSearchEngine;
import io.github.trae.utilities.UtilJava;
import io.github.trae.utilities.UtilString;
import lombok.experimental.UtilityClass;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Utility class for managing and querying plugins within the Hytale server environment.
 *
 * <p>Supports two categories of plugin lookup:</p>
 * <ul>
 *   <li><b>External plugins</b>: registered through Hytale's native
 *       {@link com.hypixel.hytale.server.core.plugin.PluginManager}, listed in full or queried by
 *       {@link PluginIdentifier}.</li>
 *   <li><b>Internal plugins</b>: instances of {@link HytalePlugin} managed by the
 *       framework, stored in an insertion-ordered map keyed by uppercase class simple name.</li>
 * </ul>
 *
 * <p>Internal plugins additionally support name-style searching through
 * {@link InternalPluginSearchEngine}, where an exact match wins immediately, a single partial match
 * is accepted, and an empty or ambiguous search optionally reports the outcome to the sender.</p>
 */
@UtilityClass
public class UtilPlugin {

    /**
     * Insertion-ordered registry of internal {@link HytalePlugin} instances, keyed by uppercase simple class name.
     */
    private static final LinkedHashMap<String, HytalePlugin> internalPluginMap = new LinkedHashMap<>();

    /**
     * Search engine used to resolve plugin names for
     * {@link #searchInternalPlugin(CommandSender, String, boolean, Predicate)}.
     */
    private static final InternalPluginSearchEngine INTERNAL_PLUGIN_SEARCH_ENGINE = new InternalPluginSearchEngine();

    /**
     * Returns all plugins currently loaded by Hytale's plugin manager.
     *
     * @return an unmodifiable list of all loaded {@link PluginBase} instances
     */
    public static List<PluginBase> getExternalPlugins() {
        return HytaleServer.get().getPluginManager().getPlugins();
    }

    /**
     * Retrieves a plugin by its {@link PluginIdentifier} string.
     *
     * @param identifier the plugin identifier string (e.g. {@code "namespace:plugin-name"})
     * @return an {@link Optional} containing the plugin if found, or empty if not present
     * @throws IllegalArgumentException if {@code identifier} is {@code null} or empty
     */
    public static Optional<PluginBase> getExternalPluginByIdentifier(final String identifier) {
        if (UtilString.isEmpty(identifier)) {
            throw new IllegalArgumentException("Identifier cannot be null.");
        }

        return Optional.ofNullable(HytaleServer.get().getPluginManager().getPlugin(PluginIdentifier.fromString(identifier)));
    }

    /**
     * Checks whether a plugin with the given {@link PluginIdentifier} string is currently loaded.
     *
     * @param identifier the plugin identifier string
     * @return {@code true} if the plugin is loaded, {@code false} otherwise
     * @throws IllegalArgumentException if {@code identifier} is {@code null} or empty
     */
    public static boolean isExternalPluginByIdentifier(final String identifier) {
        return getExternalPluginByIdentifier(identifier).isPresent();
    }

    /**
     * Returns an immutable snapshot of all registered internal plugins.
     *
     * @return an unmodifiable list of all {@link HytalePlugin} instances
     */
    public static List<HytalePlugin> getInternalPlugins() {
        return List.copyOf(internalPluginMap.values());
    }

    /**
     * Registers an internal {@link HytalePlugin} in the framework registry.
     *
     * <p>The plugin is keyed by its uppercase simple class name. If a plugin with the
     * same key is already registered, this call is a no-op ({@code putIfAbsent}).</p>
     *
     * @param hytalePlugin the plugin instance to register
     * @throws IllegalArgumentException if {@code hytalePlugin} is {@code null}
     */
    public static void addInternalPlugin(final HytalePlugin hytalePlugin) {
        if (hytalePlugin == null) {
            throw new IllegalArgumentException("HytalePlugin cannot be null.");
        }

        internalPluginMap.putIfAbsent(hytalePlugin.getClass().getSimpleName().toUpperCase(Locale.ROOT), hytalePlugin);
    }

    /**
     * Removes an internal {@link HytalePlugin} from the framework registry.
     *
     * <p>The entry is resolved by the plugin's uppercase simple class name. If no plugin is
     * registered under that key, this call is a no-op.</p>
     *
     * @param hytalePlugin the plugin instance to remove
     * @throws IllegalArgumentException if {@code hytalePlugin} is {@code null}
     */
    public static void removeInternalPlugin(final HytalePlugin hytalePlugin) {
        if (hytalePlugin == null) {
            throw new IllegalArgumentException("HytalePlugin cannot be null.");
        }

        internalPluginMap.remove(hytalePlugin.getClass().getSimpleName().toUpperCase(Locale.ROOT));
    }

    /**
     * Retrieves an internal plugin by name.
     *
     * <p>The lookup is case-insensitive; the name is converted to uppercase
     * via {@link Locale#ROOT} before matching. Note that the registry is keyed by simple class name,
     * not by {@link HytalePlugin#getPluginName()}, so this resolves the former.</p>
     *
     * @param name the plugin name (typically the simple class name)
     * @return the matching {@link HytalePlugin}, or {@code null} if not found
     * @throws IllegalArgumentException if {@code name} is {@code null} or empty
     */
    public static HytalePlugin getInternalPluginByName(final String name) {
        if (UtilString.isEmpty(name)) {
            throw new IllegalArgumentException("Name cannot be null.");
        }

        return internalPluginMap.get(name.toUpperCase(Locale.ROOT));
    }

    /**
     * Retrieves an internal plugin by its class type.
     *
     * <p>Resolves the plugin by its simple class name, then casts it to the
     * requested type using {@link UtilJava#cast(Class, Object)}.</p>
     *
     * @param clazz    the expected plugin class
     * @param <Plugin> the plugin type, extending {@link HytalePlugin}
     * @return the plugin instance cast to the requested type, or {@code null} if not found
     * @throws IllegalArgumentException if {@code clazz} is {@code null}
     */
    public static <Plugin extends HytalePlugin> Plugin getInternalPluginByClass(final Class<Plugin> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("Clazz cannot be null.");
        }

        return UtilJava.cast(clazz, getInternalPluginByName(clazz.getSimpleName()));
    }

    /**
     * Checks whether an internal plugin with the given name is registered.
     *
     * <p>The lookup is case-insensitive.</p>
     *
     * @param name the plugin name to check
     * @return {@code true} if a plugin with the given name is registered, {@code false} otherwise
     * @throws IllegalArgumentException if {@code name} is {@code null} or empty
     */
    public static boolean isInternalPluginByName(final String name) {
        if (UtilString.isEmpty(name)) {
            throw new IllegalArgumentException("Name cannot be null.");
        }

        return internalPluginMap.containsKey(name.toUpperCase(Locale.ROOT));
    }

    /**
     * Searches the internal registry for the plugin identified by the given input.
     *
     * <p>Matching runs against {@link HytalePlugin#getPluginName()}, not the class simple name the
     * registry is keyed by. An exact name match wins immediately, otherwise a single partial match is
     * returned. An empty or ambiguous search yields an empty result and, when informing is enabled,
     * messages the sender with the outcome.</p>
     *
     * @param sender    the sender to inform of the search outcome
     * @param input     the search input
     * @param inform    whether to message the sender when the search fails to resolve
     * @param predicate an optional filter applied before matching, or null to consider every plugin
     * @return the resolved plugin, or {@link Optional#empty()} if the search was empty or ambiguous
     */
    public static Optional<HytalePlugin> searchInternalPlugin(final CommandSender sender, final String input, final boolean inform, final Predicate<HytalePlugin> predicate) {
        return INTERNAL_PLUGIN_SEARCH_ENGINE.find(
                sender,
                input,
                inform,
                predicate
        );
    }

    /**
     * Searches the internal registry for the plugin identified by the given input, without filtering.
     *
     * @param sender the sender to inform of the search outcome
     * @param input  the search input
     * @param inform whether to message the sender when the search fails to resolve
     * @return the resolved plugin, or {@link Optional#empty()} if the search was empty or ambiguous
     * @see #searchInternalPlugin(CommandSender, String, boolean, Predicate)
     */
    public static Optional<HytalePlugin> searchInternalPlugin(final CommandSender sender, final String input, final boolean inform) {
        return searchInternalPlugin(sender, input, inform, null);
    }
}