package org.extstudios.extcore.api.command;

import java.util.List;

public interface TabCompleter {

    List<String> complete(CommandContext context);
}