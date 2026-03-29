package io.github.trae.hytale.framework.plugin.events;

import io.github.trae.hytale.framework.HytalePlugin;
import io.github.trae.hytale.framework.event.types.CustomEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Fired before a {@link HytalePlugin} begins its shutdown sequence.
 *
 * <p>Dispatched at the start of {@link HytalePlugin#shutdownPlugin()} before
 * components are torn down through the hierarchy lifecycle. Listeners can use
 * this event to perform cleanup or persistence logic while the plugin's
 * services are still available.</p>
 */
@AllArgsConstructor
@Getter
public class PluginShutdownEvent extends CustomEvent {

    /**
     * The plugin that is about to shut down.
     */
    private final HytalePlugin plugin;
}