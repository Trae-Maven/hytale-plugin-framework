package io.github.trae.hytale.framework.command.subcommand;

import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import io.github.trae.hf.Module;
import io.github.trae.hf.SubModule;
import io.github.trae.hytale.framework.HytalePlugin;
import io.github.trae.hytale.framework.utility.UtilArgument;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

/**
 * Base synchronous subcommand that simplifies {@link AbstractCommand} for use
 * as a child of a parent command, skipping the first two input tokens.
 *
 * @param <BasePlugin> the plugin type
 * @param <BaseModule> the parent command module this subcommand belongs to
 */
public abstract class SubCommand<BasePlugin extends HytalePlugin, BaseModule extends Module<BasePlugin, ?>> extends AbstractCommand implements SubModule<BasePlugin, BaseModule> {

    public SubCommand(final String name, final String description, final boolean requiresConfirmation) {
        super(name, description, requiresConfirmation);

        // Override Hytale default fallback message for unrecognised arguments
        this.setAllowsExtraArguments(true);
    }

    public SubCommand(final String name, final String description) {
        this(name, description, false);
    }

    @Nullable
    @Override
    protected CompletableFuture<Void> execute(@Nonnull final CommandContext commandContext) {
        this.execute(commandContext.sender(), UtilArgument.getArguments(commandContext, 2));
        return null;
    }

    /**
     * Called when the subcommand is executed.
     *
     * @param sender the entity or console that ran the command
     * @param args   the arguments following the subcommand name
     */
    public abstract void execute(final CommandSender sender, final String[] args);
}