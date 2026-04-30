package io.github.trae.hytale.framework.command;

import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.World;
import io.github.trae.hf.Manager;
import io.github.trae.hf.Module;
import io.github.trae.hytale.framework.HytalePlugin;
import io.github.trae.hytale.framework.command.settings.CommandSettings;
import io.github.trae.hytale.framework.utility.UtilArgument;
import lombok.Getter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

/**
 * Base synchronous command that simplifies {@link AbstractCommand} by exposing
 * a clean {@link #execute(CommandSender, String[])} method.
 *
 * @param <BasePlugin>  the plugin type
 * @param <BaseManager> the manager this command belongs to
 */
@Getter
public abstract class Command<BasePlugin extends HytalePlugin, BaseManager extends Manager<BasePlugin>> extends AbstractCommand implements Module<BasePlugin, BaseManager> {

    private final Object requiredPermission;

    public Command(final String name, final String description, final Object requiredPermission, final boolean requiresConfirmation) {
        super(name, description, requiresConfirmation);

        this.requiredPermission = requiredPermission;

        // Override Hytale default fallback message for unrecognised arguments
        this.setAllowsExtraArguments(true);
    }

    public Command(final String name, final String description, final Object requiredPermission) {
        this(name, description, requiredPermission, false);
    }

    @Override
    protected boolean canGeneratePermission() {
        return false;
    }

    @Nullable
    @Override
    protected CompletableFuture<Void> execute(@Nonnull final CommandContext commandContext) {
        final CommandSender sender = commandContext.sender();

        if (sender instanceof final Player player) {
            final World world = player.getWorld();
            if (world != null) {
                final CompletableFuture<Void> future = new CompletableFuture<>();
                world.execute(() -> {
                    if (CommandSettings.getPermissionCheckPredicate().test(sender, this.getRequiredPermission(), true)) {
                        this.execute(sender, UtilArgument.getArguments(commandContext, 1));
                    }
                    future.complete(null);
                });
                return future;
            }
        }

        if (CommandSettings.getPermissionCheckPredicate().test(sender, this.getRequiredPermission(), true)) {
            this.execute(sender, UtilArgument.getArguments(commandContext, 1));
        }
        return null;
    }

    /**
     * Called when the command is executed.
     *
     * @param sender the entity or console that ran the command
     * @param args   the arguments following the command name
     */
    public abstract void execute(final CommandSender sender, final String[] args);
}