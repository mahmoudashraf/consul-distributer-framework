package pl.piomin.services.customer.event;

import org.springframework.context.ApplicationEvent;

public class IfGrantedLeaderEvent extends ApplicationEvent {
    public IfGrantedLeaderEvent(Object source) {
        super(source);
    }

    @Override
    public String toString() {
        return "My Granted Leader Event";
    }
}
