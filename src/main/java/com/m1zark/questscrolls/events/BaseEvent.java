package com.m1zark.questscrolls.events;

import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;

public abstract class BaseEvent extends AbstractEvent implements Cancellable {
    private boolean cancelled = false;

    public BaseEvent() {
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public abstract Cause getCause();
}
