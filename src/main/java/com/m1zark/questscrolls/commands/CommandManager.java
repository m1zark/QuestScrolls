package com.m1zark.questscrolls.commands;

import com.m1zark.questscrolls.QSInfo;
import com.m1zark.questscrolls.QuestScrolls;
import com.m1zark.questscrolls.commands.admin.ItemNBT;
import com.m1zark.questscrolls.commands.admin.Quest;
import com.m1zark.questscrolls.commands.admin.Reload;
import com.m1zark.questscrolls.data.enums.QuestTiers;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

import java.util.HashMap;

public class CommandManager {
    public void registerCommands(QuestScrolls plugin) {
        Sponge.getCommandManager().register(plugin, questscrolls, "qs", "qscrolls", "questscrolls");

        QuestScrolls.getInstance().getConsole().ifPresent(console -> console.sendMessages(Text.of(QSInfo.PREFIX, "Registering commands...")));
    }

    private CommandSpec reload = CommandSpec.builder().description(Text.of("Reload all config files.")).executor(new Reload()).build();

    private CommandSpec give = CommandSpec.builder()
            .arguments(
                    GenericArguments.player(Text.of("player")),
                    GenericArguments.enumValue(Text.of("tier"), QuestTiers.class),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("id"))),
                    GenericArguments.optionalWeak(GenericArguments.integer(Text.of("quantity")))
            )
            .executor(new Quest())
            .build();

    private CommandSpec generate = CommandSpec.builder()
            .arguments(
                    GenericArguments.player(Text.of("player")),
                    GenericArguments.enumValue(Text.of("tier"), QuestTiers.class),
                    GenericArguments.optionalWeak(GenericArguments.integer(Text.of("quantity")))
            )
            .executor(new Quest.Generate())
            .build();

    private CommandSpec nbt = CommandSpec.builder().executor(new ItemNBT()).build();

    private CommandSpec questscrolls = CommandSpec.builder()
            .permission("questscrolls.admin")
            .child(reload,"reload")
            .child(give,"give")
            .child(generate,"generate")
            .child(nbt, "nbt")
            .build();
}
