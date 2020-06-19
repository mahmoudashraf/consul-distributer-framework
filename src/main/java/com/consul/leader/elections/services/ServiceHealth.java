package com.consul.leader.elections.services;

import java.util.Map;

public interface ServiceHealth {

    /**
     * Gets a key/value metadata associated with the service.
     */
    Map<String, String> getMetadata();

    /**
     * States if the service is healthy or not
     */
    boolean isHealthy();
}
