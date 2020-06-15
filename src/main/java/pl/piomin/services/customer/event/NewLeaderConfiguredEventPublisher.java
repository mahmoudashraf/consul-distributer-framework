package pl.piomin.services.customer.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Component;

@Component
public class NewLeaderConfiguredEventPublisher implements ApplicationEventPublisherAware {

    private ApplicationEventPublisher publisher;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void publish() {
        NewLeaderConfiguredEvent ce = new NewLeaderConfiguredEvent(this);
        publisher.publishEvent(ce);
    }
}
