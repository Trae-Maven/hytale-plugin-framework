package io.github.trae.hytale.framework.sidebar.interfaces;

import com.hypixel.hytale.server.core.Message;

public interface ISidebar {

    void setLine(final int index, final Message line);

    void removeLine(final int index);

    void display();

    void update();

    void reset();
}