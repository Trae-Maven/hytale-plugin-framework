package io.github.trae.hytale.framework.sidebar;

import com.hypixel.hytale.server.core.Message;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * Immutable data object representing a sidebar's content.
 *
 * <p>A {@code Sidebar} holds a title and an ordered list of lines, both as
 * {@link Message} objects supporting Hytale's full rich text formatting
 * (color, bold, italic, parameters, etc.).</p>
 *
 * <p>Instances are created by plugin listeners handling
 * {@link io.github.trae.hytale.framework.sidebar.events.SidebarCreateEvent}
 * and passed to the {@link AbstractSidebarManager} for rendering. The manager
 * diffs incoming sidebars against previously stored ones to determine which
 * title and line values need updating.</p>
 *
 * @see AbstractSidebarManager#update(com.hypixel.hytale.server.core.universe.PlayerRef, Sidebar)
 * @see io.github.trae.hytale.framework.sidebar.events.SidebarCreateEvent
 */
@AllArgsConstructor
@Getter
public class Sidebar {

    /**
     * The title displayed at the top of the sidebar.
     */
    private final Message title;

    /**
     * The ordered list of lines displayed below the title.
     */
    private final List<Message> lines;
}
