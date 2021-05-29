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

public class ConfigMessages {
    private static ConfigurationLoader<CommentedConfigurationNode> loader;
    private static CommentedConfigurationNode main;

    public ConfigMessages() {
        this.load();
    }

    private void load() {
        Path configFile = Paths.get(QuestScrolls.getInstance().getConfigDir() + "/messages.conf");
        loader = HoconConfigurationLoader.builder().setPath(configFile).build();

        try {
            if (!Files.exists(QuestScrolls.getInstance().getConfigDir())) Files.createDirectory(QuestScrolls.getInstance().getConfigDir());
            if (!Files.exists(configFile)) Files.createFile(configFile);
            if (main == null) main = loader.load(ConfigurationOptions.defaults().setShouldCopyDefaults(true));

            CommentedConfigurationNode messages = main.getNode("Messages");
            messages.getNode("broadcast").getString("&3&lPok\u00E9Quest &e»&r &b{player} &7just completed a {tier} quest!");
            messages.getNode("reward").getString("&3&lPok\u00E9Quest &e»&r &7You received &b{rewards} &7for completing this quest.");
            messages.getNode("inventory-full").getString("&3&lPok\u00E9Quest &e»&r &7You could not receive &b{reward} &7due to a full inventory.");
            messages.getNode("claim-owner").getString("&3&lPok\u00E9Quest &e»&r &7You have claimed ownership of this quest. Only you can work towards its completion.");
            messages.getNode("has-owner").getString("&3&lPok\u00E9Quest &e»&r &7This quest has been claimed by someone else.");

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

    public static String getMessages(String value) { return main.getNode((Object[])value.split("\\.")).getString(); }
}
