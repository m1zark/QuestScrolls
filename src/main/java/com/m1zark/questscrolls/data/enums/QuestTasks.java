package com.m1zark.questscrolls.data.enums;

import com.pixelmonmod.pixelmon.RandomHelper;
import lombok.Getter;

import java.util.Optional;
import java.util.Random;

@Getter
public enum QuestTasks {
    CATCH (1, "Catch"),
    DEFEAT (2, "Defeat"),
    LEVEL (3, "Level"),
    BREED (4, "Breed"),
    EVOLVE (5, "Evolve"),
    COLLECT (6, "Collect"),
    SHRINE (7, "Activate");

    private final int id;
    private final String task;

    QuestTasks(int id, String name) {
        this.id = id;
        this.task = name;
    }

    public static QuestTasks getTaskFromID(int index) {
        for(QuestTasks task : QuestTasks.values()) {
            if(task.getId() == index) return task;
        }

        return null;
    }

    public static Optional<QuestTasks> getTaskFromName(String name) {
        for(QuestTasks task : QuestTasks.values()) {
            if(task.getTask().toLowerCase().equals(name.toLowerCase())) return Optional.of(task);
        }

        return Optional.empty();
    }

    public static QuestTasks getRandomTask() {
        return QuestTasks.getTaskFromID(RandomHelper.getRandomNumberBetween(1,7));
    }

}
