package io.github.alikhanzhomartov.gateway.core.mapper;

import io.github.alikhanzhomartov.gateway.api.dto.RouteDefinitionDTO;
import io.github.alikhanzhomartov.gateway.infrastructure.config.properties.GatewayDefaultProperties;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.*;

@Component
public class RouteDefinitionMapper {

    private final GatewayDefaultProperties defaultProps;

    public RouteDefinitionMapper(GatewayDefaultProperties defaultProps) {
        this.defaultProps = defaultProps;
    }

    public RouteDefinition convertToRouteDefinition(RouteDefinitionDTO dto) {
        RouteDefinition routeDefinition = new RouteDefinition();

        routeDefinition.setId(dto.getId());
        routeDefinition.setUri(URI.create(dto.getUri()));
        routeDefinition.setOrder(dto.getOrder() != null ? dto.getOrder() : 0);

        PredicateDefinition predicateDefinition = new PredicateDefinition("Path=" + dto.getPath());
        routeDefinition.setPredicates(List.of(predicateDefinition));

        List<FilterDefinition> filterDefinitions = new ArrayList<>();

        if (dto.getStripPrefix() != null) {
            filterDefinitions.add(new FilterDefinition("StripPrefix=" + dto.getStripPrefix()));
        }

        var config = Optional.ofNullable(dto.getConfig()).orElse(new RouteDefinitionDTO.ResilienceConfig());
        var cacheConfig = Optional.ofNullable(dto.getCacheConfig()).orElse(new RouteDefinitionDTO.LocalCacheConfig());

        // filters
        if (cacheConfig.isEnabled()) {
            FilterDefinition cacheDefinition = cacheDefinition(cacheConfig);
            filterDefinitions.add(cacheDefinition);
        }

        FilterDefinition redisLimiterDefinition = redisLimiterDefinition(config);
        filterDefinitions.add(redisLimiterDefinition);

        if (config.getMaxConcurrentCalls() != null && config.getMaxConcurrentCalls() != 0) {
            FilterDefinition bulkheadDefinition = bulkheadDefinition(dto.getId(), config.getMaxConcurrentCalls());
            filterDefinitions.add(bulkheadDefinition);
        }

        FilterDefinition cbDefinition = cbDefinition(dto.getId());
        filterDefinitions.add(cbDefinition);

        routeDefinition.setFilters(filterDefinitions);

        // metadata
        Map<String, Object> metadata = getMetaData(config);
        routeDefinition.setMetadata(metadata);

        return routeDefinition;
    }

    private FilterDefinition cacheDefinition(RouteDefinitionDTO.LocalCacheConfig config) {
        FilterDefinition definition = new FilterDefinition();

        String timeToLive = getOrDefault(config.getTimeToLiveSeconds(), defaultProps.getLocalCacheTtlSeconds()) + "s";
        String sizeLimit = getOrDefault(config.getLocalCacheSize(), defaultProps.getLocalCacheSize());

        definition.setName("LocalResponseCache");

        definition.addArg("timeToLive", timeToLive);
        definition.addArg("size", sizeLimit);

        return definition;
    }

    private FilterDefinition redisLimiterDefinition(RouteDefinitionDTO.ResilienceConfig config) {
        FilterDefinition definition = new FilterDefinition();

        String replenish = String.valueOf(
                getOrDefault(config.getReplenishRate(), defaultProps.getReplenishRate())
        );
        String burst = String.valueOf(
                getOrDefault(config.getBurstCapacity(), defaultProps.getBurstCapacity())
        );
        String tokens = String.valueOf(
                getOrDefault(config.getRequestedTokens(), defaultProps.getRequestedTokens())
        );

        definition.setName("RequestRateLimiter");
        definition.addArg("key-resolver", "#{@routeIpKeyResolver}");
        definition.addArg("redis-rate-limiter.replenishRate", replenish);
        definition.addArg("redis-rate-limiter.burstCapacity", burst);
        definition.addArg("redis-rate-limiter.requestedTokens", tokens);

        return definition;
    }

    private FilterDefinition bulkheadDefinition(String routeId, int maxConcurrentCalls) {
        FilterDefinition definition = new FilterDefinition();

        definition.setName("DynamicBulkhead");

        definition.addArg("name", routeId);
        definition.addArg("maxConcurrentCalls", String.valueOf(maxConcurrentCalls));

        return definition;
    }

    private FilterDefinition cbDefinition(String routeId) {
        FilterDefinition definition = new FilterDefinition();

        definition.setName("CircuitBreaker");
        definition.addArg("name", routeId);
        definition.addArg("fallbackUri", "forward:/fallback");

        return definition;
    }

    private Map<String, Object> getMetaData(RouteDefinitionDTO.ResilienceConfig config) {
        int timeout = getOrDefault(config.getTimeoutMillis(), defaultProps.getTimeoutMillis());
        int connectionTimeout = getOrDefault(config.getConnectionTimeout(), defaultProps.getConnectionTimeout());

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("response-timeout", timeout);
        metadata.put("connect-timeout", connectionTimeout);

        return metadata;
    }

    private <T> T getOrDefault(T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }
}
