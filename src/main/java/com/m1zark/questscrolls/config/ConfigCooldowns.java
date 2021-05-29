package com.m1zark.questscrolls.config;

import com.m1zark.questscrolls.QuestScrolls;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.UUID;

public class ConfigCooldowns {
    private static ConfigurationLoader<CommentedConfigurationNode> loader;
    private static CommentedConfigurationNode main;

    public ConfigCooldowns() {
        this.load();
    }

    private void load() {
        Path configFile = Paths.get(QuestScrolls.getInstance().getConfigDir() + "/cooldowns.conf");
        loader = HoconConfigurationLoader.builder().setPath(configFile).build();

        try {
            if (!Files.exists(QuestScrolls.getInstance().getConfigDir())) Files.createDirectory(QuestScrolls.getInstance().getConfigDir());
            if (!Files.exists(configFile)) Files.createFile(configFile);
            if (main == null) main = loader.load(ConfigurationOptions.defaults().setShouldCopyDefaults(true));

            CommentedConfigurationNode cooldowns = main.getNode("Cooldowns");

            loader.save(main);
        } catch (IOException e) {
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

    public static boolean saveCooldown(String quest, UUID uuid, long time) {
        main.getNode("Cooldowns",quest,uuid.toString()).setValue(time);

        saveConfig();
        return true;
    }

    public static HashMap<String,Long> getCooldown(String quest) {
        HashMap<String,Long> data = new HashMap<>();

        main.getNode("Cooldowns",quest).getChildrenMap().forEach((player,time) -> {
            data.put((String)player, time.getLong());
        });

        return data;
    }
}
