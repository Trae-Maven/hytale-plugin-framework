package io.github.trae.hytale.framework.command;

import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import io.github.trae.hf.Manager;
import io.github.trae.hf.Module;
import io.github.trae.hytale.framework.HytalePlugin;
import io.github.trae.hytale.framework.command.impl.SharedBaseCommand;
import io.github.trae.hytale.framework.command.wrappers.AbstractAsyncCommandWrapper;
import io.github.trae.hytale.framework.command.wrappers.AbstractCommandWrapper;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for top-level framework commands.
 *
 * <p>A {@code BaseCommand} is both a {@link Module} (participating in the plugin's
 * manager/module lifecycle) and a {@link SharedBaseCommand} (carrying the shared
 * command contract). The backing engine wrapper —
 * {@link AbstractAsyncCommandWrapper} or {@link AbstractCommandWrapper}, chosen by
 * {@link #isAsynchronous()} — is built lazily in {@link #initializeFrame()} and
 * released in {@link #shutdownFrame()}, rather than at construction time.</p>
 *
 * <p>Deferring wrapper creation until {@link #initializeFrame()} ensures all fields
 * (label, description, aliases) are fully assigned before the wrapper's constructor
 * reads them via {@link #getLabel()}, {@link #getDescription()}, and
 * {@link #getAliases()}.</p>
 *
 * @param <BasePlugin>  the owning plugin type
 * @param <BaseManager> the manager type this command belongs to
 * @param <Sender>      the expected {@link CommandSender} subtype
 */
@Getter
@Setter
public abstract class BaseCommand<BasePlugin extends HytalePlugin, BaseManager extends Manager<BasePlugin>, Sender extends CommandSender> implements Module<BasePlugin, BaseManager>, SharedBaseCommand<Sender> {

    /**
     * The command's primary label and human-readable description.
     */
    private final String label, description;

    /**
     * Mutable list of aliases for this command.
     */
    private final List<String> aliases;

    /**
     * Permission node required to execute this command; {@code null} if unrestricted.
     */
    private String permission;

    /**
     * The engine command wrapper backing this command.
     */
    private AbstractCommand abstractCommand;

    /**
     * Creates a command with the given label, description, and permission.
     *
     * @param label       the primary command label
     * @param description the human-readable description
     * @param permission  the required permission node, or {@code null} if unrestricted
     */
    public BaseCommand(final String label, final String description, final String permission) {
        this.label = label;
        this.description = description;
        this.aliases = new ArrayList<>();
        this.permission = permission;
    }

    /**
     * Creates a command with no permission requirement.
     *
     * @param label       the primary command label
     * @param description the human-readable description
     */
    public BaseCommand(final String label, final String description) {
        this(label, description, null);
    }

    /**
     * Lazily builds the backing engine wrapper if it has not already been created.
     *
     * <p>Selects {@link AbstractAsyncCommandWrapper} or {@link AbstractCommandWrapper}
     * based on {@link #isAsynchronous()}. Idempotent — a no-op if the wrapper already
     * exists.</p>
     */
    @Override
    public void initializeFrame() {
        if (this.abstractCommand == null) {
            this.abstractCommand = this.isAsynchronous() ? new AbstractAsyncCommandWrapper(this) : new AbstractCommandWrapper(this);
        }
    }

    /**
     * Releases the backing engine wrapper, allowing it to be rebuilt on a subsequent
     * {@link #initializeFrame()} call.
     */
    @Override
    public void shutdownFrame() {
        this.abstractCommand = null;
    }
}