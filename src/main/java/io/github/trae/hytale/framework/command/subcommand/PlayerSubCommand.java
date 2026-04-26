package io.github.trae.hytale.framework.command.subcommand;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.trae.hf.Module;
import io.github.trae.hf.SubModule;
import io.github.trae.hytale.framework.HytalePlugin;
import io.github.trae.hytale.framework.utility.UtilArgument;

import javax.annotation.Nonnull;

/**
 * Base synchronous player-only subcommand that simplifies {@link AbstractPlayerCommand}
 * for use as a child of a parent command, skipping the first two input tokens.
 *
 * @param <BasePlugin> the plugin type
 * @param <BaseModule> the parent command module this subcommand belongs to
 */
public abstract class PlayerSubCommand<BasePlugin extends HytalePlugin, BaseModule extends Module<BasePlugin, ?>> extends AbstractPlayerCommand implements SubModule<BasePlugin, BaseModule> {

    public PlayerSubCommand(final String name, final String description, final boolean requiresConfirmation) {
        super(name, description, requiresConfirmation);

        // Override Hytale default fallback message for unrecognised arguments
        this.setAllowsExtraArguments(true);
    }

    public PlayerSubCommand(final String name, final String description) {
        this(name, description, false);
    }

    @Override
    protected void execute(@Nonnull final CommandContext commandContext, @Nonnull final Store<EntityStore> store, @Nonnull final Ref<EntityStore> ref, @Nonnull final PlayerRef playerRef, @Nonnull final World world) {
        this.execute(playerRef, UtilArgument.getArguments(commandContext, 2));
    }

    /**
     * Called when a player executes the subcommand.
     *
     * @param player the player who ran the command
     * @param args   the arguments following the subcommand name
     */
    public abstract void execute(final PlayerRef player, final String[] args);
}