package com.consul.leader.elections.services;

import java.util.Map;

public interface ServiceDefinition {
    String SERVICE_META_PREFIX = "service.";

    // default service meta-data keys
    String SERVICE_META_ID = "service.id";
    String SERVICE_META_NAME = "service.name";
    String SERVICE_META_HOST = "service.host";
    String SERVICE_META_PORT = "service.port";
    String SERVICE_META_ZONE = "service.zone";
    String SERVICE_META_PROTOCOL = "service.protocol";
    String SERVICE_META_PATH = "service.path";

    /**
     * Gets the service id.
     */
    String getId();

    /**
     * Gets the service name.
     */
    String getName();

    /**
     * Gets the IP or hostname of the server hosting the service.
     */
    String getHost();

    /**
     * Gets the port number of the server hosting the service.
     */
    int getPort();

    /**
     * Gets the health.
     */
    ServiceHealth getHealth();

    /**
     * Gets a key/value metadata associated with the service.
     */
    Map<String, String> getMetadata();

    /**
     * Check if a service definition matches.
     */
    default boolean matches(ServiceDefinition other) {
        if (this.equals(other)) {
            return true;
        }

        return getPort() == other.getPort() && Helper.matches(getName(), other.getName())
                && Helper.matches(getId(), other.getId())
                && Helper.matches(getHost(), other.getHost());
    }
}
