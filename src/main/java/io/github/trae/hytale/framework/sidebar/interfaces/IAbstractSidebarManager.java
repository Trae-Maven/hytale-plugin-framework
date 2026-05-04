package io.github.trae.hytale.framework.sidebar.interfaces;

import com.hypixel.hytale.server.core.universe.PlayerRef;
import io.github.trae.hytale.framework.sidebar.Sidebar;
import io.github.trae.hytale.framework.sidebar.events.SidebarCreateEvent;
import io.github.trae.hytale.framework.sidebar.events.SidebarUpdateEvent;

public interface IAbstractSidebarManager {

    void update(final PlayerRef playerRef, final Sidebar sidebar);

    void remove(final PlayerRef playerRef);

    boolean hasSidebar(final PlayerRef playerRef);

    void onSidebarCreateEvent(final SidebarCreateEvent event);

    void onSidebarUpdateEvent(final SidebarUpdateEvent event);
}