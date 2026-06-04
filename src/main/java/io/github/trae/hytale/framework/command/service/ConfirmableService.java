package io.github.trae.hytale.framework.command.service;

import com.hypixel.hytale.server.core.command.system.CommandSender;
import io.github.trae.di.annotations.type.component.Component;
import io.github.trae.hytale.framework.command.impl.Confirmable;
import io.github.trae.hytale.framework.command.service.interfaces.IConfirmableService;
import io.github.trae.utilities.UtilTime;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ConfirmableService implements IConfirmableService {

    private record Cache(Confirmable confirmable, long systemTime) {}

    private final ConcurrentHashMap<CommandSender, Cache> map = new ConcurrentHashMap<>();

    @Override
    public void put(final CommandSender commandSender, final Confirmable confirmable) {
        this.map.put(commandSender, new Cache(confirmable, System.currentTimeMillis()));
    }

    @Override
    public void remove(final CommandSender commandSender, final Confirmable confirmable) {
        if (Optional.ofNullable(this.map.get(commandSender)).map(cache -> cache.confirmable().equals(confirmable)).orElse(false)) {
            this.map.remove(commandSender);
        }
    }

    @Override
    public boolean contains(final CommandSender commandSender, final Confirmable confirmable) {
        this.map.values().removeIf(cache -> UtilTime.elapsed(cache.systemTime(), cache.confirmable().getExpiration()));

        return Optional.ofNullable(this.map.get(commandSender)).map(cache -> cache.confirmable().equals(confirmable)).orElse(false);
    }
}