package org.extstudios.extcore.api.command;

import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandFactory {

    private final String name;
    private String description;
    private String usage;
    private String permission;
    private String permissionMessage;
    private List<String> aliases;
    private boolean playerOnly;
    private boolean consoleOnly;
    private int minArgs;
    private int maxArgs;

    private CommandExecutor executor;
    private TabCompleter tabCompleter;
    private Map<String, CommandFactory> subCommands;

    private CommandFactory parent;

    private CommandFactory(String name) {
        this.name = name;
        this.aliases = new ArrayList<>();
        this.subCommands = new HashMap<>();
        this.minArgs = -1;
        this.maxArgs = -1;
    }

    public static CommandFactory create(String name) {
        return new CommandFactory(name);
    }

    public CommandFactory description(String description) {
        this.description = description;
        return this;
    }

    public CommandFactory usage(String usage) {
        this.usage = usage;
        return this;
    }

    public CommandFactory permission(String permission) {
        this.permission = permission;
        return this;
    }

    public CommandFactory permissionMessage(String message) {
        this.permissionMessage = message;
        return this;
    }

    public CommandFactory alias(String alias) {
        this.aliases.add(alias);
        return this;
    }

    public CommandFactory playerOnly() {
        this.playerOnly = true;
        return this;
    }

    public CommandFactory consoleOnly() {
        this.consoleOnly = true;
        return this;
    }

    public CommandFactory minArgs(int minArgs) {
        this.minArgs = minArgs;
        return this;
    }

    public CommandFactory maxArgs(int maxArgs) {
        this.maxArgs = maxArgs;
        return this;
    }

    public CommandFactory executor(CommandExecutor executor) {
        this.executor = executor;
        return this;
    }

    public CommandFactory tabCompleter(TabCompleter tabCompleter) {
        this.tabCompleter = tabCompleter;
        return this;
    }

    public CommandFactory subCommand(String name) {
        CommandFactory subCommand = new CommandFactory(name);
        subCommand.parent = this;
        this.subCommands.put(name.toLowerCase(), subCommand);
        return subCommand;
    }

    public CommandFactory done() {
        return parent;
    }

    public void register(Plugin plugin) {

        throw new UnsupportedOperationException("Use CommandService.register() instead.");
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getUsage() {
        return usage;
    }

    public String getPermission() {
        return permission;
    }

    public String getPermissionMessage() {
        return permissionMessage;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public boolean isPlayerOnly() {
        return playerOnly;
    }

    public boolean isConsoleOnly() {
        return consoleOnly;
    }

    public int getMinArgs() {
        return minArgs;
    }

    public int getMaxArgs() {
        return maxArgs;
    }

    public CommandExecutor getExecutor() {
        return executor;
    }

    public TabCompleter getTabCompleter() {
        return tabCompleter;
    }

    public Map<String, CommandFactory> getSubCommands() {
        return subCommands;
    }

    public CommandFactory getParent() {
        return parent;
    }
}