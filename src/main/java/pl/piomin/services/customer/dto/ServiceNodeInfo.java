package pl.piomin.services.customer.dto;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ServiceNodeInfo {

    private static final Logger logger = LoggerFactory.getLogger(ServiceNodeInfo.class);

    @EventListener(WebServerInitializedEvent.class)
    public void bind(WebServerInitializedEvent event) {
        this.port = event.getWebServer().getPort();
        logger.info("WebServerInitializedEvent recieved and port is " + this.port);
    }

    private String ipAddress;
    private int port;

    @Value("${spring.cloud.consul.discovery.instance-id}")
    private String nodeId;

    @Value("${spring.application.name}")
    private String serviceName;
    private boolean capableLeader = true;

    public ServiceNodeInfo() {

        try {
            this.ipAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
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

    @Override
    public String toString() {
        return "{\"host\":\"" + this.ipAddress + "\",\"port\":\"" + this.port + "\",\"nodeId\":\""
                + this.nodeId + "\"}";
    }

    public String getServiceName() {
        return this.serviceName;
    }

    public void setServiceName(String serviceNode) {
        this.serviceName = serviceNode;
    }

    public void disableLeadership() {
        this.capableLeader = false;
        logger.info("Leadership  is disabled");
    }

    public void enableLeadership() {
        this.capableLeader = true;
        logger.info("Leadership  is enabled");
    }

    public boolean isCapableLeader() {
        return this.capableLeader;
    }
}
