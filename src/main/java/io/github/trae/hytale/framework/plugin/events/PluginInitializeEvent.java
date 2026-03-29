package io.github.trae.hytale.framework.plugin.events;

import io.github.trae.hytale.framework.HytalePlugin;
import io.github.trae.hytale.framework.event.types.CustomEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Fired after a {@link HytalePlugin} has completed initialization.
 *
 * <p>Dispatched at the end of {@link HytalePlugin#initializePlugin()} after
 * all components have been discovered, wired, and initialized through the
 * hierarchy lifecycle, and all queued commands have been bulk-registered.
 * Listeners can use this event to perform post-startup logic that depends
 * on the plugin being fully ready.</p>
 */
@AllArgsConstructor
@Getter
public class PluginInitializeEvent extends CustomEvent {

    /**
     * The plugin that has finished initializing.
     */
    private final HytalePlugin plugin;
}