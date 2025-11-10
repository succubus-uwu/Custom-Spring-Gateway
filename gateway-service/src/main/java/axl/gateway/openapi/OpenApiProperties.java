package axl.gateway.openapi;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "gateway.openapi")
public class OpenApiProperties {

    private String title;

    private String description;

    private String version;

    private List<ServerConfig> servers;

    private String token;

    @Data
    public static class ServerConfig {

        private String url;

        private String description;
    }
}
