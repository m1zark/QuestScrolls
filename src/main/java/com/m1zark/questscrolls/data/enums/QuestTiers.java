package com.m1zark.questscrolls.data.enums;

import com.pixelmonmod.pixelmon.RandomHelper;
import lombok.Getter;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;

@Getter
public enum QuestTiers {
    COMMON (1, TextColors.WHITE, "&f", "Common", 3, 5, 2),
    UNCOMMON (2, TextColors.AQUA, "&b", "Uncommon", 3, 5, 3),
    RARE (3, TextColors.BLUE, "&1", "Rare", 5, 7, 4),
    EPIC (4, TextColors.LIGHT_PURPLE, "&d", "Epic", 7, 9, 4),
    LEGENDARY (5, TextColors.GOLD, "&e", "Legendary", 9, 12, 5),
    EVENT (6, TextColors.DARK_GREEN, "&2", "Event", 10, 10, 4);

    private final int id;
    private final TextColor color;
    private final String colorCode;
    private final String name;
    private final int min_tasks;
    private final int max_tasks;
    private final int max_rewards;

    QuestTiers(int id, TextColor color, String colorCode, String name, int min_tasks, int max_tasks, int max_rewards) {
        this.id = id;
        this.color = color;
        this.colorCode = colorCode;
        this.name = name;
        this.min_tasks = min_tasks;
        this.max_tasks = max_tasks;
        this.max_rewards = max_rewards;
    }

    public static QuestTiers getTierFromID(int index) {
        for(QuestTiers task : QuestTiers.values()) {
            if(task.getId() == index) return task;
        }

        return null;
    }

    public static QuestTiers getRandomTier() {
        return QuestTiers.getTierFromID(RandomHelper.getRandomNumberBetween(1,7));
    }
}
