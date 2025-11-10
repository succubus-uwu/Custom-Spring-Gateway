package axl.gateway.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Component
@RequiredArgsConstructor
public class ServiceRegistry {

    private final Map<String, ServiceDefinition> services = new ConcurrentHashMap<>();

    public boolean register(ServiceDefinition service) {
        ServiceDefinition old = services.get(service.getName());

        service.setLastUpdated(Instant.now());
        services.put(service.getName(), service);

        return !service.equals(old);
    }

    public void remove(String name) {
        services.remove(name);
    }

    public ServiceDefinition get(String name) {
        return services.get(name);
    }
}
