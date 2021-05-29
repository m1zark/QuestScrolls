package com.m1zark.questscrolls.events;

import com.m1zark.questscrolls.data.Quests;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.spongepowered.api.event.cause.Cause;

@Getter
@RequiredArgsConstructor
public class QuestEvent extends BaseEvent {
    private final String player;
    private final Quests quest;
    @NonNull private final Cause cause;

    @Override
    public Cause getCause() {
        return this.cause;
    }
}
