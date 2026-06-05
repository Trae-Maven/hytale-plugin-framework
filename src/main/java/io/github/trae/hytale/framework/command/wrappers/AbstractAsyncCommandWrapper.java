package io.github.trae.hytale.framework.command.wrappers;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import io.github.trae.hytale.framework.command.BaseSubCommand;
import io.github.trae.hytale.framework.command.interfaces.SharedBaseCommand;
import io.github.trae.hytale.framework.command.suggestion.Suggestion;
import io.github.trae.hytale.framework.utility.UtilArgument;
import io.github.trae.hytale.framework.utility.UtilWorld;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

/**
 * Asynchronous engine command wrapper that bridges a framework
 * {@link SharedBaseCommand} to the Hytale {@link AbstractAsyncCommand} system.
 *
 * <p>Behaves like {@link AbstractCommandWrapper}. When the sender is a {@link PlayerRef},
 * execution is marshalled onto that player's world thread; for all other senders it is
 * dispatched on a separate thread via {@link CompletableFuture#runAsync(Runnable)}.
 * Used when the wrapped command reports {@link SharedBaseCommand#isAsynchronous()} as
 * {@code true}.</p>
 */
public class AbstractAsyncCommandWrapper extends AbstractAsyncCommand {

    private final SharedBaseCommand<?> sharedBaseCommand;

    /**
     * Creates an asynchronous wrapper around the given framework command.
     *
     * @param sharedBaseCommand the framework command to wrap
     */
    public AbstractAsyncCommandWrapper(final SharedBaseCommand<?> sharedBaseCommand) {
        super(sharedBaseCommand.getLabel(), sharedBaseCommand.getDescription(), false);

        this.setAllowsExtraArguments(true);

        this.addAliases(sharedBaseCommand.getAliases().toArray(new String[0]));

        Suggestion.CONSUMER.accept(sharedBaseCommand, this);

        this.sharedBaseCommand = sharedBaseCommand;
    }

    /**
     * Disables automatic permission generation, deferring entirely to the wrapped
     * command's {@link SharedBaseCommand#hasPermission(CommandSender)}.
     *
     * @return always {@code false}
     */
    @Override
    protected boolean canGeneratePermission() {
        return false;
    }

    /**
     * Delegates the permission check to the wrapped command.
     *
     * @param sender the sender to check
     * @return {@code true} if the sender may execute the wrapped command
     */
    @Override
    public boolean hasPermission(@Nonnull final CommandSender sender) {
        return this.sharedBaseCommand.hasPermission(sender);
    }

    /**
     * Executes the wrapped command.
     *
     * <p>Strips the leading command/sub-command tokens from the context arguments —
     * two for a {@link BaseSubCommand}, one otherwise — before dispatching to
     * {@link SharedBaseCommand#_Execute(CommandSender, String[])}.</p>
     *
     * <p>If the sender is a {@link PlayerRef}, dispatch is marshalled onto the player's
     * world thread; otherwise it is run on a separate thread via
     * {@link CompletableFuture#runAsync(Runnable)}. The returned future completes once
     * that execution finishes.</p>
     *
     * @param commandContext the engine-provided command context
     * @return a future completing when the execution finishes
     */
    @Nonnull
    @Override
    protected CompletableFuture<Void> executeAsync(@Nonnull final CommandContext commandContext) {
        final CommandSender commandSender = commandContext.sender();

        final Runnable runnable = () -> this.sharedBaseCommand._Execute(commandSender, UtilArgument.getArguments(commandContext, this.sharedBaseCommand instanceof BaseSubCommand<?, ?, ?> ? 2 : 1));

        if (commandSender instanceof final PlayerRef playerRef) {
            final CompletableFuture<Void> completableFuture = new CompletableFuture<>();

            UtilWorld.getWorldByPlayerRef(playerRef).ifPresent(world -> world.execute(() -> {
                runnable.run();

                completableFuture.complete(null);
            }));

            return completableFuture;
        }

        return CompletableFuture.runAsync(runnable);
    }
}