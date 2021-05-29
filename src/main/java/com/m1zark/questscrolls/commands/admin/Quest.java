package com.m1zark.questscrolls.commands.admin;

import com.google.common.collect.Lists;
import com.m1zark.m1utilities.api.Chat;
import com.m1zark.m1utilities.api.Inventories;
import com.m1zark.questscrolls.config.ConfigMain;
import com.m1zark.questscrolls.config.ConfigQuests;
import com.m1zark.questscrolls.config.ConfigTiers;
import com.m1zark.questscrolls.data.Quests;
import com.m1zark.questscrolls.data.Task;
import com.m1zark.questscrolls.data.enums.QuestTasks;
import com.m1zark.questscrolls.data.enums.QuestTiers;
import com.m1zark.questscrolls.utils.Item;
import com.m1zark.questscrolls.utils.Rewards;
import com.m1zark.questscrolls.utils.Utils;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.RandomHelper;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.enums.EnumShrine;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

public class Quest implements CommandExecutor {
    @Override public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        //if (!(src instanceof Player)) throw new CommandException(Text.of(TextColors.RED,"You must be logged onto the server to run this command."));
        Player player = (Player) args.getOne("player").orElse(src);
        int quantity = (Integer) args.getOne("quantity").orElse(1);

        if(args.getOne("tier").isPresent()) {
            QuestTiers tier = (QuestTiers) args.getOne("tier").get();

            Quests quest;
            if(args.getOne("id").isPresent()) {
                quest = ConfigQuests.getQuest(tier.getName(), (String) args.getOne("id").get());
                if(quest == null) throw new CommandException(Text.of(TextColors.RED, "Error generating quest... ", TextColors.GRAY, "tier: ", tier.getName(), " | id: ", (String) args.getOne("id").get()));
            } else {
                quest = ConfigQuests.getQuests(tier.getName()).get(new Random().nextInt(ConfigQuests.getQuests(tier.getName()).size()));
                if(quest == null) throw new CommandException(Text.of(TextColors.RED, "Error generating random quest... ", TextColors.GRAY, "tier: ", tier.getName()));
            }

            if(Inventories.giveItem(player, quest.giveItem(), quantity)) {
                Chat.sendMessage(player, Text.of(TextColors.GRAY, "You received a ",  quest.getTier().getColor(), quest.getTier().getName(), TextColors.GRAY, " ", ConfigMain.NAME, "!"));
            } else {
                Chat.sendMessage(src, "&7Unable to send &b" + player.getName() + " &7a " + ConfigMain.NAME + " due to a full inventory.");
                Chat.sendMessage(player, Text.of(TextColors.GRAY, "Unable to receive a ",  quest.getTier().getColor(), quest.getTier().getName(), TextColors.GRAY, " ", ConfigMain.NAME, "!"));
            }
        }

        return CommandResult.success();
    }

    public static class Generate implements CommandExecutor {
        @Override public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
            if (!(src instanceof Player)) throw new CommandException(Text.of(TextColors.RED,"You must be logged onto the server to run this command."));
            Player player = (Player) args.getOne("player").orElse(src);
            int quantity = (Integer) args.getOne("quantity").orElse(1);

            if(args.getOne("tier").isPresent()) {
                QuestTiers tier = (QuestTiers) args.getOne("tier").get();

                List<Task> tasks = Lists.newArrayList();
                int number_of_tasks = RandomHelper.getRandomNumberBetween(ConfigTiers.getTierOptions(tier.getName(),"min-tasks"), ConfigTiers.getTierOptions(tier.getName(),"max-tasks"));
                for(int t = 0; t < number_of_tasks; t++) tasks.add(randomTask(tier));

                List<Rewards> finalRewards = Lists.newArrayList();
                List<Rewards> rewards = ConfigTiers.getAllRewards(tier.getName());
                if(rewards.isEmpty()) throw new CommandException(Text.of(TextColors.RED,"You have not setup the config for the " + tier.getName() + " Tier."));

                Collections.shuffle(rewards);
                for(int i = 0; i < ConfigTiers.getTierOptions(tier.getName(), "max-rewards"); i++) finalRewards.add(rewards.get(i));

                Quests quest = new Quests("GeneratedQuest", "", tier, tasks, finalRewards, true, true, null);

                Inventories.giveItem(player, quest.giveItem(), quantity);
            }

            return CommandResult.success();
        }

        private static Task randomTask(QuestTiers tier) {
            QuestTasks task;

            do {
                task = QuestTasks.getRandomTask();
            } while(tier.getId() < 5 && task == QuestTasks.SHRINE);

            String pokemon = EnumSpecies.randomPoke(false).name();
            int[] amounts = {5,10,25,50,100};

            int needed = amounts[new Random().nextInt(amounts.length)];

            switch (task) {
                case BREED:
                    if (EnumSpecies.hasPokemonAnyCase(pokemon)) {
                        Pokemon poke = Pixelmon.pokemonFactory.create(EnumSpecies.getFromNameAnyCase(pokemon));

                        if(poke.getBaseStats().preEvolutions == null || poke.getBaseStats().preEvolutions.length > 0) {
                            if(!poke.getBaseStats().preEvolutions[0].equals(pokemon)) pokemon = "pokemon";
                        }
                    }
                    break;
                case EVOLVE:
                    if (EnumSpecies.hasPokemonAnyCase(pokemon)) {
                        Pokemon poke = Pixelmon.pokemonFactory.create(EnumSpecies.getFromNameAnyCase(pokemon));
                        if(poke.getBaseStats().evolutions == null || poke.getBaseStats().evolutions.size() <= 0) pokemon = "pokemon";
                    }
                    break;
                case CATCH:
                case DEFEAT:
                    if(EnumSpecies.hasPokemonAnyCase(pokemon) && Utils.checkSpawning(pokemon)) pokemon = "pokemon";
                    break;
            }

            EnumShrine shrine = null;
            if (tier.getId() >= 5 && task == QuestTasks.SHRINE) {
                shrine = EnumShrine.values()[new Random().nextInt(2)];
                needed = 1;
            }

            boolean boss = false;
            if(tier.getId() >= 3 && task == QuestTasks.DEFEAT) {
                boss = new Random().nextBoolean();
                if(boss) pokemon = "pokemon";
            }

            boolean shiny = false;
            if(tier.getId() >= 4 && task == QuestTasks.CATCH) {
                shiny = new Random().nextBoolean();
                if(shiny) {
                    needed = 1;
                    pokemon = "pokemon";
                }
            }

            List<Item> items = ConfigTiers.getAllItems(tier.getName());
            Item item = null;
            if(task == QuestTasks.COLLECT) {
                if(items.isEmpty()) return randomTask(tier);
                item = items.get(new Random().nextInt(items.size()));
            }

            return new Task(task, pokemon, shrine, item, needed, boss, shiny, 0);
        }
    }
}
