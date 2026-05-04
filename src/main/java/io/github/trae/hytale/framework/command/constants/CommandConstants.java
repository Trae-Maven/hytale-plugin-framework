package io.github.trae.hytale.framework.command.constants;

import java.util.function.Function;

public class CommandConstants {

    public static final Function<String, String> REQUIRED_ARG_FORMATTER = "<%s>"::formatted;
    public static final Function<String, String> OPTIONAL_ARG_FORMATTER = "[%s]"::formatted;
}