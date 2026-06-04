package io.github.trae.hytale.framework.command.impl;

import com.hypixel.hytale.server.core.command.system.CommandSender;
import io.github.trae.hytale.framework.utility.UtilMessage;

import java.time.Duration;

public interface Confirmable {

    default boolean isConfirmable(final CommandSender commandSender) {
        return true;
    }

    default long getExpiration() {
        return Duration.ofMinutes(1).toMillis();
    }

    default void sendConfirmationMessage(final CommandSender commandSender) {
        UtilMessage.message(commandSender, "Command", "Run the command again to confirm!");
    }
}