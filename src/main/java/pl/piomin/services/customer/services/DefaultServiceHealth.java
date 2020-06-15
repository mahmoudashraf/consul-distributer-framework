package pl.piomin.services.customer.services;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DefaultServiceHealth implements ServiceHealth {
    private final boolean healthy;
    private final Map<String, String> meta;

    public DefaultServiceHealth() {
        this(true, null);
    }

    public DefaultServiceHealth(boolean healthy) {
        this(healthy, null);
    }

    public DefaultServiceHealth(Map<String, String> meta) {
        this(true, meta);
    }

    public DefaultServiceHealth(boolean healthy, Map<String, String> meta) {
        this.healthy = healthy;
        this.meta = meta != null ? Collections.unmodifiableMap(new HashMap<>(meta))
                : Collections.emptyMap();
    }

    @Override
    public boolean isHealthy() {
        return this.healthy;
    }

    @Override
    public Map<String, String> getMetadata() {
        return this.meta;
    }
}
