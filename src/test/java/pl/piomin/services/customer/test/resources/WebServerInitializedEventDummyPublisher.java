package pl.piomin.services.customer.test.resources;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.server.WebServer;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Component;

@Component
public class WebServerInitializedEventDummyPublisher implements ApplicationEventPublisherAware {

    private ApplicationEventPublisher publisher;

    private WebServer webServer;
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


        webServer = new DummyWebServer();



        WebServerInitializedDummyEvent ce = new WebServerInitializedDummyEvent(webServer);
        simpleApplicationEventMulticaster.multicastEvent(ce);

    }
}
