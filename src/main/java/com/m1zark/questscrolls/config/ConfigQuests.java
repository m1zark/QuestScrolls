package com.m1zark.questscrolls.config;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.m1zark.m1utilities.api.Inventories;
import com.m1zark.questscrolls.QSInfo;
import com.m1zark.questscrolls.QuestScrolls;
import com.m1zark.questscrolls.data.Quests;
import com.m1zark.questscrolls.data.Task;
import com.m1zark.questscrolls.data.enums.QuestTasks;
import com.m1zark.questscrolls.data.enums.QuestTiers;
import com.m1zark.questscrolls.utils.Item;
import com.m1zark.questscrolls.utils.Rewards;
import com.m1zark.questscrolls.utils.Utils;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.spawning.SpawnInfo;
import com.pixelmonmod.pixelmon.api.spawning.SpawnSet;
import com.pixelmonmod.pixelmon.api.spawning.archetypes.entities.pokemon.SpawnInfoPokemon;
import com.pixelmonmod.pixelmon.api.spawning.util.SetLoader;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.Gender;
import com.pixelmonmod.pixelmon.enums.EnumGrowth;
import com.pixelmonmod.pixelmon.enums.EnumShrine;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.enchantment.EnchantmentType;
import org.spongepowered.api.text.Text;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class ConfigQuests {
    private static ConfigurationLoader<CommentedConfigurationNode> loader;
    private static CommentedConfigurationNode main;

    public ConfigQuests() {
        this.load();
    }

    private void load() {
        Path dir = Paths.get(QuestScrolls.getInstance().getConfigDir() + "/quests");
        Path configFile = Paths.get(dir + "/quests.conf");

        loader = HoconConfigurationLoader.builder().setPath(configFile).build();

        try {
            if (!Files.exists(dir)) Files.createDirectory(dir);
            if (!Files.exists(configFile)) Files.createFile(configFile);
            if (main == null) main = loader.load(ConfigurationOptions.defaults().setShouldCopyDefaults(true));

            CommentedConfigurationNode questScrolls = main.getNode("Quests");
            main.setComment("Quest Tasks: 1 - Capture, 2 - Defeat, 3 - Level, 4 - Breed, 5 - Evolve, 6 - Collect, 7 - Shrine");

            questScrolls.getNode("Common").getList(TypeToken.of(String.class), Lists.newArrayList());
            questScrolls.getNode("Uncommon").getList(TypeToken.of(String.class), Lists.newArrayList());
            questScrolls.getNode("Rare").getList(TypeToken.of(String.class), Lists.newArrayList());
            questScrolls.getNode("Epic").getList(TypeToken.of(String.class), Lists.newArrayList());
            questScrolls.getNode("Legendary").getList(TypeToken.of(String.class), Lists.newArrayList());
            questScrolls.getNode("Event").getList(TypeToken.of(String.class), Lists.newArrayList());

            loader.save(main);
        } catch (ObjectMappingException | IOException e) {
            e.printStackTrace();
        }

        this.loadConfig();
    }

    private void loadConfig() {

    }

    private static void saveConfig() {
        try {
            loader.save(main);
        } catch (IOException var1) {
            var1.printStackTrace();
        }
    }

    public void reload() {
        try {
            main = loader.load();
            this.loadConfig();
        } catch (IOException var2) {
            var2.printStackTrace();
        }
    }

    public static Quests getQuest(String tier, String id) {
        Quests quest = null;

        for (int i = 0; i < main.getNode("Quests",tier).getChildrenList().size(); i++) {
            CommentedConfigurationNode q = main.getNode("Quests",tier).getChildrenList().get(i);

            if(q.getNode("id").getString().equals(id)) {
                List<Task> tasks = Lists.newArrayList();
                List<Rewards> rewards = Lists.newArrayList();

                boolean displayRewards = !q.getNode("show-rewards").isVirtual() && q.getNode("show-rewards").getBoolean();
                boolean broadcast = !q.getNode("broadcast").isVirtual() && q.getNode("broadcast").getBoolean();

                for(int a = 0; a < q.getNode("tasks").getChildrenList().size(); a++) {
                    CommentedConfigurationNode task = q.getNode("tasks").getChildrenList().get(a);

                    Optional<QuestTasks> type = QuestTasks.getTaskFromName(task.getNode("type").getString().replace("shrine","activate"));
                    if(type.isPresent()) {
                        String pokemon = task.getNode("pokemon").isVirtual() ? "pokemon" : task.getNode("pokemon").getString();
                        Item item = null;
                        if(!task.getNode("item").isVirtual()) {
                            String item_id = task.getNode("item","id").isVirtual() ? null : task.getNode("item","id").getString();
                            String display = task.getNode("item","display-name").isVirtual() ? null : task.getNode("item","display-name").getString();
                            Integer meta = task.getNode("item","data","meta").isVirtual() ? null : task.getNode("item","data","meta").getInt();
                            boolean unbreakable = !task.getNode("item","data","unbreakable").isVirtual() && task.getNode("item","data","unbreakable").getBoolean();

                            List<String> lore = Lists.newArrayList();
                            if(!task.getNode("item","data","lore").isVirtual()) {
                                try {
                                    lore = task.getNode("item","data","lore").getList(TypeToken.of(String.class));
                                } catch (ObjectMappingException e) {
                                    e.printStackTrace();
                                }
                            }

                            List<Enchantment> enchantments = new ArrayList<>();
                            if(!task.getNode("item","data","enchantments").isVirtual()){
                                task.getNode("item","data","enchantments").getChildrenMap().forEach((enchantID, level) -> {
                                    Optional<EnchantmentType> enchantType = Sponge.getRegistry().getType(EnchantmentType.class, enchantID.toString());
                                    if(!enchantType.isPresent()) {
                                        QuestScrolls.getInstance().getConsole().ifPresent(console -> console.sendMessages(Text.of(QSInfo.ERROR_PREFIX, "Invalid Enchantment specified! ",task.getNode("item","data","enchantments",enchantID).getPath())));
                                    } else if(!(level.getValue() instanceof Integer)){
                                        QuestScrolls.getInstance().getConsole().ifPresent(console -> console.sendMessages(Text.of(QSInfo.ERROR_PREFIX, "Invalid Type for Enchantment Level! ",task.getNode("item","data","enchantments",enchantID).getPath())));
                                    }else {
                                        enchantments.add(Enchantment.builder().type(enchantType.get()).level((Integer) level.getValue()).build());
                                    }
                                });
                            }

                            Map nbt = new LinkedHashMap();
                            if(!task.getNode("item","data","nbt").isVirtual() && task.getNode("item","data","nbt").getValue() instanceof LinkedHashMap) {
                                nbt = (LinkedHashMap) task.getNode("item","data","nbt").getValue();
                            }

                            item = new Item(i,item_id,display,meta,unbreakable,nbt,lore,enchantments);
                        }

                        int count = task.getNode("count").isVirtual() ? 1 : task.getNode("count").getInt();
                        boolean boss = !task.getNode("boss").isVirtual() && task.getNode("boss").getBoolean();
                        boolean shiny = type.get() == QuestTasks.CATCH && !task.getNode("shiny").isVirtual() && task.getNode("shiny").getBoolean();

                        switch (type.get()) {
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
                                if(shiny || boss) pokemon = "pokemon";
                                break;
                        }

                        EnumShrine shrine = null;
                        if (type.get() == QuestTasks.SHRINE && !task.getNode("shrine").isVirtual() && task.getNode("shrine").getBoolean()) {
                            shrine = EnumShrine.values()[new Random().nextInt(3)];
                        }

                        if(shrine != null || shiny) count = 1;

                        if (!pokemon.equals("pokemon")) {
                            if (EnumSpecies.hasPokemonAnyCase(pokemon)) tasks.add(new Task(type.get(), pokemon, shrine, item, count, boss, shiny, 0));
                        } else {
                            tasks.add(new Task(type.get(), pokemon, shrine, item, count, boss, shiny, 0));
                        }
                    }
                }

                for(int b = 0; b < q.getNode("rewards").getChildrenList().size(); b++) {
                    CommentedConfigurationNode reward = q.getNode("rewards").getChildrenList().get(b);

                    String type = reward.getNode("type").getString();
                    String name = reward.getNode("display-name").getString();
                    String item = reward.getNode("id").isVirtual() ? null : reward.getNode("id").getString();
                    String command = reward.getNode("command").isVirtual() ? null : reward.getNode("command").getString();
                    Integer cost = reward.getNode("cost").isVirtual() ? 0 : reward.getNode("cost").getInt();

                    Integer count = reward.getNode("data","count").isVirtual() ? 1 : reward.getNode("data","count").getInt();
                    Integer meta = reward.getNode("data","meta").isVirtual() ? null : reward.getNode("data","meta").getInt();

                    boolean unbreakable = !reward.getNode("data","unbreakable").isVirtual() && reward.getNode("data","unbreakable").getBoolean();
                    String sprite = reward.getNode("data","sprite-data").isVirtual() ? null : reward.getNode("data","sprite-data").getString();

                    List<String> lore = Lists.newArrayList();
                    if(!reward.getNode("data","lore").isVirtual()) {
                        try {
                            lore = reward.getNode("data","lore").getList(TypeToken.of(String.class));
                        } catch (ObjectMappingException e) {
                            e.printStackTrace();
                        }
                    }

                    List<Enchantment> enchantments = new ArrayList<>();
                    if(!reward.getNode("data","enchantments").isVirtual()){
                        reward.getNode("data","enchantments").getChildrenMap().forEach((enchantID, level) -> {
                            Optional<EnchantmentType> enchantType = Sponge.getRegistry().getType(EnchantmentType.class, enchantID.toString());
                            if(!enchantType.isPresent()) {
                                QuestScrolls.getInstance().getConsole().ifPresent(console -> console.sendMessages(Text.of(QSInfo.ERROR_PREFIX, "Invalid Enchantment specified! ",reward.getNode("data","enchantments",enchantID).getPath())));
                            } else if(!(level.getValue() instanceof Integer)){
                                QuestScrolls.getInstance().getConsole().ifPresent(console -> console.sendMessages(Text.of(QSInfo.ERROR_PREFIX, "Invalid Type for Enchantment Level! ",reward.getNode("data","enchantments",enchantID).getPath())));
                            }else {
                                enchantments.add(Enchantment.builder().type(enchantType.get()).level((Integer) level.getValue()).build());
                            }
                        });
                    }

                    Map nbt = new LinkedHashMap();
                    if(!reward.getNode("data","nbt").isVirtual() && reward.getNode("data","nbt").getValue() instanceof LinkedHashMap) {
                        nbt = (LinkedHashMap) reward.getNode("data","nbt").getValue();
                    }

                    rewards.add(new Rewards(i,type,item,command,name,meta,nbt,unbreakable,lore,enchantments,sprite,cost,count));
                }

                quest = new Quests(id, q.getNode("lore").getString(), QuestTiers.valueOf(tier.toUpperCase()), tasks, rewards, displayRewards, broadcast, null);
            }
        }

        return quest;
    }

    public static List<Quests> getQuests(String tier) {
        List<Quests> quests = Lists.newArrayList();

        for (int i = 0; i < main.getNode("Quests",tier).getChildrenList().size(); i++) {
            CommentedConfigurationNode q = main.getNode("Quests",tier).getChildrenList().get(i);
            quests.add(getQuest(tier, q.getNode("id").getString()));

            /*
            List<Task> tasks = Lists.newArrayList();
            List<Rewards> rewards = Lists.newArrayList();

            boolean displayRewards = !q.getNode("show-rewards").isVirtual() && q.getNode("show-rewards").getBoolean();
            boolean broadcast = !q.getNode("broadcast").isVirtual() && q.getNode("broadcast").getBoolean();

            for(int a = 0; a < q.getNode("tasks").getChildrenList().size(); a++) {
                    CommentedConfigurationNode task = q.getNode("tasks").getChildrenList().get(a);

                    Optional<QuestTasks> type = QuestTasks.getTaskFromIndex(task.getNode("type").getInt());
                    if(type.isPresent()) {
                        String pokemon = task.getNode("pokemon").isVirtual() ? "pokemon" : task.getNode("pokemon").getString();
                        switch (type.get()) {
                            case BREED:
                                if (EnumSpecies.hasPokemonAnyCase(pokemon)) {
                                    Pokemon poke = Pixelmon.pokemonFactory.create(EnumSpecies.getFromNameAnyCase(pokemon));

                                    if(poke.getBaseStats().preEvolutions.length > 0) {
                                        if(!poke.getBaseStats().preEvolutions[0].name().equals(pokemon)) pokemon = "pokemon";
                                    }
                                }
                                break;
                            case EVOLVE:
                                if (EnumSpecies.hasPokemonAnyCase(pokemon)) {
                                    Pokemon poke = Pixelmon.pokemonFactory.create(EnumSpecies.getFromNameAnyCase(pokemon));
                                    if(poke.getBaseStats().evolutions.size() < 1) pokemon = "pokemon";
                                }
                                break;
                        }

                        String item = task.getNode("item").isVirtual() ? null : task.getNode("item").getString();
                        int count = task.getNode("count").isVirtual() ? 0 : task.getNode("count").getInt();
                        boolean boss = !task.getNode("boss").isVirtual() && task.getNode("boss").getBoolean();

                        EnumShrine shrine = null;
                        if (type.get() == QuestTasks.SHRINE && !task.getNode("shrine").isVirtual() && task.getNode("shrine").getBoolean()) {
                            shrine = EnumShrine.values()[new Random().nextInt(EnumShrine.values().length)];
                        }

                        tasks.add(new Task(type.get(), pokemon, shrine, item, count, boss, 0));
                    }
                }

            for(int b = 0; b < q.getNode("rewards").getChildrenList().size(); b++) {
                    CommentedConfigurationNode reward = q.getNode("rewards").getChildrenList().get(b);

                    String type = reward.getNode("type").getString();
                    String item = reward.getNode("id").isVirtual() ? null : reward.getNode("id").getString();
                    String command = reward.getNode("command").isVirtual() ? null : reward.getNode("command").getString();
                    String name = reward.getNode("display-name").getString();
                    Integer cost = reward.getNode("cost").getInt();

                    Integer count = reward.getNode("data","count").isVirtual() ? 1 : reward.getNode("data","count").getInt();
                    Integer meta = reward.getNode("data","meta").isVirtual() ? null : reward.getNode("data","meta").getInt();

                    boolean unbreakable = !reward.getNode("data","unbreakable").isVirtual() && reward.getNode("data","unbreakable").getBoolean();
                    String sprite = reward.getNode("data","sprite-data").isVirtual() ? null : reward.getNode("data","sprite-data").getString();

                    List<String> lore = Lists.newArrayList();
                    if(!reward.getNode("data","lore").isVirtual()) {
                        try {
                            lore = reward.getNode("data","lore").getList(TypeToken.of(String.class));
                        } catch (ObjectMappingException e) {
                            e.printStackTrace();
                        }
                    }

                    Map nbt = new LinkedHashMap();
                    if(!reward.getNode("data","nbt").isVirtual() && reward.getNode("data","nbt").getValue() instanceof LinkedHashMap) {
                        nbt = (LinkedHashMap) reward.getNode("data","nbt").getValue();
                    }

                    rewards.add(new Rewards(i,type,item,command,name,meta,nbt,unbreakable,lore,sprite,cost,count));
                }

            quests.add(new Quests(q.getNode("id").getString(), q.getNode("lore").getString(), QuestTiers.valueOf(tier.toUpperCase()), tasks, rewards, displayRewards, broadcast, null));
            */
        }

        return quests;
    }
}
