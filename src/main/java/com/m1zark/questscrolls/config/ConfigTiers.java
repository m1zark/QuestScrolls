package com.m1zark.questscrolls.config;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.m1zark.questscrolls.QSInfo;
import com.m1zark.questscrolls.QuestScrolls;
import com.m1zark.questscrolls.data.enums.QuestTiers;
import com.m1zark.questscrolls.utils.Item;
import com.m1zark.questscrolls.utils.Rewards;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.enchantment.EnchantmentType;
import org.spongepowered.api.text.Text;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class ConfigTiers {
    private static ConfigurationLoader<CommentedConfigurationNode> loader;
    private static CommentedConfigurationNode main;

    public ConfigTiers() {
        this.load();
    }

    private void load() {
        Path dir = Paths.get(QuestScrolls.getInstance().getConfigDir() + "/quests");
        Path configFile = Paths.get(dir + "/tiers.conf");

        loader = HoconConfigurationLoader.builder().setPath(configFile).build();

        try {
            if (!Files.exists(dir)) Files.createDirectory(dir);
            if (!Files.exists(configFile)) Files.createFile(configFile);
            if (main == null) main = loader.load(ConfigurationOptions.defaults().setShouldCopyDefaults(true));

            CommentedConfigurationNode tiers = main.getNode("Tiers");
            for(int i = 0; i < QuestTiers.values().length; i++) {
                QuestTiers tier = QuestTiers.getTierFromID(i + 1);
                if(tier != null) {
                    tiers.getNode(tier.getName(),"min-tasks").getInt(tier.getMin_tasks());
                    tiers.getNode(tier.getName(),"max-tasks").getInt(tier.getMax_tasks());
                    tiers.getNode(tier.getName(),"max-rewards").getInt(tier.getMax_rewards());
                    tiers.getNode(tier.getName(),"random-items").getList(TypeToken.of(String.class), Lists.newArrayList());
                    tiers.getNode(tier.getName(),"random-rewards").getList(TypeToken.of(String.class), Lists.newArrayList());
                }
            }

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

    public static int getTierOptions(String tier,String option) {
        return main.getNode("Tiers",tier,option).getInt();
    }

    public static List<Item> getAllItems(String tier) {
        List<Item> items = Lists.newArrayList();

        for(int i = 0; i < main.getNode("Tiers",tier,"random-items").getChildrenList().size(); i++) {
            CommentedConfigurationNode reward = main.getNode("Tiers",tier,"random-items").getChildrenList().get(i);

            String item = reward.getNode("id").getString();
            String display = reward.getNode("display-name").isVirtual() ? null : reward.getNode("display-name").getString();
            Integer meta = reward.getNode("data","meta").isVirtual() ? null : reward.getNode("data","meta").getInt();
            boolean unbreakable = !reward.getNode("data","unbreakable").isVirtual() && reward.getNode("data","unbreakable").getBoolean();

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

            items.add(new Item(i,item,display,meta,unbreakable,nbt,lore,enchantments));
        }

        return items;
    }

    public static List<Rewards> getAllRewards(String tier) {
        List<Rewards> rewards = Lists.newArrayList();

        for(int i = 0; i < main.getNode("Tiers",tier,"random-rewards").getChildrenList().size(); i++) {
            CommentedConfigurationNode reward = main.getNode("Tiers",tier,"random-rewards").getChildrenList().get(i);

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

        return rewards;
    }
}
