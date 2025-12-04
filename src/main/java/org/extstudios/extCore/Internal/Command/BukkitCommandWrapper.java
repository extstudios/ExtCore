package org.extstudios.extCore.Internal.Command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.extstudios.extCore.API.Command.CommandContext;
import org.extstudios.extCore.API.Command.CommandFactory;
import org.extstudios.extCore.API.LoggingService;
import org.extstudios.extCore.API.MessageService;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class BukkitCommandWrapper extends Command {

    private final CommandFactory command;
    private final LoggingService logger;
    private final MessageService messages;

    public BukkitCommandWrapper(CommandFactory command, LoggingService logger, MessageService messages) {
        super(command.getName());
        this.command = command;
        this.logger = logger;
        this.messages = messages;

        if (command.getDescription() != null) {
            setDescription(command.getDescription());
        }
        if (command.getUsage() != null) {
            setUsage(command.getUsage());
        }
        if (command.getPermission() != null) {
            setPermission(command.getPermission());
        }
        if (command.getPermissionMessage() != null) {
            setPermissionMessage(command.getPermissionMessage());
        }
        if (!command.getAliases().isEmpty()) {
            setAliases(command.getAliases());
        }
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, String @NotNull [] args) {
        try {
            if (command.isPlayerOnly() && !(sender instanceof Player)) {
                messages.send(sender, "<red>This command can only be used by players!</red>");
                return true;
            }

            if (command.isConsoleOnly() && sender instanceof Player) {
                messages.send(sender, "<red>This command can only be used from console!</red>");
                return true;
            }

            if (command.getPermission() != null && !sender.hasPermission(command.getPermission())) {
                String denyMsg = command.getPermissionMessage();
                messages.send(sender, Objects.requireNonNullElse(denyMsg, "<red>You don't have permission to use this command!</red>"));
                return true;
            }

            if (args.length > 0 && !command.getSubCommands().isEmpty()) {
                String subCommandName = args[0].toLowerCase();
                CommandFactory subCommand = command.getSubCommands().get(subCommandName);

                if (subCommand != null) {
                    String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
                    return executeCommand(subCommand, sender, label + " " + args[0], subArgs);
                }
            }

            return executeCommand(command, sender, label, args);
        } catch (Exception e) {
            logger.error(e, "Error executing command:", label);
            messages.send(sender,"<red>An error occurred while executing this command!</red>");
            return true;
        }
    }

    private boolean executeCommand(CommandFactory cmd, CommandSender sender, String label, String[] args) {
        if (cmd.getMinArgs() > 0 && args.length < cmd.getMaxArgs()) {
            if (cmd.getUsage() != null) {
                messages.send(sender, "<red>Usage:</red>", cmd.getUsage());
            } else {
                messages.send(sender, "<red>Not enough arguments!</red>");
            }
            return true;
        }

        if (cmd.getExecutor() == null) {
            if (!cmd.getSubCommands().isEmpty()) {
                messages.send(sender, "<yellow>Available sub-commands:</yellow>");
                for (String subName : cmd.getSubCommands().keySet()) {
                    messages.send(sender, " -", subName);
                }
            } else {
                messages.send(sender, "<red>This command is not fully implemented yet!</red>");
            }
            return true;
        }

        CommandContext context = new CommandContextImpl(sender, label, args, messages);
        cmd.getExecutor().execute(context);

        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, String @NotNull [] args) throws IllegalArgumentException {
        try {
            if (args.length == 1 && !command.getSubCommands().isEmpty()) {
                String partial = args[0].toLowerCase();
                return command.getSubCommands().keySet().stream()
                        .filter(name -> name.startsWith(partial))
                        .collect(Collectors.toList());
            }

            if (args.length > 1 && !command.getSubCommands().isEmpty()) {
                String subCommandName = args[0].toLowerCase();
                CommandFactory subCommand = command.getSubCommands().get(subCommandName);

                if (subCommand != null && subCommand.getTabCompleter() != null) {
                    String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
                    CommandContext context = new CommandContextImpl(sender, alias, subArgs, messages);
                    return subCommand.getTabCompleter().complete(context);
                }
            }

            if (command.getTabCompleter() != null) {
                CommandContext context = new CommandContextImpl(sender, alias, args, messages);
                return command.getTabCompleter().complete(context);
            }

            return new ArrayList<>();

        } catch (Exception e) {
            logger.error(e, "Error in tab completion for:", alias);
            return new ArrayList<>();
        }
    }
}
