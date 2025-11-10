package axl.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtUserHeaderFilter implements GlobalFilter, Ordered {

    @Value("${jwt.access.name}")
    private String name;

    @Value("${jwt.access.secret}")
    private String accessSecret;

    private SecretKey accessKey;

    @PostConstruct
    public void init() {
        if (accessSecret == null || accessSecret.isEmpty() || name == null || name.isEmpty())
            return;

        this.accessKey = Keys.hmacShaKeyFor(accessSecret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest.Builder requestBuilder = exchange.getRequest().mutate()
                .headers(httpHeaders -> httpHeaders.remove("X-User-Id"));

        if (accessKey != null) {
            String token = null;

            if (exchange.getRequest().getCookies().getFirst(name) != null) {
                //noinspection DataFlowIssue
                token = exchange.getRequest().getCookies().getFirst(name).getValue();
            }

            if (token != null && !token.isEmpty()) {
                try {
                    Claims claims = Jwts.parser()
                            .verifyWith(accessKey)
                            .build()
                            .parseSignedClaims(token)
                            .getPayload();

                    String userId = claims.getSubject();
                    if (userId != null) {
                        requestBuilder.header("X-User-Id", userId);
                    }
                } catch (Exception ignored) {
                }
            }
        }

        return chain.filter(exchange.mutate().request(requestBuilder.build()).build());
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
