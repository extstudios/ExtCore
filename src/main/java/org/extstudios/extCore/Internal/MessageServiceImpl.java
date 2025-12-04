package org.extstudios.extCore.Internal;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.extstudios.extCore.API.MessageService;

import java.time.Duration;
import java.util.Collection;

public class MessageServiceImpl implements MessageService {

    private final MiniMessage miniMessage;
    private final LegacyComponentSerializer legacySerializer;
    private String prefix;
    private Component prefixComponent;

    public MessageServiceImpl() {
        this(null);
    }

    public MessageServiceImpl(String prefix) {
        this.miniMessage = MiniMessage.miniMessage();
        this.legacySerializer = LegacyComponentSerializer.legacyAmpersand();
        setPrefix(prefix);
    }

    @Override
    public void send(CommandSender sender, Object... parts) {
        Component message = parse(parts);
        if (prefixComponent != null) {
            message = prefixComponent.append(Component.space()).append(message);
        }
        sender.sendMessage(message);
    }
    @Override
    public void send(CommandSender sender, Component component) {
        if (prefixComponent != null) {
            component = prefixComponent.append(Component.space()).append(component);
        }
        sender.sendMessage(component);
    }

    @Override
    public void sendRaw(CommandSender sender, Object... parts) {
        sender.sendMessage(parse(parts));
    }

    @Override
    public void broadcast(Object... parts) {
        Component message = parse(parts);
        if (prefixComponent != null) {
            message = prefixComponent.append(Component.space()).append(message);
        }

        for(Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(message);
        }

        Bukkit.getConsoleSender().sendMessage(message);
    }

    @Override
    public void broadcast(String permission, Object... parts) {
        Component message = parse(parts);
        if (prefixComponent != null) {
            message = prefixComponent.append(Component.space()).append(message);
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission(permission)) {
                player.sendMessage(message);
            }
        }
    }

    @Override
    public void broadcast(Collection<? extends Player> players, Object... parts) {
        Component message = parse(parts);
        if (prefixComponent != null) {
            message = prefixComponent.append(Component.space()).append(message);
        }

        for(Player player : players) {
            player.sendMessage(message);
        }
    }

    @Override
    public void console(Object... parts) {
        sendRaw(Bukkit.getConsoleSender(), parts);
    }

    @Override
    public Component parse(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }

        if (text.contains("<")) {
            try {
                return miniMessage.deserialize(text);
            } catch (Exception e) {

            }
        }

        if (text.contains("&")) {
            return legacySerializer.deserialize(text);
        }

        return Component.text(text);
    }

    @Override
    public Component parse(Object... parts) {
        if (parts == null || parts.length == 0) {
            return Component.empty();
        }

        if (parts.length == 1) {
            return parse(String.valueOf(parts[0]));
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i <parts.length; i++) {
            if (i > 0) {
                sb.append(" ");
            }
            sb.append(parts[i]);
        }

        return parse(sb.toString());
    }

    @Override
    public String strip(String text) {
        if (text == null) {
            return "";
        }

        String stripped = text.replaceAll("&[0-9a-fk-or]", "");

        stripped = stripped.replaceAll("<[^>]+>", "");

        return stripped;
    }

    @Override
    public void setPrefix(String prefix) {
        this.prefix = prefix;
        this.prefixComponent = (prefix != null && !prefix.isEmpty()) ? parse(prefix) : null;
    }


    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public MessageService withPrefix(String prefix) {
        return new MessageServiceImpl(prefix);
    }

    @Override
    public void actionBar(Player player, Object... parts) {
        player.sendActionBar(parse(parts));
    }

    @Override
    public void title(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        Component titleComponent = parse(title);
        Component subtitleComponent = subtitle != null ? parse(subtitle) : Component.empty();

        Title adventureTitle = Title.title(
                titleComponent,
                subtitleComponent,
                Title.Times.times(
                        Duration.ofMillis(fadeIn * 50L),
                        Duration.ofMillis(stay * 50L),
                        Duration.ofMillis(fadeOut * 50L)
                )
        );

        player.showTitle(adventureTitle);
    }

    @Override
    public void title(Player player, String title, String subtitle) {
        title(player, title, subtitle, 10, 70, 20);
    }
}
