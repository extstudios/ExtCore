package org.extstudios.extCore.API.Command;

import java.util.List;

public interface TabCompleter {

    List<String> complete(CommandContext context);
}
