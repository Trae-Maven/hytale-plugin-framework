package io.github.trae.hytale.framework.utility.message;

import com.hypixel.hytale.protocol.MaybeBool;
import com.hypixel.hytale.server.core.Message;
import io.github.trae.hytale.framework.utility.enums.ChatColor;
import lombok.experimental.UtilityClass;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses a custom HTML-style tag-based markup language into Hytale {@link Message} objects.
 *
 * <p>Supported tags:</p>
 * <ul>
 *   <li><b>Formatting</b> — {@code <bold>}, {@code <italic>}, {@code <monospace>}, {@code <underline>}</li>
 *   <li><b>Named colors</b> — {@code <red>text</red>}, {@code <gold>text</gold>} (from {@link ChatColor})</li>
 *   <li><b>Hex colors</b> — {@code <#FF5555>text</#FF5555>}</li>
 *   <li><b>Links</b> — {@code <link>Click here (https://example.com)</link>}</li>
 *   <li><b>Translations</b> — {@code <translate>translation.key</translate>}</li>
 * </ul>
 *
 * <p>Tags are nestable. Closing a color tag resets to the provided reset color.
 * Unrecognized tags are rendered as literal text.</p>
 *
 * <h3>Example usage:</h3>
 * <pre>{@code
 * Message msg = MessageParser.parse("<bold><red>Warning:</red></bold> Server restarting in <gold>5</gold> seconds.");
 * Message msg = MessageParser.parse("<#00FF00>Custom hex color</#00FF00>");
 * Message msg = MessageParser.parse("<link>Click here (https://example.com)</link>");
 * }</pre>
 */
@UtilityClass
public class MessageParser {

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /**
     * Parses a markup string into a styled {@link Message} using {@link Color#WHITE} as the reset color.
     *
     * @param input the raw markup string
     * @return the parsed message
     */
    public static Message parse(final String input) {
        return parse(input, Color.WHITE);
    }

    /**
     * Parses a custom tag-based markup string into a composite {@link Message}.
     *
     * <p>Supported tags:</p>
     * <ul>
     *   <li>{@code <bold>}, {@code <italic>}, {@code <monospace>}, {@code <underline>} — toggle formatting</li>
     *   <li>{@code <red>}, {@code <gold>}, etc. — named colors (from {@link ChatColor})</li>
     *   <li>{@code <#RRGGBB>} — hex color</li>
     *   <li>{@code <translate>key</translate>} — translation key lookup</li>
     *   <li>{@code <link>text (url)</link>} — clickable link with optional display text</li>
     *   <li>{@code </tag>} — closes the corresponding tag, resetting to the inherited state</li>
     * </ul>
     *
     * <p>Unrecognized tags are rendered as literal text (e.g. {@code <unknown>} appears as-is).</p>
     *
     * @param input      the raw markup string to parse
     * @param resetColor the base color to fall back to when a color tag is closed
     * @return the fully parsed and styled message
     */
    public static Message parse(final String input, final Color resetColor) {
        if (input == null || input.isEmpty()) {
            return Message.empty();
        }

        final List<Message> segments = new ArrayList<>();

        Color currentColor = resetColor;
        boolean bold = false;
        boolean italic = false;
        boolean monospace = false;
        boolean underline = false;

        int index = 0;
        final int length = input.length();

        while (index < length) {
            final int open = input.indexOf('<', index);

            // No more tags — flush remaining text and break.
            if (open == -1) {
                segments.add(buildSegment(input.substring(index), currentColor, bold, italic, monospace, underline));
                break;
            }

            // Flush literal text before the tag.
            if (open > index) {
                segments.add(buildSegment(input.substring(index, open), currentColor, bold, italic, monospace, underline));
            }

            final int close = input.indexOf('>', open);

            // Unclosed '<' — flush the rest as literal text and break.
            if (close == -1) {
                segments.add(buildSegment(input.substring(open), currentColor, bold, italic, monospace, underline));
                break;
            }

            final String tag = input.substring(open + 1, close).trim().toLowerCase();

            // --- <translate>key</translate> ---
            if (tag.equals("translate")) {
                index = parseTranslateTag(input, close, segments, currentColor, bold, italic, monospace, underline);
                continue;
            }

            // --- <link>text (url)</link> ---
            if (tag.equals("link")) {
                index = parseLinkTag(input, close, segments, currentColor, bold, italic, monospace, underline);
                continue;
            }

            // --- Closing tags (</bold>, </red>, etc.) ---
            if (tag.startsWith("/")) {
                switch (tag.substring(1)) {
                    case "bold" -> bold = false;
                    case "italic" -> italic = false;
                    case "monospace" -> monospace = false;
                    case "underline" -> underline = false;
                    default -> currentColor = resetColor;
                }

                index = close + 1;
                continue;
            }

            // --- Opening tags (formatting, named color, hex color) ---
            try {
                switch (tag) {
                    case "bold" -> bold = true;
                    case "italic" -> italic = true;
                    case "monospace" -> monospace = true;
                    case "underline" -> underline = true;
                    default -> currentColor = resolveColor(tag);
                }
            } catch (final IllegalArgumentException ignored) {
                // Unrecognized tag — render as literal text.
                segments.add(buildSegment("<" + tag + ">", currentColor, bold, italic, monospace, underline));
            }

            index = close + 1;
        }

        return joinSegments(segments);
    }

    // -----------------------------------------------------------------------
    // Tag parsers
    // -----------------------------------------------------------------------

    /**
     * Parses a {@code <translate>key</translate>} block starting after the opening tag's closing bracket.
     *
     * @return the updated parser index
     */
    private static int parseTranslateTag(final String input, final int closeOfOpenTag, final List<Message> segments, final Color color, final boolean bold, final boolean italic, final boolean monospace, final boolean underline) {
        final int end = input.indexOf("</translate>", closeOfOpenTag + 1);

        if (end == -1) {
            // Malformed — no closing tag. Render the opening tag as literal text.
            segments.add(buildSegment(input.substring(closeOfOpenTag - "translate".length() - 1, closeOfOpenTag + 1), color, bold, italic, monospace, underline));
            return closeOfOpenTag + 1;
        }

        final String key = input.substring(closeOfOpenTag + 1, end).trim();
        segments.add(buildTranslationSegment(key, color, bold, italic, monospace, underline));

        return end + "</translate>".length();
    }

    /**
     * Parses a {@code <link>text (url)</link>} block starting after the opening tag's closing bracket.
     * If the content contains {@code text (url)}, the text is used as the display and the URL as the link target.
     * Otherwise the entire content is used as both display and URL.
     *
     * @return the updated parser index
     */
    private static int parseLinkTag(final String input, final int closeOfOpenTag, final List<Message> segments, final Color color, final boolean bold, final boolean italic, final boolean monospace, final boolean underline) {
        final int end = input.indexOf("</link>", closeOfOpenTag + 1);

        if (end == -1) {
            // Malformed — render the opening tag as literal text.
            segments.add(buildSegment(input.substring(closeOfOpenTag - "link".length() - 1, closeOfOpenTag + 1), color, bold, italic, monospace, underline));
            return closeOfOpenTag + 1;
        }

        final String content = input.substring(closeOfOpenTag + 1, end).trim();

        String displayText = content;
        String url = content;

        // Extract URL from trailing parentheses: "Click here (https://example.com)"
        final int parenOpen = content.lastIndexOf('(');
        if (parenOpen != -1 && content.endsWith(")")) {
            displayText = content.substring(0, parenOpen).trim();
            url = content.substring(parenOpen + 1, content.length() - 1).trim();
        }

        // Recursively parse the display text to support nested formatting.
        segments.add(parse(displayText, color).link(url));

        return end + "</link>".length();
    }

    // -----------------------------------------------------------------------
    // Segment construction
    // -----------------------------------------------------------------------

    /**
     * Resolves a tag string to a {@link Color}. Supports hex ({@code #RRGGBB}) and named
     * {@link ChatColor} values.
     *
     * @param tag the lowercase tag content (e.g. {@code "red"} or {@code "#ff5555"})
     * @return the resolved color
     * @throws IllegalArgumentException if the tag is not a valid color
     */
    private static Color resolveColor(final String tag) {
        if (tag.startsWith("#")) {
            return new Color(Integer.parseInt(tag.substring(1), 16));
        }

        return ChatColor.valueOf(tag.toUpperCase()).getColor();
    }

    /**
     * Builds a single styled {@link Message} segment with raw text and the given formatting state.
     *
     * @param text      the literal text content
     * @param color     the text color
     * @param bold      whether bold is active
     * @param italic    whether italic is active
     * @param monospace whether monospace is active
     * @param underline whether underline is active
     * @return the styled message segment
     */
    private static Message buildSegment(final String text, final Color color, final boolean bold,
                                        final boolean italic, final boolean monospace, final boolean underline) {
        final Message message = Message.raw(text).color(color).bold(bold).italic(italic).monospace(monospace);
        message.getFormattedMessage().underlined = underline ? MaybeBool.True : MaybeBool.False;
        return message;
    }

    /**
     * Builds a single styled {@link Message} segment with a translation key and the given formatting state.
     *
     * @param key       the i18n translation key
     * @param color     the text color
     * @param bold      whether bold is active
     * @param italic    whether italic is active
     * @param monospace whether monospace is active
     * @param underline whether underline is active
     * @return the styled message segment
     */
    private static Message buildTranslationSegment(final String key, final Color color, final boolean bold, final boolean italic, final boolean monospace, final boolean underline) {
        final Message message = Message.translation(key).color(color).bold(bold).italic(italic).monospace(monospace);
        message.getFormattedMessage().underlined = underline ? MaybeBool.True : MaybeBool.False;
        return message;
    }

    /**
     * Joins a list of message segments into a single {@link Message}.
     *
     * @param segments the collected message parts
     * @return the combined message, or {@link Message#empty()} if the list is empty
     */
    private static Message joinSegments(final List<Message> segments) {
        if (segments.isEmpty()) {
            return Message.empty();
        }

        if (segments.size() == 1) {
            return segments.getFirst();
        }

        return Message.join(segments.toArray(Message[]::new));
    }
}