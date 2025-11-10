package axl.gateway.service;

import axl.gateway.openapi.OpenApiAggregator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/internal/service")
@RequiredArgsConstructor
public class ServiceController {

    private final ServiceRegistry registry;

    private final ApplicationEventPublisher publisher;

    private final OpenApiAggregator openApiAggregator;

    @PostMapping
    public Map<?, ?> add(@RequestBody ServiceDefinition service) {
        boolean changed = registry.register(service);
        if (changed) {
            publisher.publishEvent(new RefreshRoutesEvent(this));
            log.info("Service `{}` registered/updated", service.getName());
        } else {
            openApiAggregator.setDocumentation(null);
            log.info("Service `{}` has a heartbeat", service.getName());
        }
        return Map.of("status", "200", "message", "Service `" + service.getName() + "` registered");
    }

    @DeleteMapping("/{name}")
    public Map<?, ?> delete(@PathVariable String name) {
        registry.remove(name);
        publisher.publishEvent(new RefreshRoutesEvent(this));
        log.warn("Service `{}` removed", name);
        return Map.of("status", "200", "message", "Service `" + name + "` removed");
    }

    @GetMapping
    public Object list() {
        log.info("Listing all registered services (count={}):", registry.getServices().size());
        for (ServiceDefinition service: registry.getServices().values()) {
            log.info("- service `{}` registered. updated={}", service.getName(), service.getLastUpdated().toString());
        }
        return registry.getServices().values();
    }
}
