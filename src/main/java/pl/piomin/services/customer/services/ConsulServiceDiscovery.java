package pl.piomin.services.customer.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import com.orbitz.consul.model.catalog.CatalogService;
import com.orbitz.consul.model.health.ServiceHealth;
import com.orbitz.consul.option.QueryOptions;
import pl.piomin.services.customer.dto.ServiceNodeInfo;
import pl.piomin.services.customer.leader.ConsulConnector;

@Component
@Configuration
public class ConsulServiceDiscovery {

    @Autowired
    private ConsulConnector connector;

    @Autowired
    private ServiceNodeInfo serviceNode;


    public ConsulServiceDiscovery(ConsulConnector connector, ServiceNodeInfo serviceNode) {
        super();
        this.connector = connector;
        this.serviceNode = serviceNode;
    }

    public List<ServiceDefinition> getServices() {
        List<CatalogService> services = connector.getConsulClient().catalogClient()
                .getService(serviceNode.getServiceName(), QueryOptions.BLANK).getResponse();
        List<ServiceHealth> healths = connector.getConsulClient().healthClient()
                .getAllServiceInstances(serviceNode.getServiceName(), QueryOptions.BLANK)
                .getResponse();

        return services.stream().filter(h -> (!Helper.isLocalService(h, serviceNode)))
                .map(service -> newService(serviceNode.getServiceName(), service, healths))
                .collect(Collectors.toList());
    }

    private boolean isHealthy(ServiceHealth serviceHealth) {
        return serviceHealth.getChecks().stream()
                .allMatch(check -> check.getStatus().equals("passing"));
    }

    private ServiceDefinition newService(String serviceName, CatalogService service,
            List<ServiceHealth> serviceHealthList) {
        Map<String, String> meta = new HashMap<>();
        Helper.ifNotEmpty(service.getServiceId(),
                val -> meta.put(ServiceDefinition.SERVICE_META_ID, val));
        Helper.ifNotEmpty(service.getServiceName(),
                val -> meta.put(ServiceDefinition.SERVICE_META_NAME, val));
        Helper.ifNotEmpty(service.getNode(), val -> meta.put("service.node", val));

        // Consul < 1.0.7 does not have a concept of meta-data so meta is
        // retrieved using tags
        List<String> tags = service.getServiceTags();
        if (tags != null) {
            for (String tag : service.getServiceTags()) {
                String[] items = tag.split("=");
                if (items.length == 1) {
                    meta.put(items[0], items[0]);
                } else if (items.length == 2) {
                    meta.put(items[0], items[1]);
                }
            }
        }

        service.getServiceMeta().entrySet().stream().filter(
                e -> (e != null && e.getValue() != null && !StringUtils.isBlank(e.getValue())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
                .forEach(meta::put);

        return new DefaultServiceDefinition(serviceName, service.getServiceAddress(),
                service.getServicePort(), meta,
                new DefaultServiceHealth(serviceHealthList.stream()
                        .filter(h -> (h.getService().getId().equals(service.getServiceId())))
                        .allMatch(this::isHealthy)));
    }



}
