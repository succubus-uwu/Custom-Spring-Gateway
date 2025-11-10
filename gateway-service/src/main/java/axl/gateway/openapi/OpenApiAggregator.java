package axl.gateway.openapi;

import axl.gateway.service.ServiceRegistry;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.parser.OpenAPIV3Parser;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
@Setter
@RequiredArgsConstructor
public class OpenApiAggregator {

    private final ServiceRegistry registry;
    private final OpenApiProperties props;
    private final WebClient webClient = WebClient.create();

    private OpenAPI documentation = null;

    public Mono<OpenAPI> aggregate() {
        if (documentation != null) {
            return Mono.just(documentation);
        }

        return Flux.fromIterable(registry.getServices().values())
                .flatMap(service -> {
                    if (service.getOpenApiUrl() == null) return Mono.empty();
                    return webClient.get()
                            .uri(service.getOpenApiUrl())
                            .retrieve()
                            .bodyToMono(String.class)
                            .map(specJson -> new OpenAPIV3Parser().readContents(specJson).getOpenAPI())
                            .map(this::removeUserIdHeaders) // <-- удаляем X-User-Id здесь
                            .onErrorResume(e -> {
                                log.error("Failed to fetch OpenAPI for {}: {}", service.getName(), e.getMessage());
                                return Mono.empty();
                            });
                })
                .collectList()
                .map(list -> {
                    OpenAPI aggregated = new OpenAPI()
                            .paths(new Paths())
                            .components(new Components())
                            .info(new Info()
                                    .title(props.getTitle())
                                    .description(props.getDescription())
                                    .version(props.getVersion())
                            );

                    if (props.getServers() != null) {
                        props.getServers().forEach(s ->
                                aggregated.addServersItem(new Server()
                                        .url(s.getUrl())
                                        .description(s.getDescription()))
                        );
                    }

                    list.forEach(parsed -> {
                        if (parsed == null) return;

                        if (parsed.getPaths() != null) {
                            parsed.getPaths().forEach((k, v) -> {
                                if (aggregated.getPaths().containsKey(k)) {
                                    log.warn("Path conflict: {}", k);
                                } else {
                                    aggregated.getPaths().addPathItem(k, v);
                                }
                            });
                        }

                        Components src = parsed.getComponents();
                        if (src != null) {
                            if (src.getSchemas() != null) {
                                src.getSchemas().forEach((k, v) -> {
                                    if (aggregated.getComponents().getSchemas() != null &&
                                            aggregated.getComponents().getSchemas().containsKey(k)) {
                                        log.warn("Schema conflict: {}", k);
                                    } else {
                                        aggregated.getComponents().addSchemas(k, v);
                                    }
                                });
                            }
                            if (src.getParameters() != null) {
                                src.getParameters().forEach((k, v) -> {
                                    if ("X-User-Id".equalsIgnoreCase(v.getName())) return;
                                    if (aggregated.getComponents().getParameters() != null &&
                                            aggregated.getComponents().getParameters().containsKey(k)) {
                                        log.warn("Parameter conflict: {}", k);
                                    } else {
                                        aggregated.getComponents().addParameters(k, v);
                                    }
                                });
                            }
                            if (src.getResponses() != null) {
                                src.getResponses().forEach((k, v) -> {
                                    if (aggregated.getComponents().getResponses() != null &&
                                            aggregated.getComponents().getResponses().containsKey(k)) {
                                        log.warn("Response conflict: {}", k);
                                    } else {
                                        aggregated.getComponents().addResponses(k, v);
                                    }
                                });
                            }
                            if (src.getSecuritySchemes() != null) {
                                src.getSecuritySchemes().forEach((k, v) -> {
                                    if (aggregated.getComponents().getSecuritySchemes() != null &&
                                            aggregated.getComponents().getSecuritySchemes().containsKey(k)) {
                                        log.warn("SecurityScheme conflict: {}", k);
                                    } else {
                                        aggregated.getComponents().addSecuritySchemes(k, v);
                                    }
                                });
                            }
                        }

                        if (parsed.getTags() != null) {
                            parsed.getTags().forEach(t -> {
                                if (aggregated.getTags() != null &&
                                        aggregated.getTags().stream().anyMatch(existing -> existing.getName().equals(t.getName()))) {
                                    log.warn("Tag conflict: {}", t.getName());
                                } else {
                                    aggregated.addTagsItem(t);
                                }
                            });
                        }

                        if (parsed.getSecurity() != null) {
                            parsed.getSecurity().forEach(aggregated::addSecurityItem);
                        }
                    });

                    documentation = aggregated;
                    return aggregated;
                });
    }

    private OpenAPI removeUserIdHeaders(OpenAPI openAPI) {
        if (openAPI == null) return null;

        if (openAPI.getPaths() != null) {
            openAPI.getPaths().values().forEach(pathItem ->
                    pathItem.readOperations().forEach(operation -> {
                        if (operation.getParameters() != null) {
                            operation.setParameters(
                                    operation.getParameters().stream()
                                            .filter(p -> !"X-User-Id".equalsIgnoreCase(p.getName()))
                                            .collect(Collectors.toList())
                            );
                        }
                    })
            );
        }

        if (openAPI.getComponents() != null && openAPI.getComponents().getParameters() != null) {
            Map<String, Parameter> filtered = openAPI.getComponents().getParameters().entrySet().stream()
                    .filter(e -> !"X-User-Id".equalsIgnoreCase(
                            Objects.requireNonNullElse(e.getValue().getName(), "")
                    ))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            openAPI.getComponents().setParameters(filtered);
        }

        return openAPI;
    }
}
