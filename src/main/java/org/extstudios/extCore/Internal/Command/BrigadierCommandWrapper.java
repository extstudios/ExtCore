package org.extstudios.extCore.Internal.Command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;
import org.extstudios.extCore.API.Command.CommandFactory;
import org.extstudios.extCore.API.LoggingService;
import org.extstudios.extCore.API.MessageService;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class BrigadierCommandWrapper {

    private final CommandFactory command;
    private final LoggingService logger;
    private final MessageService messages;

    public BrigadierCommandWrapper(CommandFactory command, LoggingService logger, MessageService messages) {
        this.command = command;
        this.logger = logger;
        this.messages = messages;
    }

    public LiteralCommandNode<CommandSourceStack> build() {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal(command.getName());

        if (command.getExecutor() != null) {
            builder.executes(ctx -> {
                executeCommand(command, ctx, new String[0]);
                return 1;
            });
        }

        for (CommandFactory subCommand : command.getSubCommands().values()) {
            builder.then(buildSubCommand(subCommand));
        }

        if (command.getSubCommands().isEmpty() && command.getExecutor() != null) {
            RequiredArgumentBuilder<CommandSourceStack, String> argsBuilder =
                    Commands.argument("args", StringArgumentType.greedyString());

            argsBuilder.executes(ctx -> {
                String argsString = ctx.getArgument("args", String.class);
                String[] args = argsString.split(" ");
                executeCommand(command, ctx, args);
                return 1;
            });

            if(command.getTabCompleter() != null) {
                argsBuilder.suggests((ctx, builder2) -> getTabCompletions(command, ctx, builder2));
            }

            builder.then(argsBuilder);
        }

        return builder.build();
    }

    private LiteralArgumentBuilder<CommandSourceStack> buildSubCommand(CommandFactory subCommand) {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal(subCommand.getName());

        if (subCommand.getExecutor() != null) {
            builder.executes(ctx -> {
                executeCommand(subCommand, ctx, new String[0]);
                return 1;
            });

            if (subCommand.getMinArgs() > 0 || subCommand.getMaxArgs() >= 0) {
                RequiredArgumentBuilder<CommandSourceStack, String> argsBuilder =
                        Commands.argument("args", StringArgumentType.greedyString());

                argsBuilder.executes(ctx -> {
                    String argsString = ctx.getArgument("agrs", String.class);
                    String[] args = argsString.split(" ");
                    executeCommand(subCommand, ctx, args);
                    return 1;
                });

                if (subCommand.getTabCompleter() != null) {
                    argsBuilder.suggests((ctx, builder2) -> getTabCompletions(subCommand, ctx, builder2));
                }

                builder.then(argsBuilder);
            }
        }

        for (CommandFactory nestedSub : subCommand.getSubCommands().values()) {
            builder.then(buildSubCommand(nestedSub));
        }

        return builder;
    }

    private void executeCommand(CommandFactory cmd, CommandContext<CommandSourceStack> ctx, String[] args) {
        try {
            CommandSourceStack source = ctx.getSource();

            if (cmd.isPlayerOnly() && !(source.getSender() instanceof Player)) {
                messages.send(source.getSender(), "<red>This command can only be used by players!</red>");
                return;
            }

            if (cmd.isConsoleOnly() && source.getSender() instanceof Player) {
                messages.send(source.getSender(), "<red>This command can only be used from console!</red>");
                return;
            }

            if (cmd.getPermission() != null && !source.getSender().hasPermission(cmd.getPermission())) {
                String denyMsg = cmd.getPermissionMessage();
                messages.send(source.getSender(), Objects.requireNonNullElse(denyMsg, "<red>You don't have permission to use this command!</red>"));
                return;
            }

            if (cmd.getMinArgs() >= 0 && args.length < cmd.getMinArgs()) {
                if (cmd.getUsage() != null) {
                    messages.send(source.getSender(), "<red>Usage:</red>", cmd.getUsage());
                } else {
                    messages.send(source.getSender(), "<red> Not enough arguments!</red>");
                }
                return;
            }

            if (cmd.getMaxArgs() >= 0 && args.length > cmd.getMaxArgs()) {
                if (cmd.getUsage() != null) {
                    messages.send(source.getSender(), "<red>Usage:</red>", cmd.getUsage());
                } else {
                    messages.send(source.getSender(), "<red>Too many arguments!</red>");
                }
                return;
            }

            if (cmd.getExecutor() != null) {
                String label = cmd.getName();
                org.extstudios.extCore.API.Command.CommandContext context = new CommandContextImpl(source.getSender(), label, args, messages);
                cmd.getExecutor().execute(context);
            }
        } catch (Exception e) {
            logger.error(e, "Error executing Brigadier command");
            messages.send(ctx.getSource().getSender(), "<red>An error occurred while executing this command!</red>");
        }
    }

    private CompletableFuture<Suggestions> getTabCompletions(CommandFactory cmd, CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        try {
            if (cmd.getTabCompleter() == null) {
                return builder.buildFuture();
            }

            String input = builder.getRemaining();
            String[] args = input.isEmpty() ? new String[0] : input.split(" ", -1);

            org.extstudios.extCore.API.Command.CommandContext context = new CommandContextImpl(
                    ctx.getSource().getSender(),
                    cmd.getName(),
                    args,
                    messages
            );

            List<String> completions = cmd.getTabCompleter().complete(context);
            if (completions != null) {
                for (String completion : completions) {
                    builder.suggest(completion);
                }
            }
        } catch (Exception e) {
            logger.debug("Error in Brigadier tab completion:", e.getMessage());
        }

        return builder.buildFuture();
    }
}
