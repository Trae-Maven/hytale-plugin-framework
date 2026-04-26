package io.github.trae.hytale.framework.command.subcommand;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import io.github.trae.hf.Module;
import io.github.trae.hf.SubModule;
import io.github.trae.hytale.framework.HytalePlugin;
import io.github.trae.hytale.framework.utility.UtilArgument;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

/**
 * Base asynchronous subcommand that runs execution off the main thread via
 * {@link CompletableFuture#runAsync(Runnable)}, skipping the first two input tokens.
 *
 * @param <BasePlugin> the plugin type
 * @param <BaseModule> the parent command module this subcommand belongs to
 */
public abstract class AsyncSubCommand<BasePlugin extends HytalePlugin, BaseModule extends Module<BasePlugin, ?>> extends AbstractAsyncCommand implements SubModule<BasePlugin, BaseModule> {

    public AsyncSubCommand(final String name, final String description, final boolean requiresConfirmation) {
        super(name, description, requiresConfirmation);

        // Override Hytale default fallback message for unrecognised arguments
        this.setAllowsExtraArguments(true);
    }

    public AsyncSubCommand(final String name, final String description) {
        this(name, description, false);
    }

    @Nonnull
    @Override
    protected CompletableFuture<Void> executeAsync(@Nonnull final CommandContext commandContext) {
        return CompletableFuture.runAsync(() -> this.execute(commandContext.sender(), UtilArgument.getArguments(commandContext, 2)));
    }

    /**
     * Called asynchronously when the subcommand is executed.
     *
     * @param sender the entity or console that ran the command
     * @param args   the arguments following the subcommand name
     */
    public abstract void execute(final CommandSender sender, final String[] args);
}