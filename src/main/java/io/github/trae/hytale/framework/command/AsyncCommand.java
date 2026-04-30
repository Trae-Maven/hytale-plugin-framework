package io.github.trae.hytale.framework.command;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import io.github.trae.hf.Manager;
import io.github.trae.hf.Module;
import io.github.trae.hytale.framework.HytalePlugin;
import io.github.trae.hytale.framework.command.settings.CommandSettings;
import io.github.trae.hytale.framework.utility.UtilArgument;
import lombok.Getter;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

/**
 * Base asynchronous command that runs execution off the main thread via
 * {@link CompletableFuture#runAsync(Runnable)}.
 *
 * @param <BasePlugin>  the plugin type
 * @param <BaseManager> the manager this command belongs to
 */
@Getter
public abstract class AsyncCommand<BasePlugin extends HytalePlugin, BaseManager extends Manager<BasePlugin>> extends AbstractAsyncCommand implements Module<BasePlugin, BaseManager> {

    private final Object requiredPermission;

    public AsyncCommand(final String name, final String description, final Object requiredPermission, final boolean requiresConfirmation) {
        super(name, description, requiresConfirmation);

        this.requiredPermission = requiredPermission;

        // Override Hytale default fallback message for unrecognised arguments
        this.setAllowsExtraArguments(true);
    }

    public AsyncCommand(final String name, final String description, final Object requiredPermission) {
        this(name, description, requiredPermission, false);
    }

    @Override
    protected boolean canGeneratePermission() {
        return false;
    }

    @Nonnull
    @Override
    protected CompletableFuture<Void> executeAsync(@Nonnull final CommandContext commandContext) {
        return CompletableFuture.runAsync(() -> {
            final CommandSender sender = commandContext.sender();

            if (CommandSettings.getPermissionCheckPredicate().test(sender, this.getRequiredPermission(), true)) {
                this.execute(sender, UtilArgument.getArguments(commandContext, 1));
            }
        });
    }

    /**
     * Called asynchronously when the command is executed.
     *
     * @param sender the entity or console that ran the command
     * @param args   the arguments following the command name
     */
    public abstract void execute(final CommandSender sender, final String[] args);
}