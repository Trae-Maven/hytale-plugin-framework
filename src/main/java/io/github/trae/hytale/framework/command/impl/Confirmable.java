package io.github.trae.hytale.framework.command.impl;

import com.hypixel.hytale.server.core.command.system.CommandSender;
import io.github.trae.di.InjectorApi;
import io.github.trae.hytale.framework.command.service.ConfirmableService;
import io.github.trae.hytale.framework.utility.UtilMessage;

import java.time.Duration;

public interface Confirmable {

    default boolean isPreExecuteConfirmCheck() {
        return true;
    }

    default boolean isConfirmable(final CommandSender commandSender) {
        return true;
    }

    default long getConfirmationExpiry() {
        return Duration.ofMinutes(1).toMillis();
    }

    default void sendConfirmationMessage(final CommandSender commandSender) {
        UtilMessage.message(commandSender, "Command", "<red>Run the command again to confirm!</red>");
    }

    default boolean hasConfirmed(final CommandSender commandSender) {
        if (!(this.isConfirmable(commandSender))) {
            return true;
        }

        final ConfirmableService confirmableService = InjectorApi.get(ConfirmableService.class);

        if (!(confirmableService.contains(commandSender, this))) {
            confirmableService.put(commandSender, this);
            this.sendConfirmationMessage(commandSender);
            return false;
        }

        confirmableService.remove(commandSender, this);

        return true;
    }
}