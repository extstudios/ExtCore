package org.extstudios.extCore.Internal.Command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.extstudios.extCore.API.Command.CommandContext;
import org.extstudios.extCore.API.MessageService;

public class CommandContextImpl implements CommandContext {

    private final CommandSender sender;
    private final String label;
    private final String[] args;
    private final MessageService messages;

    public CommandContextImpl(CommandSender sender, String label, String[] args, MessageService messages) {
        this.sender = sender;
        this.label = label;
        this.args = args;
        this.messages = messages;
    }

    @Override
    public CommandSender getSender() {
        return sender;
    }

    @Override
    public Player getPlayer() {
        if (!isPlayer()) {
            throw new IllegalStateException("Sender is not a player!");
        }
        return (Player) sender;
    }

    @Override
    public boolean isPlayer() {
        return sender instanceof Player;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String[] getArgs() {
        return args;
    }

    @Override
    public int getArgCount() {
        return args.length;
    }

    @Override
    public String getArg(int index) {
        return index >= 0 && index < args.length ? args[index] : null;
    }

    @Override
    public String getArg(int index, String defaultValue) {
        String arg = getArg(index);
        return arg != null ? arg : defaultValue;
    }

    @Override
    public int getArgAsInt(int index, int defaultValue) {
        try {
            String arg = getArg(index);
            return arg != null ? Integer.parseInt(arg) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    public double getArgAsDouble(int index, double defaultValue) {
        try {
            String arg = getArg(index);
            return arg != null ? Double.parseDouble(arg) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    public boolean getArgAsBoolean(int index, boolean defaultValue) {
        String arg = getArg(index);
        if (arg == null) {
            return  defaultValue;
        }

        if (arg.equalsIgnoreCase("true") || arg.equalsIgnoreCase("yes") ||
                arg.equalsIgnoreCase("on") || arg.equals("1")) {
            return true;
        }

        if (arg.equalsIgnoreCase("false") || arg.equalsIgnoreCase("no") ||
                arg.equalsIgnoreCase("off") || arg.equals("0")) {
            return false;
        }

        return defaultValue;
    }

    @Override
    public String joinArgs(int startIndex) {
        return joinArgs(startIndex, " ");
    }

    @Override
    public String joinArgs(int startIndex, String delimiter) {
        if (startIndex >= args.length) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        for (int i = startIndex; i < args.length; i++) {
            if (i > startIndex) {
                builder.append(delimiter);
            }
            builder.append(args[i]);
        }
        return builder.toString();
    }

    @Override
    public void reply(Object... message) {
        messages.send(sender, message);
    }

    @Override
    public void replyError(Object... message) {
        messages.sendRaw(sender, "<red>", message);
    }

    @Override
    public void replySuccess(Object... message) {
        messages.sendRaw(sender, "<green>", message);
    }
}
