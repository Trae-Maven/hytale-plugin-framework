package io.github.trae.hytale.framework.utility;

import com.hypixel.hytale.common.plugin.PluginIdentifier;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.plugin.PluginBase;
import io.github.trae.hytale.framework.HytalePlugin;
import io.github.trae.utilities.UtilJava;
import io.github.trae.utilities.UtilString;
import lombok.experimental.UtilityClass;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Utility class for managing and querying plugins within the Hytale server environment.
 *
 * <p>Supports two categories of plugin lookup:</p>
 * <ul>
 *   <li><b>External plugins</b> — registered through Hytale's native
 *       {@link com.hypixel.hytale.server.core.plugin.PluginManager}, queried by
 *       {@link PluginIdentifier}.</li>
 *   <li><b>Internal plugins</b> — instances of {@link HytalePlugin} managed by the
 *       framework, stored in an insertion-ordered map keyed by uppercase class simple name.</li>
 * </ul>
 */
@UtilityClass
public class UtilPlugin {

    /**
     * Insertion-ordered registry of internal {@link HytalePlugin} instances, keyed by uppercase simple class name.
     */
    private static final LinkedHashMap<String, HytalePlugin> INTERNAL_PLUGINS = new LinkedHashMap<>();

    /**
     * Returns all plugins currently loaded by Hytale's plugin manager.
     *
     * @return an unmodifiable list of all loaded {@link PluginBase} instances
     */
    public static List<PluginBase> getPlugins() {
        return HytaleServer.get().getPluginManager().getPlugins();
    }

    /**
     * Retrieves a plugin by its {@link PluginIdentifier} string.
     *
     * @param identifier the plugin identifier string (e.g. {@code "namespace:plugin-name"})
     * @return an {@link Optional} containing the plugin if found, or empty if not present
     * @throws IllegalArgumentException if {@code identifier} is {@code null} or empty
     */
    public static Optional<PluginBase> getPluginByIdentifier(final String identifier) {
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
    public static boolean isPluginByIdentifier(final String identifier) {
        if (UtilString.isEmpty(identifier)) {
            throw new IllegalArgumentException("Identifier cannot be null.");
        }

        return getPluginByIdentifier(identifier).isPresent();
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
    public static void registerInternalPlugin(final HytalePlugin hytalePlugin) {
        if (hytalePlugin == null) {
            throw new IllegalArgumentException("HytalePlugin cannot be null.");
        }

        INTERNAL_PLUGINS.putIfAbsent(hytalePlugin.getClass().getSimpleName().toUpperCase(Locale.ROOT), hytalePlugin);
    }

    /**
     * Returns an immutable snapshot of all registered internal plugins.
     *
     * @return an unmodifiable list of all {@link HytalePlugin} instances
     */
    public static List<HytalePlugin> getInternalPlugins() {
        return List.copyOf(INTERNAL_PLUGINS.values());
    }

    /**
     * Retrieves an internal plugin by name.
     *
     * <p>The lookup is case-insensitive; the name is converted to uppercase
     * via {@link Locale#ROOT} before matching.</p>
     *
     * @param name the plugin name (typically the simple class name)
     * @return the matching {@link HytalePlugin}, or {@code null} if not found
     * @throws IllegalArgumentException if {@code name} is {@code null} or empty
     */
    public static HytalePlugin getInternalPluginByName(final String name) {
        if (UtilString.isEmpty(name)) {
            throw new IllegalArgumentException("Name cannot be null.");
        }

        return INTERNAL_PLUGINS.get(name.toUpperCase(Locale.ROOT));
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

        return UtilJava.cast(clazz, getInternalPluginByName(clazz.getSimpleName().toUpperCase(Locale.ROOT)));
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

        return INTERNAL_PLUGINS.containsKey(name.toUpperCase(Locale.ROOT));
    }
}