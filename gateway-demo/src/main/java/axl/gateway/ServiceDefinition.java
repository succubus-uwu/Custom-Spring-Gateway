package axl.gateway;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceDefinition {

    private String name;

    private List<String> routes;

    private String uri;

    private String openApiUrl;

    private int ttlSeconds;
}
