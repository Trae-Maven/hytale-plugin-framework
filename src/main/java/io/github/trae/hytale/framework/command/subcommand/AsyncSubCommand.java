package io.github.trae.hytale.framework.command.subcommand;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.World;
import io.github.trae.hf.Module;
import io.github.trae.hf.SubModule;
import io.github.trae.hytale.framework.HytalePlugin;
import io.github.trae.hytale.framework.command.settings.CommandSettings;
import io.github.trae.hytale.framework.utility.UtilArgument;
import lombok.Getter;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

/**
 * Base asynchronous subcommand that runs execution off the main thread via
 * {@link CompletableFuture#runAsync(Runnable)}, skipping the first two input tokens.
 *
 * @param <BasePlugin> the plugin type
 * @param <BaseModule> the parent command module this subcommand belongs to
 */
@Getter
public abstract class AsyncSubCommand<BasePlugin extends HytalePlugin, BaseModule extends Module<BasePlugin, ?>> extends AbstractAsyncCommand implements SubModule<BasePlugin, BaseModule> {

    private final Object requiredPermission;

    public AsyncSubCommand(final String name, final String description, final Object requiredPermission, final boolean requiresConfirmation) {
        super(name, description, requiresConfirmation);

        this.requiredPermission = requiredPermission;

        // Override Hytale default fallback message for unrecognised arguments
        this.setAllowsExtraArguments(true);
    }

    public AsyncSubCommand(final String name, final String description, final Object requiredPermission) {
        this(name, description, requiredPermission, false);
    }

    @Override
    protected boolean canGeneratePermission() {
        return false;
    }

    @Nonnull
    @Override
    protected CompletableFuture<Void> executeAsync(@Nonnull final CommandContext commandContext) {
        final CommandSender sender = commandContext.sender();

        if (sender instanceof final Player player) {
            final World world = player.getWorld();
            if (world != null) {
                final CompletableFuture<Void> future = new CompletableFuture<>();
                world.execute(() -> {
                    if (CommandSettings.getPermissionCheckPredicate().test(sender, this.getRequiredPermission(), true)) {
                        this.execute(sender, UtilArgument.getArguments(commandContext, 2));
                    }
                    future.complete(null);
                });
                return future;
            }
        }

        return CompletableFuture.runAsync(() -> {
            if (CommandSettings.getPermissionCheckPredicate().test(sender, this.getRequiredPermission(), true)) {
                this.execute(sender, UtilArgument.getArguments(commandContext, 2));
            }
        });
    }

    /**
     * Called asynchronously when the subcommand is executed.
     *
     * @param sender the entity or console that ran the command
     * @param args   the arguments following the subcommand name
     */
    public abstract void execute(final CommandSender sender, final String[] args);
}