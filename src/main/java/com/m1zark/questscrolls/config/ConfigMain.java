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

public class ConfigMain {
    private static ConfigurationLoader<CommentedConfigurationNode> loader;
    private static CommentedConfigurationNode main;

    public static String NAME;
    public static String ID;

    public ConfigMain() {
        this.load();
    }

    private void load() {
        Path configFile = Paths.get(QuestScrolls.getInstance().getConfigDir() + "/questscrolls.conf");
        loader = HoconConfigurationLoader.builder().setPath(configFile).build();

        try {
            if (!Files.exists(QuestScrolls.getInstance().getConfigDir())) Files.createDirectory(QuestScrolls.getInstance().getConfigDir());
            if (!Files.exists(configFile)) Files.createFile(configFile);
            if (main == null) main = loader.load(ConfigurationOptions.defaults().setShouldCopyDefaults(true));

            CommentedConfigurationNode questScrolls = main.getNode("QuestScrolls");
            questScrolls.getNode("name").getString("Pok\u00E9Quest");
            questScrolls.getNode("item-id").getString("minecraft:book");

            loader.save(main);
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.loadConfig();
    }

    private void loadConfig() {
        NAME = main.getNode("QuestScrolls","name").getString();
        ID = main.getNode("QuestScrolls","item-id").getString();
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
}
