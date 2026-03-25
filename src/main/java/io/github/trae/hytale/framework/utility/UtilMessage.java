package io.github.trae.hytale.framework.utility;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.console.ConsoleSender;
import com.hypixel.hytale.server.core.receiver.IMessageReceiver;
import com.hypixel.hytale.server.core.universe.Universe;
import io.github.trae.hytale.framework.utility.message.MessageParser;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.UtilityClass;

import java.awt.*;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Utility for sending styled messages to players, broadcasts, and the console.
 *
 * <p>Supports a custom tag-based markup language parsed at runtime:</p>
 * <ul>
 *   <li><b>Color</b> — Named: {@code <red>text</red>} ...
 *   <li><b>Formatting</b> — {@code <bold>}, {@code <italic>}, ...
 *   <li><b>Links</b> — {@code <link>Click here (https://example.com)</link>}</li>
 *   <li><b>Translations</b> — {@code <translate>translation.key</translate>}</li>
 * </ul>
 *
 * <p>Tags are nestable and closing a color tag resets to the inherited reset color.</p>
 */
@UtilityClass
public class UtilMessage {

    /**
     * Default prefix color (blue). Applied to the {@code Prefix} label in messages.
     */
    @Getter
    @Setter
    private static Color prefixColor = new Color(0, 128, 255);

    /**
     * Default body text color (light gray). Applied to message content when a prefix is present.
     */
    @Getter
    @Setter
    private static Color messageColor = new Color(168, 168, 168);

    /**
     * Fallback color used when no prefix is provided or when a color tag is closed.
     */
    @Getter
    @Setter
    private static Color resetColor = new Color(255, 255, 255);

    /**
     * Format string for the prefix label. Must contain a single {@code %s} placeholder.
     */
    @Getter
    @Setter
    private static String prefixFormat = "[%s] ";

    // -----------------------------------------------------------------------
    // Prefix resolution
    // -----------------------------------------------------------------------

    /**
     * Builds a colored prefix {@link Message} from the given label and color.
     *
     * @param color  the prefix color
     * @param prefix the prefix label, or {@code null} for an empty message
     * @return the formatted prefix message
     */
    public static Message resolvePrefix(final Color color, final String prefix) {
        if (prefix == null) {
            return Message.empty();
        }

        return Message.raw(getPrefixFormat().formatted(prefix)).color(color);
    }

    /**
     * Builds a prefix {@link Message} using the default {@link #prefixColor}.
     *
     * @param prefix the prefix label
     * @return the formatted prefix message
     */
    public static Message resolvePrefix(final String prefix) {
        return resolvePrefix(getPrefixColor(), prefix);
    }

    // -----------------------------------------------------------------------
    // Single-recipient messaging
    // -----------------------------------------------------------------------

    /**
     * Sends a pre-built {@link Message} to a single receiver.
     *
     * @param messageReceiver the target receiver, or {@code null} (no-op)
     * @param message         the message to send
     */
    public static void message(final IMessageReceiver messageReceiver, final Message message) {
        if (messageReceiver == null) {
            return;
        }

        messageReceiver.sendMessage(message);
    }

    /**
     * Parses a markup string and sends it to a single receiver.
     *
     * @param messageReceiver the target receiver
     * @param message         the raw markup string
     */
    public static void message(final IMessageReceiver messageReceiver, final String message) {
        message(messageReceiver, MessageParser.parse(message, getResetColor()));
    }

    /**
     * Sends a prefixed {@link Message} to a single receiver.
     *
     * @param messageReceiver the target receiver
     * @param prefix          the prefix label
     * @param message         the pre-built message body
     */
    public static void message(final IMessageReceiver messageReceiver, final String prefix, final Message message) {
        message(messageReceiver, Message.join(resolvePrefix(prefix), message));
    }

    /**
     * Parses a markup string and sends it with a prefix to a single receiver.
     * Uses {@link #messageColor} as the base text color when a prefix is present.
     *
     * @param messageReceiver the target receiver
     * @param prefix          the prefix label, or {@code null} for no prefix
     * @param message         the raw markup string
     */
    public static void message(final IMessageReceiver messageReceiver, final String prefix, final String message) {
        message(messageReceiver, prefix, MessageParser.parse(message, prefix == null ? getResetColor() : getMessageColor()));
    }

    // -----------------------------------------------------------------------
    // Multi-recipient messaging
    // -----------------------------------------------------------------------

    /**
     * Sends a prefixed {@link Message} to a collection of receivers, optionally ignoring specific UUIDs.
     *
     * @param messageReceivers the target receivers
     * @param prefix           the prefix label
     * @param message          the pre-built message body
     * @param ignored          UUIDs to skip, or {@code null} to send to all
     */
    public static <MessageReceiver extends IMessageReceiver> void message(final Collection<MessageReceiver> messageReceivers, final String prefix, final Message message, final List<UUID> ignored) {
        for (final MessageReceiver messageReceiver : messageReceivers) {
            if (ignored != null && messageReceiver instanceof final CommandSender commandSender && ignored.contains(commandSender.getUuid())) {
                continue;
            }

            message(messageReceiver, prefix, message);
        }
    }

    /**
     * Parses a markup string and sends it with a prefix to a collection of receivers.
     *
     * @param messageReceivers the target receivers
     * @param prefix           the prefix label
     * @param message          the raw markup string
     * @param ignored          UUIDs to skip, or {@code null} to send to all
     */
    public static <MessageReceiver extends IMessageReceiver> void message(final Collection<MessageReceiver> messageReceivers, final String prefix, final String message, final List<UUID> ignored) {
        message(messageReceivers, prefix, MessageParser.parse(message, getResetColor()), ignored);
    }

    // -----------------------------------------------------------------------
    // Broadcast (all online players)
    // -----------------------------------------------------------------------

    /**
     * Broadcasts a {@link Message} to all players, optionally ignoring specific UUIDs.
     */
    public static void broadcast(final Message message, final List<UUID> ignored) {
        message(Universe.get().getPlayers(), null, message, ignored);
    }

    /**
     * Broadcasts a {@link Message} to all players.
     */
    public static void broadcast(final Message message) {
        broadcast(message, null);
    }

    /**
     * Parses and broadcasts a markup string to all players, optionally ignoring specific UUIDs.
     */
    public static void broadcast(final String message, final List<UUID> ignored) {
        broadcast(MessageParser.parse(message, getResetColor()), ignored);
    }

    /**
     * Parses and broadcasts a markup string to all players.
     */
    public static void broadcast(final String message) {
        broadcast(message, (List<UUID>) null);
    }

    /**
     * Broadcasts a prefixed {@link Message} to all players, optionally ignoring specific UUIDs.
     */
    public static void broadcast(final String prefix, final Message message, final List<UUID> ignored) {
        broadcast(Message.join(resolvePrefix(prefix), message), ignored);
    }

    /**
     * Broadcasts a prefixed {@link Message} to all players.
     */
    public static void broadcast(final String prefix, final Message message) {
        broadcast(prefix, message, null);
    }

    /**
     * Parses and broadcasts a prefixed markup string to all players, optionally ignoring specific UUIDs.
     */
    public static void broadcast(final String prefix, final String message, final List<UUID> ignored) {
        broadcast(prefix, MessageParser.parse(message, prefix == null ? getResetColor() : getMessageColor()), ignored);
    }

    /**
     * Parses and broadcasts a prefixed markup string to all players.
     */
    public static void broadcast(final String prefix, final String message) {
        broadcast(prefix, message, null);
    }

    // -----------------------------------------------------------------------
    // Console logging
    // -----------------------------------------------------------------------

    /**
     * Sends a {@link Message} to the server console.
     */
    public static void log(final Message message) {
        ConsoleSender.INSTANCE.sendMessage(message);
    }

    /**
     * Parses a markup string and sends it to the server console.
     */
    public static void log(final String message) {
        log(MessageParser.parse(message, getResetColor()));
    }

    /**
     * Sends a prefixed {@link Message} to the server console.
     */
    public static void log(final String prefix, final Message message) {
        log(Message.join(resolvePrefix(prefix), message));
    }

    /**
     * Parses a markup string and sends it with a prefix to the server console.
     */
    public static void log(final String prefix, final String message) {
        log(prefix, MessageParser.parse(message, prefix == null ? getResetColor() : getMessageColor()));
    }
}