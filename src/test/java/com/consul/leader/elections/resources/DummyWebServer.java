package com.consul.leader.elections.resources;

import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.server.WebServerException;

public class DummyWebServer implements WebServer {

    @Override
    public void start() throws WebServerException {
        // TODO Auto-generated method stub

    }

    @Override
    public void stop() throws WebServerException {
        // TODO Auto-generated method stub

    }

    @Override
    public int getPort() {
        // TODO Auto-generated method stub
        return 8080;
    }

}
