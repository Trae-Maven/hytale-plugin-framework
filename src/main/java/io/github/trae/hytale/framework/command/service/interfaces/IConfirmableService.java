package io.github.trae.hytale.framework.command.service.interfaces;

import com.hypixel.hytale.server.core.command.system.CommandSender;
import io.github.trae.hytale.framework.command.impl.Confirmable;

public interface IConfirmableService {

    void put(final CommandSender commandSender, final Confirmable confirmable);

    void remove(final CommandSender commandSender, final Confirmable confirmable);

    boolean contains(final CommandSender commandSender, final Confirmable confirmable);
}