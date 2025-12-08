package org.extstudios.extcore.api;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;

public interface MessageService {

    void send(CommandSender sender, Object... parts);

    void send(CommandSender sender, Component component);

    void sendRaw(CommandSender sender, Object... parts);

    void broadcast(Object... parts);

    void broadcast(String permission, Object... parts);

    void broadcast(Collection<? extends Player> players, Object... parts);

    void console(Object... parts);

    Component parse(String text);

    Component parse(Object... parts);

    String strip(String text);

    void setPrefix(String prefix);

    String getPrefix();

    MessageService withPrefix(String prefix);

    void actionBar(Player player, Object... parts);

    void title(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut);

    void title(Player player, String title, String subtitle);
}