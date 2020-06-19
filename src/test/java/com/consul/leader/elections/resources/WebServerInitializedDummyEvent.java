package com.consul.leader.elections.resources;

import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.boot.web.server.WebServer;

public class WebServerInitializedDummyEvent extends WebServerInitializedEvent {


    WebServerApplicationContext webServerContext;

    public WebServerInitializedDummyEvent(WebServer source) {
        super(source);
        webServerContext = new DummyWebServerContext();
    }

    @Override
    public String toString() {
        return "My Granted Leader Event";
    }

    @Override
    public WebServerApplicationContext getApplicationContext() {
        // TODO Auto-generated method stub
        return webServerContext;
    }
}
