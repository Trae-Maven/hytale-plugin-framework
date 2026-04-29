package io.github.trae.hytale.framework.command.settings;

import com.hypixel.hytale.function.predicate.TriPredicate;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import io.github.trae.hytale.framework.utility.UtilMessage;
import lombok.Getter;
import lombok.Setter;

public class CommandSettings {

    @Getter
    @Setter
    private static TriPredicate<CommandSender, Object, Boolean> permissionCheckPredicate = (sender, permission, inform) -> {
        if (permission != null) {
            if (permission instanceof String permissionString) {
                if (sender.hasPermission(permissionString)) {
                    return true;
                }

                if (inform) {
                    UtilMessage.message(sender, "Permissions", "You do not have permission to use this command!");
                }

                return false;
            }
        }

        return true;
    };
}