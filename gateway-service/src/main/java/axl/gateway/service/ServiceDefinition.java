package axl.gateway.service;

import lombok.*;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = "lastUpdated")
public class ServiceDefinition {

    private String name;

    private List<String> routes;

    private String uri;

    private String openApiUrl;

    private int ttlSeconds;

    private Instant lastUpdated;
}

