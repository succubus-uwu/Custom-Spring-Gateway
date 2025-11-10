package axl.gateway;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ServiceRegistrar {

    @Value("${gateway.url}")
    private String gatewayUrl;

    @Value("${server.port}")
    private int servicePort;

    @Value("${gateway.ttl-seconds}")
    private int ttlSeconds;

    @Value("${service.name}")
    private String serviceName;

    @Value("${service.routes}")
    private List<String> serviceRoutes;

    @Value("${service.open-api-path}")
    private String openApiPath;

    private final RestTemplate restTemplate = new RestTemplate();

    private ServiceDefinition definition;

    @PostConstruct
    public void register() {
        definition = ServiceDefinition.builder()
                .name(serviceName)
                .routes(serviceRoutes)
                .uri("http://localhost:" + servicePort)
                .openApiUrl("http://localhost:" + servicePort + openApiPath)
                .ttlSeconds(ttlSeconds)
                .build();

        log.info("Registering service {} with gateway {}", definition.getName(), gatewayUrl);
        restTemplate.postForObject(gatewayUrl, definition, Map.class);
    }

    @Scheduled(fixedDelayString = "${gateway.heartbeat-ms}")
    public void heartbeat() {
        if (definition != null) {
            log.info("Sending heartbeat for {}", definition.getName());
            Map<?, ?> response = restTemplate.postForObject(gatewayUrl, definition, Map.class);
            log.debug("Gateway response: {}", response);
        }
    }
}
