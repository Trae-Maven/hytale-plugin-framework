package io.github.trae.hytale.framework.command.events;

import com.hypixel.hytale.server.core.command.system.CommandSender;
import io.github.trae.hytale.framework.command.interfaces.SharedBaseCommand;
import io.github.trae.hytale.framework.event.types.CustomCancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Cancellable event fired immediately before a framework command's typed
 * {@link SharedBaseCommand#execute(CommandSender, String[])} logic runs.
 *
 * <p>Dispatched from {@link SharedBaseCommand#_Execute(CommandSender, String[])}
 * after sender-type and permission validation but before any
 * {@link io.github.trae.hytale.framework.command.impl.Confirmable} gating. Cancelling
 * this event aborts execution of the command.</p>
 */
@AllArgsConstructor
@Getter
public class CommandExecuteEvent extends CustomCancellableEvent {

    /**
     * The framework command about to be executed.
     */
    private final SharedBaseCommand<?> command;

    /**
     * The sender that triggered the command.
     */
    private final CommandSender sender;

    /**
     * The arguments supplied to the command, with leading command/sub-command
     * tokens already stripped.
     */
    private final String[] args;
}