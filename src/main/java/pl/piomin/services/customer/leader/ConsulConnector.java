package pl.piomin.services.customer.leader;

import java.time.Duration;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.google.common.net.HostAndPort;
import com.orbitz.consul.Consul;

@Component
public class ConsulConnector {

    private static final Logger logger = LoggerFactory.getLogger(ConsulConnector.class);

    protected Consul client;

    @Value("${spring.cloud.consul.host}")
    private String host;

    @Value("${spring.cloud.consul.ReadTimeoutScounds:10}")
    private int CONSUL_READ_TIMEOUT_SECONDS;

    @Value("${spring.cloud.consul.port}")
    private int port;

    protected HostAndPort defaultClientHostAndPort;


    public ConsulConnector() {
        super();
    }

    @PostConstruct
    public void postConstruct() {
        defaultClientHostAndPort = HostAndPort.fromParts(host, port);
        client = Consul.builder().withHostAndPort(defaultClientHostAndPort)
                .withReadTimeoutMillis(Duration.ofSeconds(CONSUL_READ_TIMEOUT_SECONDS).toMillis())
                .build();
        logger.info("ConsuleClient Created successfully");
        logger.debug("ConsuleClient Created successfully On Host: " + host + "And Port: " + port
                + " CONSUL_READ_TIMEOUT_SECONDS: " + CONSUL_READ_TIMEOUT_SECONDS);
    }

    public Consul getConsulClient() {
        return this.client;
    }


}
