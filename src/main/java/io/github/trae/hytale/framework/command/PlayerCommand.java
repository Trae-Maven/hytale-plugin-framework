package io.github.trae.hytale.framework.command;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.trae.hf.Manager;
import io.github.trae.hf.Module;
import io.github.trae.hytale.framework.HytalePlugin;
import io.github.trae.hytale.framework.command.settings.CommandSettings;
import io.github.trae.hytale.framework.utility.UtilArgument;
import lombok.Getter;

import javax.annotation.Nonnull;

/**
 * Base synchronous player-only command that simplifies {@link AbstractPlayerCommand}
 * by exposing a clean {@link #execute(PlayerRef, String[])} method.
 *
 * @param <BasePlugin>  the plugin type
 * @param <BaseManager> the manager this command belongs to
 */
@Getter
public abstract class PlayerCommand<BasePlugin extends HytalePlugin, BaseManager extends Manager<BasePlugin>> extends AbstractPlayerCommand implements Module<BasePlugin, BaseManager> {

    private final Object requiredPermission;

    public PlayerCommand(final String name, final String description, final Object requiredPermission, final boolean requiresConfirmation) {
        super(name, description, requiresConfirmation);

        this.requiredPermission = requiredPermission;

        // Override Hytale default fallback message for unrecognised arguments
        this.setAllowsExtraArguments(true);
    }

    public PlayerCommand(final String name, final String description, final Object requiredPermission) {
        this(name, description, requiredPermission, false);
    }

    @Override
    protected boolean canGeneratePermission() {
        return false;
    }

    @Override
    protected void execute(@Nonnull final CommandContext commandContext, @Nonnull final Store<EntityStore> store, @Nonnull final Ref<EntityStore> ref, @Nonnull final PlayerRef playerRef, @Nonnull final World world) {
        if (CommandSettings.getPermissionCheckPredicate().test(commandContext.sender(), this.getRequiredPermission(), true)) {
            this.execute(playerRef, UtilArgument.getArguments(commandContext, 1));
        }
    }

    /**
     * Called when a player executes the command.
     *
     * @param playerRef the player who ran the command
     * @param args      the arguments following the command name
     */
    public abstract void execute(final PlayerRef playerRef, final String[] args);
}