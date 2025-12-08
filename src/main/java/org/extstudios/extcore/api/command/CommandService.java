package org.extstudios.extcore.api.command;

import org.bukkit.plugin.Plugin;

import java.util.List;

public interface CommandService {

    boolean register(Plugin plugin, CommandFactory command);

    boolean unregister(Plugin plugin, String commandName);

    int unregisterAll(Plugin plugin);

    List<String> getCommands(Plugin plugin);

    boolean isRegistered(Plugin plugin, String commandName);
}