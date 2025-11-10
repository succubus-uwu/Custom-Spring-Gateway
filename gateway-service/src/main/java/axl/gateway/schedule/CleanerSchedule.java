package axl.gateway.schedule;

import axl.gateway.service.ServiceRegistry;
import axl.gateway.openapi.OpenApiAggregator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Iterator;

@Slf4j
@Component
@RequiredArgsConstructor
public class CleanerSchedule {

    private final ServiceRegistry registry;
    private final OpenApiAggregator openApiAggregator;

    @Scheduled(fixedDelay = 5000)
    public void cleanupExpiredServices() {
        Instant now = Instant.now();

        Iterator<String> it = registry.getServices().keySet().iterator();
        while (it.hasNext()) {
            String name = it.next();
            var service = registry.get(name);

            if (service != null && service.getLastUpdated() != null) {
                long age = now.getEpochSecond() - service.getLastUpdated().getEpochSecond();
                if (age > service.getTtlSeconds()) {
                    it.remove();
                    openApiAggregator.setDocumentation(null);
                    log.warn("Service `{}` expired", name);
                }
            }
        }
    }
}
