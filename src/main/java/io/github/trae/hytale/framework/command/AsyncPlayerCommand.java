package io.github.trae.hytale.framework.command;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.trae.hf.Manager;
import io.github.trae.hf.Module;
import io.github.trae.hytale.framework.HytalePlugin;
import io.github.trae.hytale.framework.utility.UtilArgument;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

/**
 * Base asynchronous player-only command that runs execution off the main thread
 * via {@link CompletableFuture#runAsync(Runnable)}.
 *
 * @param <BasePlugin>  the plugin type
 * @param <BaseManager> the manager this command belongs to
 */
public abstract class AsyncPlayerCommand<BasePlugin extends HytalePlugin, BaseManager extends Manager<BasePlugin>> extends AbstractAsyncPlayerCommand implements Module<BasePlugin, BaseManager> {

    public AsyncPlayerCommand(final String name, final String description, final boolean requiresConfirmation) {
        super(name, description, requiresConfirmation);

        // Override Hytale default fallback message for unrecognised arguments
        this.setAllowsExtraArguments(true);
    }

    public AsyncPlayerCommand(final String name, final String description) {
        this(name, description, false);
    }

    @Nonnull
    @Override
    protected CompletableFuture<Void> executeAsync(@Nonnull final CommandContext commandContext, @Nonnull final Store<EntityStore> store, @Nonnull final Ref<EntityStore> ref, @Nonnull final PlayerRef playerRef, @Nonnull final World world) {
        return CompletableFuture.runAsync(() -> this.execute(playerRef, UtilArgument.getArguments(commandContext, 1)));
    }

    /**
     * Called asynchronously when a player executes the command.
     *
     * @param player the player who ran the command
     * @param args   the arguments following the command name
     */
    public abstract void execute(final PlayerRef player, final String[] args);
}