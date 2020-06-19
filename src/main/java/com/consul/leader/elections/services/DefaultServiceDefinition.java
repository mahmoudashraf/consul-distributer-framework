package com.consul.leader.elections.services;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DefaultServiceDefinition implements ServiceDefinition {
    private ServiceHealth DEFAULT_SERVICE_HEALTH = new DefaultServiceHealth();

    private final String name;
    private final String host;
    private final int port;
    private final Map<String, String> meta;
    private final ServiceHealth health;

    public DefaultServiceDefinition(String name, String host, int port) {
        this.name = name;
        this.host = host;
        this.port = port;
        this.meta = Collections.emptyMap();
        this.health = DEFAULT_SERVICE_HEALTH;
    }

    public DefaultServiceDefinition(String name, String host, int port, ServiceHealth health) {
        this.name = name;
        this.host = host;
        this.port = port;
        this.meta = Collections.emptyMap();
        this.health = health;
    }

    public DefaultServiceDefinition(String name, String host, int port, Map<String, String> meta) {
        this.name = name;
        this.host = host;
        this.port = port;
        this.meta = meta != null ? Collections.unmodifiableMap(new HashMap<>(meta))
                : Collections.emptyMap();
        this.health = DEFAULT_SERVICE_HEALTH;
    }

    public DefaultServiceDefinition(String name, String host, int port, Map<String, String> meta,
            ServiceHealth health) {
        this.name = name;
        this.host = host;
        this.port = port;
        this.meta = meta != null ? Collections.unmodifiableMap(new HashMap<>(meta))
                : Collections.emptyMap();
        this.health = health;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public ServiceHealth getHealth() {
        return health;
    }

    @Override
    public Map<String, String> getMetadata() {
        return this.meta;
    }

    @Override
    public String toString() {
        return "DefaultServiceCallService[" + name + "@" + host + ":" + port + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DefaultServiceDefinition that = (DefaultServiceDefinition) o;

        if (port != that.port) {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        return host != null ? host.equals(that.host) : that.host == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (host != null ? host.hashCode() : 0);
        result = 31 * result + port;
        return result;
    }

    @Override
    public String getId() {
        // TODO Auto-generated method stub
        return null;
    }
}
