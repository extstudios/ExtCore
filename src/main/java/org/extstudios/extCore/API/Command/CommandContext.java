package org.extstudios.extCore.API.Command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public interface CommandContext {

    CommandSender getSender();

    Player getPlayer();

    boolean isPlayer();

    String getLabel();

    String[] getArgs();

    int getArgCount();

    String getArg(int index);

    String getArg(int index, String defaultValue);

    int getArgAsInt(int index, int defaultValue);

    double getArgAsDouble(int index, double defaultValue);

    boolean getArgAsBoolean(int index, boolean defaultValue);

    String joinArgs(int startIndex);

    String joinArgs(int startIndex, String delimiter);

    void reply(Object... message);

    void replyError(Object... message);

    void replySuccess(Object... message);
}
