package io.github.trae.hytale.framework.command.events;

import com.hypixel.hytale.server.core.command.system.CommandSender;
import io.github.trae.hytale.framework.command.interfaces.SharedBaseCommand;
import io.github.trae.hytale.framework.event.types.CustomCancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CommandRequestSuggestionsEvent extends CustomCancellableEvent {

    private final SharedBaseCommand<?> command;

    private final CommandSender sender;
}