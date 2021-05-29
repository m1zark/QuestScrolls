package com.m1zark.questscrolls;

import com.google.inject.Inject;
import com.m1zark.questscrolls.commands.CommandManager;
import com.m1zark.questscrolls.config.*;
import com.m1zark.questscrolls.listeners.PixelmonListeners;
import com.m1zark.questscrolls.listeners.PlayerListeners;
import com.m1zark.questscrolls.storage.DataSource;
import com.pixelmonmod.pixelmon.Pixelmon;
import lombok.Getter;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStoppingEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;

import java.nio.file.Path;
import java.util.Optional;

@Getter
@Plugin(id=QSInfo.ID, name=QSInfo.NAME, version=QSInfo.VERSION, description=QSInfo.DESCRIPTION, authors = "m1zark")
public class QuestScrolls {
    @Inject private Logger logger;
    @Inject private PluginContainer pluginContainer;
    private static QuestScrolls instance;
    private DataSource sql;
    @Inject @ConfigDir(sharedRoot = false) private Path configDir;
    private ConfigMain configMain;
    private ConfigQuests configQuests;
    //private ConfigCooldowns configCooldowns;
    private ConfigMessages configMessages;
    private ConfigTiers configTiers;

    private boolean enabled = true;

    @Listener public void onServerStart(GameInitializationEvent event) {
        instance = this;

        QSInfo.startup();
        this.enabled = QSInfo.dependencyCheck();

        if(enabled) {
            //this.sql = new DataSource("Quests_PlayerData");
            //this.sql.createTables();

            this.configMain = new ConfigMain();
            this.configQuests = new ConfigQuests();
            //this.configCooldowns = new ConfigCooldowns();
            this.configMessages = new ConfigMessages();
            this.configTiers = new ConfigTiers();

            new CommandManager().registerCommands(this);
            Pixelmon.EVENT_BUS.register(new PixelmonListeners());
            Sponge.getEventManager().registerListeners(this, new PlayerListeners());
        }
    }

    @Listener public void onReload(GameReloadEvent e) {
        if (this.enabled) {
            this.configMain = new ConfigMain();
            this.configQuests = new ConfigQuests();
            //this.configCooldowns = new ConfigCooldowns();
            this.configMessages = new ConfigMessages();
            this.configTiers = new ConfigTiers();

            getConsole().ifPresent(console -> console.sendMessages(Text.of(QSInfo.PREFIX, "All configurations have been reloaded")));
        }
    }

    @Listener public void onServerStop(GameStoppingEvent e) {
        try {
            //this.sql.shutdown();
        } catch (Exception error) {
            error.printStackTrace();
        }
    }

    public static QuestScrolls getInstance() {
        return instance;
    }

    public Optional<ConsoleSource> getConsole() {
        return Optional.ofNullable(Sponge.isServerAvailable() ? Sponge.getServer().getConsole() : null);
    }
}
