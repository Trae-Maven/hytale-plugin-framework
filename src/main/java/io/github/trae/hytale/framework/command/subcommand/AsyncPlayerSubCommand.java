package io.github.trae.hytale.framework.command.subcommand;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.trae.hf.Module;
import io.github.trae.hf.SubModule;
import io.github.trae.hytale.framework.HytalePlugin;
import io.github.trae.hytale.framework.command.settings.CommandSettings;
import io.github.trae.hytale.framework.utility.UtilArgument;
import lombok.Getter;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

/**
 * Base asynchronous player-only subcommand that runs execution off the main thread
 * via {@link CompletableFuture#runAsync(Runnable)}, skipping the first two input tokens.
 *
 * @param <BasePlugin> the plugin type
 * @param <BaseModule> the parent command module this subcommand belongs to
 */
@Getter
public abstract class AsyncPlayerSubCommand<BasePlugin extends HytalePlugin, BaseModule extends Module<BasePlugin, ?>> extends AbstractAsyncPlayerCommand implements SubModule<BasePlugin, BaseModule> {

    private final Object requiredPermission;

    public AsyncPlayerSubCommand(final String name, final String description, final Object requiredPermission, final boolean requiresConfirmation) {
        super(name, description, requiresConfirmation);

        this.requiredPermission = requiredPermission;

        // Override Hytale default fallback message for unrecognised arguments
        this.setAllowsExtraArguments(true);
    }

    public AsyncPlayerSubCommand(final String name, final String description, final Object requiredPermission) {
        this(name, description, requiredPermission, false);
    }

    @Nonnull
    @Override
    protected CompletableFuture<Void> executeAsync(@Nonnull final CommandContext commandContext, @Nonnull final Store<EntityStore> store, @Nonnull final Ref<EntityStore> ref, @Nonnull final PlayerRef playerRef, @Nonnull final World world) {
        return CompletableFuture.runAsync(() -> {
            if (CommandSettings.getPermissionCheckPredicate().test(commandContext.sender(), this.getRequiredPermission(), true)) {
                this.execute(playerRef, UtilArgument.getArguments(commandContext, 2));
            }
        });
    }

    /**
     * Called asynchronously when a player executes the subcommand.
     *
     * @param playerRef the player who ran the command
     * @param args      the arguments following the subcommand name
     */
    public abstract void execute(final PlayerRef playerRef, final String[] args);
}