package com.m1zark.questscrolls.data;

import com.m1zark.questscrolls.data.enums.QuestTasks;
import com.m1zark.questscrolls.utils.Item;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.Gender;
import com.pixelmonmod.pixelmon.enums.EnumGrowth;
import com.pixelmonmod.pixelmon.enums.EnumShrine;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class Task {
    private final QuestTasks task;
    private final String pokemon;
    private final EnumShrine shrine;
    private final Item item;
    private final int count;
    private final boolean boss;
    private final boolean shiny;
    private int progress;

    public boolean isComplete() {
        return progress >= count;
    }

    public void updateProgress(int value) {
        this.progress = this.progress + value;
    }
}
