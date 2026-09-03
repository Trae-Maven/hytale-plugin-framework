package io.github.trae.hytale.framework.utility.search.types;

import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import io.github.trae.hytale.framework.utility.UtilColor;
import io.github.trae.hytale.framework.utility.enums.ChatColor;
import io.github.trae.hytale.framework.utility.search.HytaleSearchEngine;

import java.util.Locale;

public class WorldSearchEngine extends HytaleSearchEngine<World> {

    public WorldSearchEngine() {
        super("World Search", () -> Universe.get().getWorlds().values());
    }

    @Override
    protected String getTypeFormat(final World world) {
        return UtilColor.serialize(ChatColor.YELLOW.getColor(), world.getName());
    }

    @Override
    protected boolean isExact(final World world, final String result) {
        return world.getName().equalsIgnoreCase(result);
    }

    @Override
    protected boolean isMatching(final World world, final String result) {
        return world.getName().toLowerCase(Locale.ROOT).contains(result.toLowerCase(Locale.ROOT));
    }
}