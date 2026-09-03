package io.github.trae.hytale.framework.utility.search;

import com.hypixel.hytale.server.core.command.system.CommandSender;
import io.github.trae.hytale.framework.utility.UtilColor;
import io.github.trae.hytale.framework.utility.UtilMessage;
import io.github.trae.hytale.framework.utility.enums.ChatColor;
import io.github.trae.utilities.search.AbstractSearchEngine;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * Hytale-flavoured {@link AbstractSearchEngine} that reports to a {@link CommandSender}.
 *
 * <p>Replaces the logger-backed default messaging with {@link UtilMessage}, so search feedback lands
 * in the sender's chat, and supplies the colour conventions used across the framework: interpolated
 * values are highlighted in yellow and matches are separated by a grey comma.</p>
 *
 * <p>Subclasses still supply the matching and per-candidate formatting rules inherited from the
 * parent.</p>
 *
 * @param <Type> the type being searched for
 */
public abstract class HytaleSearchEngine<Type> extends AbstractSearchEngine<Type, CommandSender> {

    /**
     * Creates a search engine over the given candidate supplier.
     *
     * @param name               the prefix applied to informational messages, may be null or empty
     * @param collectionSupplier supplies the candidates to search, evaluated on every search
     */
    protected HytaleSearchEngine(final String name, final Supplier<Collection<? extends Type>> collectionSupplier) {
        super(name, collectionSupplier);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Sends the message to the command sender through {@link UtilMessage} rather than logging it.
     */
    @Override
    protected void message(final CommandSender commandSender, final String prefix, final String message) {
        UtilMessage.message(commandSender, prefix, message);
    }

    /**
     * {@inheritDoc}
     *
     * @return the value serialized in yellow
     */
    @Override
    protected String getColorFormat(final String string) {
        return UtilColor.serialize(ChatColor.YELLOW.getColor(), string);
    }

    /**
     * {@inheritDoc}
     *
     * @return a grey comma separator
     */
    @Override
    protected String getMatchSeparator() {
        return UtilColor.serialize(ChatColor.GRAY.getColor(), ", ");
    }
}