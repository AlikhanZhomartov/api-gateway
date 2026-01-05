package io.github.alikhanzhomartov.gateway.infrastructure.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "gateway.defaults")
public class GatewayDefaultProperties {
    private int replenishRate;
    private int burstCapacity;
    private int requestedTokens;
    private int timeoutMillis;
    private int connectionTimeout;

    private String localCacheSize;
    private long localCacheTtlSeconds;
}