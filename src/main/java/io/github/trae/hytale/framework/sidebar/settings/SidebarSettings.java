package io.github.trae.hytale.framework.sidebar.settings;

import lombok.Getter;
import lombok.Setter;

import java.util.function.Supplier;

/**
 * Global configuration for the sidebar system.
 *
 * <p>Provides static settings that control the sidebar layout and line capacity.
 * Both {@link #maxLines} and {@link #markup} can be overridden by plugins before
 * any sidebars are created to customise the appearance and capacity.</p>
 *
 * <p>The {@link #markup} supplier is evaluated each time a new sidebar HUD is
 * built, reading the current {@link #maxLines} value at invocation time. This
 * allows plugins to adjust settings dynamically.</p>
 *
 * <p>Example — changing the maximum number of lines:</p>
 * <pre>{@code
 * SidebarSettings.setMaxLines(8);
 * }</pre>
 *
 * <p>Example — replacing the markup entirely:</p>
 * <pre>{@code
 * SidebarSettings.setMarkup(() -> "Group #sidebar-root { ... }");
 * }</pre>
 */
public class SidebarSettings {

    /**
     * The maximum number of lines a sidebar can display.
     *
     * <p>Determines how many {@code #line-N} labels are generated in the
     * inline UI markup. Defaults to {@code 16}.</p>
     */
    @Getter
    @Setter
    private static int maxLines = 16;

    /**
     * Supplier that generates the inline UI markup for the sidebar layout.
     *
     * <p>The default implementation builds a vertically laid out group containing
     * a title label, a divider, and {@link #maxLines} line labels. Each element
     * is addressed by a selector ID ({@code #sidebar-title}, {@code #sidebar-divider},
     * {@code #line-0} through {@code #line-N}) for targeted value updates.</p>
     *
     * <p>Plugins can replace this supplier to fully customise the sidebar's
     * visual appearance while maintaining the expected selector IDs.</p>
     */
    @Getter
    @Setter
    private static Supplier<String> markup = () -> {
        final StringBuilder sb = new StringBuilder();

        sb.append("Group #sidebar-root { ")
                .append("Style: (")
                .append("Position: Absolute; ")
                .append("Right: 10; ")
                .append("Top: 80; ")
                .append("Width: 200; ")
                .append("Layout: Vertical; ")
                .append("Padding: (Top: 8; Bottom: 8; Left: 10; Right: 10); ")
                .append("BackgroundColor: (R: 0; G: 0; B: 0; A: 0.45); ")
                .append("); ");

        sb.append("Label #sidebar-title { ")
                .append("Text: \"\"; ")
                .append("Style: (")
                .append("FontSize: 14; ")
                .append("FontWeight: Bold; ")
                .append("Color: (R: 255; G: 255; B: 85); ")
                .append("Alignment: Center; ")
                .append("Margin: (Bottom: 6); ")
                .append("); ")
                .append("} ");

        sb.append("Group #sidebar-divider { ")
                .append("Style: (")
                .append("Height: 1; ")
                .append("BackgroundColor: (R: 170; G: 170; B: 170; A: 0.5); ")
                .append("Margin: (Bottom: 4); ")
                .append("); ")
                .append("} ");

        for (int i = 0; i < maxLines; i++) {
            sb.append("Label #line-").append(i).append(" { ")
                    .append("Text: \"\"; ")
                    .append("Style: (")
                    .append("FontSize: 11; ")
                    .append("Color: (R: 255; G: 255; B: 255); ")
                    .append("Margin: (Bottom: 2); ")
                    .append("); ")
                    .append("} ");
        }

        sb.append("}");

        return sb.toString();
    };
}
