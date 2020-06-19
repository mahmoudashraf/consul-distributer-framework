package com.consul.leader.elections.event;

import org.springframework.context.ApplicationEvent;

public class NewLeaderConfiguredEvent extends ApplicationEvent {
    public NewLeaderConfiguredEvent(Object source) {
        super(source);
    }

    @Override
    public String toString() {
        return "My New Leader Event";
    }
}
