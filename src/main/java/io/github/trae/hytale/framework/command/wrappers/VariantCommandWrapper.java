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
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Nameless engine usage-variant for a framework {@link SharedBaseCommand}, declaring a
 * fixed number of positional required arguments so the client requests completions for
 * those slots on TAB.
 *
 * <p>The framework auto-generates one variant per argument count: the variant for count
 * {@code k} declares the first {@code k} of the command's shared {@link ArgumentTypeWrapper}
 * instances as required arguments. Because the wrappers are created once by the owning
 * command wrapper and shared across variants, each positional slot is registered under a
 * single stable suggestion type id — so a slot's completion chains correctly into the next
 * slot rather than appearing as a separate dead-end id per variant.</p>
 *
 * <p>Regardless of declared argument count, every variant delegates execution to the same
 * {@link SharedBaseCommand#_Execute(CommandSender, String[])} with the raw {@code String[]},
 * so the command's own argument-count handling drives behavior and messages.</p>
 *
 * <p>Created with the description-only constructor (no name), as required by
 * {@link AbstractCommand#addUsageVariant(AbstractCommand)}.</p>
 */
public class VariantCommandWrapper extends AbstractCommand {

    private final SharedBaseCommand<?> sharedBaseCommand;

    /**
     * Creates a nameless variant declaring the first {@code argCount} shared wrappers as
     * required arguments.
     *
     * @param sharedBaseCommand the framework command this variant belongs to
     * @param sharedWrappers    the shared per-slot argument-type wrappers, indexed by slot
     * @param argCount          the number of leading positional slots to declare
     */
    public VariantCommandWrapper(final SharedBaseCommand<?> sharedBaseCommand, final List<ArgumentTypeWrapper> sharedWrappers, final int argCount) {
        super(sharedBaseCommand.getDescription());

        this.setAllowsExtraArguments(true);

        for (int i = 0; i < argCount; i++) {
            final ArgumentTypeWrapper wrapper = sharedWrappers.get(i);

            this.withRequiredArg(wrapper.getSuggestion().getUsage(), wrapper.getSuggestion().getDescription(), wrapper);
        }

        this.sharedBaseCommand = sharedBaseCommand;
    }

    /**
     * Disables automatic permission generation, deferring to the wrapped command.
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
     * Dispatches to the wrapped command with the raw arguments, marshalling onto the
     * player's world thread when the sender is a {@link PlayerRef}.
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