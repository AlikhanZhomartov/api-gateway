package io.github.alikhanzhomartov.gateway.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RouteDefinitionDTO {
    @NotBlank(message = "ID is required")
    private String id;

    @NotBlank(message = "URI is required")
    private String uri;

    @NotBlank
    @Pattern(regexp = "/.*", message = "Path must start with /")
    private String path;

    private String stripPrefix;

    private Integer order = 0;

    private ResilienceConfig config;

    private LocalCacheConfig cacheConfig;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ResilienceConfig {
        private Integer replenishRate;
        private Integer burstCapacity;
        private Integer requestedTokens;
        private Integer timeoutMillis;
        private Integer connectionTimeout;

        private Integer maxConcurrentCalls;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LocalCacheConfig {
        private boolean enabled;
        private Long timeToLiveSeconds;
        private String localCacheSize;
    }
}
