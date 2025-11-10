package axl.gateway.openapi;

import io.swagger.v3.core.util.Json;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class OpenApiController {

    private final OpenApiAggregator aggregator;
    private final OpenApiProperties props;

    private boolean checkToken(String token) {
        return props.getToken() == null || props.getToken().isEmpty() || props.getToken().equals(token);
    }

    @GetMapping("/api/v1/docs")
    public Mono<ResponseEntity<String>> getAggregatedSpec(
            @RequestHeader(value = "X-API-TOKEN", required = false) String tokenHeader,
            @RequestParam(value = "token", required = false) String tokenParam) {

        String token = tokenHeader != null ? tokenHeader : tokenParam;
        if (checkToken(token)) {
            return aggregator.aggregate().map(api -> ResponseEntity.ok(Json.pretty(api)));
        }

        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized"));
    }

    @GetMapping("/api/v1/docs-ui")
    public ResponseEntity<Resource> swaggerUi(
            @RequestHeader(value = "X-API-TOKEN", required = false) String tokenHeader,
            @RequestParam(value = "token", required = false) String tokenParam) {

        String token = tokenHeader != null ? tokenHeader : tokenParam;
        if (checkToken(token)) {
            Resource resource = new ClassPathResource("swagger-ui.html");
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(resource);
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
