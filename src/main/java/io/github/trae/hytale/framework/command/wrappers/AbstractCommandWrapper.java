package io.github.trae.hytale.framework.command.wrappers;

import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import io.github.trae.hytale.framework.command.BaseSubCommand;
import io.github.trae.hytale.framework.command.interfaces.SharedBaseCommand;
import io.github.trae.hytale.framework.utility.UtilArgument;
import io.github.trae.hytale.framework.utility.UtilWorld;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

/**
 * Synchronous engine command wrapper that bridges a framework
 * {@link SharedBaseCommand} to the Hytale {@link AbstractCommand} system.
 *
 * <p>Delegates label, description, aliases, permission checks, and execution back to
 * the wrapped {@link SharedBaseCommand}, allowing framework commands to be registered
 * with the engine without extending engine types directly.</p>
 *
 * <p>When the sender is a {@link PlayerRef}, execution is marshalled onto that player's
 * world thread; otherwise it runs inline on the calling thread.</p>
 */
public class AbstractCommandWrapper extends AbstractCommand {

    private final SharedBaseCommand<?> sharedBaseCommand;

    /**
     * Creates a wrapper around the given framework command.
     *
     * @param sharedBaseCommand the framework command to wrap
     */
    public AbstractCommandWrapper(final SharedBaseCommand<?> sharedBaseCommand) {
        super(sharedBaseCommand.getLabel(), sharedBaseCommand.getDescription(), false);

        this.setAllowsExtraArguments(true);

        this.addAliases(sharedBaseCommand.getAliases().toArray(new String[0]));

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
     * world thread and the returned future completes once that execution finishes.
     * Otherwise the command runs inline and {@code null} is returned.</p>
     *
     * @param commandContext the engine-provided command context
     * @return a future completing when world-thread execution finishes, or {@code null}
     * if the command ran inline
     */
    @Nullable
    @Override
    protected CompletableFuture<Void> execute(@Nonnull final CommandContext commandContext) {
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

        runnable.run();

        return null;
    }
}