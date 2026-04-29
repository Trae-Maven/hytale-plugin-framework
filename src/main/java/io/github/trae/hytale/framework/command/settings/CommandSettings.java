package io.github.trae.hytale.framework.command.settings;

import com.hypixel.hytale.server.core.command.system.CommandSender;
import lombok.Getter;
import lombok.Setter;

import java.util.function.BiPredicate;

public class CommandSettings {

    @Getter
    @Setter
    private static BiPredicate<CommandSender, Object> permissionCheckPredicate = (sender, permission) -> {
        if (permission != null) {
            if (permission instanceof String permissionString) {
                return sender.hasPermission(permissionString);
            }
        }

        return true;
    };
}