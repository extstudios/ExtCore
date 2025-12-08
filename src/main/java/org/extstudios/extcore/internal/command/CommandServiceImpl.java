package org.extstudios.extcore.internal.command;

import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.Plugin;
import org.extstudios.extcore.api.command.CommandFactory;
import org.extstudios.extcore.api.command.CommandService;
import org.extstudios.extcore.api.LoggingService;
import org.extstudios.extcore.api.MessageService;
import org.extstudios.extcore.internal.Platform;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CommandServiceImpl implements CommandService {

    private final LoggingService logger;
    private final MessageService messages;
    private final CommandMap commandMap;
    private final boolean useBrigadier;

    private final Map<Plugin, Map<String, CommandFactory>> registeredCommands;

    public CommandServiceImpl(LoggingService logger, MessageService messages) {
        this.logger = logger.withPrefix("[Commands]");
        this.messages = messages;
        this.registeredCommands = new ConcurrentHashMap<>();
        this.commandMap = getCommandMap();

        this.useBrigadier = shouldUseBrigadier();

        if (useBrigadier) {
            logger.info("Using Brigadier command system (Paper)");
        } else {
            logger.info("Using Bukkit command system (Folia/fallback)");
        }

        if (!useBrigadier && commandMap == null) {
            logger.error("Failed to get CommandMap! Commands will not work.");
        }
    }

    @Override
    public boolean register(Plugin plugin, CommandFactory command) {
        if (command.getExecutor() == null && command.getSubCommands().isEmpty()) {
            logger.warn("Command", command.getName(), "has no executor or sub-commands");
        }

        boolean success;

        if (useBrigadier) {
            success = registerBrigadier(plugin, command);
        } else {
            success = registerBukkit(plugin, command);
        }

        if (success) {
            registeredCommands.putIfAbsent(plugin, new ConcurrentHashMap<>());
            registeredCommands.get(plugin).put(command.getName().toLowerCase(), command);

            logger.debug("Registered command:", command.getName(), "for", plugin.getName());
            return true;
        } else {
            logger.error("Failed to register command:", command.getName());
            return false;
        }
    }

    private boolean registerBrigadier(Plugin plugin, CommandFactory command) {
        try {
            LifecycleEventManager<@NotNull Plugin> manager = plugin.getLifecycleManager();

            manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
                BrigadierCommandWrapper wrapper = new BrigadierCommandWrapper(command, logger, messages);

                event.registrar().register(wrapper.build(), command.getDescription(), command.getAliases());

                logger.debug("Registered Brigadier command:", command.getName());
            });

            return true;
        } catch (Exception e) {
            logger.error(e, "Failed to register Brigadier command:", command.getName());
            return false;
        }
    }

    private boolean registerBukkit(Plugin plugin, CommandFactory command) {
        if (commandMap == null) {
            logger.error("Cannot register command - CommandMap not available");
            return false;
        }

        BukkitCommandWrapper wrapper = new BukkitCommandWrapper(command, logger, messages);

        boolean success = commandMap.register(plugin.getName().toLowerCase(), wrapper);

        if (success) {
            logger.debug("Registered Bukkit Command:", command.getName());
        }

        return success;
    }

    @Override
    public boolean unregister(Plugin plugin, String commandName) {
        Map<String, CommandFactory> commands = registeredCommands.get(plugin);
        if (commands == null) {
            return false;
        }

        CommandFactory cmd = commands.remove(commandName.toLowerCase());
        if (cmd != null) {
            logger.debug("Unregistered command:", commandName);
            return true;
        }

        return false;
    }

    @Override
    public int unregisterAll(Plugin plugin) {
        Map<String, CommandFactory> commands = registeredCommands.remove(plugin);
        if (commands == null) {
            return 0;
        }

        int count = commands.size();
        logger.info("Unregistered", count, "command(s) for", plugin.getName());
        return count;
    }

    @Override
    public List<String> getCommands(Plugin plugin) {
        Map<String, CommandFactory> commands = registeredCommands.get(plugin);
        if (commands == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(commands.keySet());
    }

    @Override
    public boolean isRegistered(Plugin plugin, String commandName) {
        Map<String, CommandFactory> commands = registeredCommands.get(plugin);
        return commands != null && commands.containsKey(commandName.toLowerCase());
    }

    private boolean shouldUseBrigadier() {
        if (Platform.isFolia()) {
            return false;
        }

         try {
             Class.forName("io.papermc.paper.command.brigadier.Commands");
             Class.forName("com.mojang.brigadier.CommandDispatcher");
             return true;
         } catch (ClassNotFoundException e) {
             return false;
         }
    }

    private CommandMap getCommandMap() {

        if(useBrigadier) {
            return null;
        }

        try {
            Field field = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            field.setAccessible(true);
            return (CommandMap) field.get(Bukkit.getServer());
        } catch (Exception e) {
            logger.error(e, "Failed to get CommandMap");
            return null;
        }
    }
}