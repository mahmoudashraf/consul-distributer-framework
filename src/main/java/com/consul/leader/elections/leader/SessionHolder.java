package com.consul.leader.elections.leader;

import java.util.LinkedList;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.orbitz.consul.Consul;
import com.orbitz.consul.model.session.ImmutableSession;
import com.orbitz.consul.model.session.Session;

public class SessionHolder implements Runnable {

    private static final String TTL_TEMPLATE = "%ss";
    private Consul client;
    private String id;
    private LinkedList<Supplier<Boolean>> liveChecks = new LinkedList();
    private long ttl;
    private boolean shutdown = false;
    private static final Logger logger = LoggerFactory.getLogger(LeaderObserver.class);

    public SessionHolder(Consul client, String service, long ttl) {
        this.client = client;
        this.ttl = ttl;
        final Session session = ImmutableSession.builder().name(service)
                .ttl(String.format(TTL_TEMPLATE, ttl)).build();
        id = client.sessionClient().createSession(session).getId();
        logger.info("Session Created ID:" + id);
        Thread upkeep = new Thread(this);
        upkeep.setDaemon(true);
        upkeep.start();
    }

    public String getId() {
        return id;
    }

    public void registerKeepAlive(Supplier<Boolean> liveCheck) {
        liveChecks.add(liveCheck);
    }

    @Override
    public synchronized void run() {
        // don't start renewing immediately
        try {
            wait(ttl / 2 * 1000);
        } catch (InterruptedException e) {
        }
        while (!isShutdown()) {
            if (liveChecks.isEmpty() || liveChecks.stream().allMatch(Supplier::get)) {
                client.sessionClient().renewSession(getId());
                logger.info("Leader Session Renewed:" + id);
            }
            try {
                wait(ttl / 2 * 1000);
            } catch (InterruptedException e) {
                logger.debug("Session renewed : InterruptedException happened");
            }
        }
        logger.info("Leader Session is shutdown:");
    }

    public synchronized boolean isShutdown() {
        return shutdown;
    }

    public synchronized void close() {
        shutdown = true;
        notify();
        client.sessionClient().destroySession(getId());
        logger.info("Session is closed");
    }
}
