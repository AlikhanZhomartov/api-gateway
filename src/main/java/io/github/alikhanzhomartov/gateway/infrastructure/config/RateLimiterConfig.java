package io.github.alikhanzhomartov.gateway.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ServerWebExchange;

import java.security.Principal;
import java.util.Optional;

@Slf4j
@Configuration
public class RateLimiterConfig {

    private static final String ANONYMOUS = "anon";

    @Bean("routeIpKeyResolver")
    public KeyResolver routeIpKeyResolver() {
        return exchange -> {
            Route route = exchange.getAttribute(
                    ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR
            );
            String routeId = route.getId() != null ? route.getId() : "unknown";

            return exchange.getPrincipal()
                    .map(this::extractUserId)
                    .defaultIfEmpty(ANONYMOUS)
                    .map(identity -> {
                        String finalKey = identity;

                        if (ANONYMOUS.equals(finalKey)) {
                            String ip = getClientIp(exchange);
                            finalKey = routeId + ":ip:" + ip;
                        } else {
                            finalKey = routeId + ":user:" + identity;
                        }

                        return finalKey;
                    });
        };
    }

    private String extractUserId(Principal principal) {
        if (principal instanceof JwtAuthenticationToken jwt) {
            return jwt.getToken().getSubject();
        }

        return principal.getName();
    }

    private String getClientIp(ServerWebExchange exchange) {
        String IP = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (IP != null && !IP.isEmpty()) {
            return IP.split(",")[0].trim();
        }

        return Optional.ofNullable(exchange.getRequest().getRemoteAddress())
                .map(remoteAddress -> remoteAddress.getAddress().getHostAddress())
                .orElse("unknown");
    }
}
