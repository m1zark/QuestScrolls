package com.m1zark.questscrolls.commands.admin;

import com.m1zark.m1utilities.api.Chat;
import com.m1zark.questscrolls.QuestScrolls;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;

public class Reload implements CommandExecutor {
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        QuestScrolls.getInstance().getConfigMain().reload();
        QuestScrolls.getInstance().getConfigQuests().reload();
        //QuestScrolls.getInstance().getConfigCooldowns().reload();
        QuestScrolls.getInstance().getConfigMessages().reload();
        QuestScrolls.getInstance().getConfigTiers().reload();

        Chat.sendMessage(src, "&7All QuestScrolls configs were successfully reloaded.");

        return CommandResult.success();
    }
}
