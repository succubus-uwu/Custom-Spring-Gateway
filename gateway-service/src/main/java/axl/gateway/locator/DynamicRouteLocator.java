package axl.gateway.locator;

import axl.gateway.service.ServiceRegistry;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.cloud.gateway.support.NameUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class DynamicRouteLocator {

    @Bean
    public RouteDefinitionLocator dynamicRoutes(ServiceRegistry registry) {
        return () -> {
            List<RouteDefinition> defs = new ArrayList<>();

            registry.getServices().forEach((name, service) -> {
                service.getRoutes().forEach(path -> {
                    RouteDefinition def = new RouteDefinition();
                    def.setId(name + "_" + path);
                    def.setUri(URI.create(service.getUri()));

                    PredicateDefinition predicate = new PredicateDefinition();
                    predicate.setName("Path");
                    predicate.addArg(NameUtils.GENERATED_NAME_PREFIX + "0", path);

                    def.setPredicates(List.of(predicate));
                    defs.add(def);
                });
            });

            return Flux.fromIterable(defs);
        };
    }
}
