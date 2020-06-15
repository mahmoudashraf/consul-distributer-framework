package pl.piomin.services.customer.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Component;

@Component
public class IfGrantedLeaderEventPublisher implements ApplicationEventPublisherAware {

    private ApplicationEventPublisher publisher;

    @Autowired
    private SimpleApplicationEventMulticaster simpleApplicationEventMulticaster;

    @Autowired

    @Qualifier(value = "taskExecutor")
    private AsyncTaskExecutor asyncTaskExecutor;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void publish() {
        simpleApplicationEventMulticaster.setTaskExecutor(asyncTaskExecutor);
        IfGrantedLeaderEvent ce = new IfGrantedLeaderEvent(this);
        simpleApplicationEventMulticaster.multicastEvent(ce);

    }
}
