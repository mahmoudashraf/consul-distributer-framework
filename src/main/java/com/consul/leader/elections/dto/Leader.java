package com.consul.leader.elections.dto;

import org.apache.commons.lang3.StringUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class Leader {

    public Leader() {
        super();
    }

    public Leader(String ip, int port, String nodeId) {
        super();
        this.ipAddress = ip;
        this.port = port;
        this.nodeId = nodeId;
    }

    public String getIPAddress() {
        return ipAddress;
    }

    public void setIPAddress(String ip) {
        this.ipAddress = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    private String ipAddress;
    private int port;
    private String nodeId;
    private String sessionId;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public String toString() {
        return "{\"host\":\"" + this.ipAddress + "\",\"port\":\"" + this.port + "\",\"nodeId\":\""
                + this.nodeId + "\"}";
    }

    @JsonIgnore
    public void reset() {
        this.ipAddress = "";
        this.port = -1;
        this.nodeId = "";
        this.sessionId = "";
    }

    @JsonIgnore
    public boolean isEmpty() {
        if (StringUtils.isBlank(this.ipAddress) || StringUtils.isBlank(this.nodeId)
                || this.port < 0)
            return true;

        return false;
    }
}
