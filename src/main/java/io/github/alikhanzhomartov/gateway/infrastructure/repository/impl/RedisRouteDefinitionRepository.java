package io.github.alikhanzhomartov.gateway.infrastructure.repository.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.alikhanzhomartov.gateway.api.dto.RouteDefinitionDTO;
import io.github.alikhanzhomartov.gateway.core.mapper.RouteDefinitionMapper;
import io.github.alikhanzhomartov.gateway.infrastructure.repository.CustomRouteDefinitionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class RedisRouteDefinitionRepository implements CustomRouteDefinitionRepository {

    private final ReactiveStringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final RouteDefinitionMapper routeDefinitionMapper;

    private static final String ROUTES_KEY = "gateway:routes";

    public RedisRouteDefinitionRepository(ReactiveStringRedisTemplate redisTemplate,
                                          ObjectMapper objectMapper,
                                          RouteDefinitionMapper routeDefinitionMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.routeDefinitionMapper = routeDefinitionMapper;
    }

    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        return redisTemplate.opsForHash().values(ROUTES_KEY)
                .flatMap(json -> {
                    try {
                        RouteDefinitionDTO dto = objectMapper.readValue(json.toString(), RouteDefinitionDTO.class);
                        return Mono.just(routeDefinitionMapper.convertToRouteDefinition(dto));
                    } catch (Exception e) {
                        log.error("Failed to parse route", e);
                        return Mono.empty();
                    }
                });
    }

    @Override
    public Flux<RouteDefinitionDTO> getRoutes() {
        return redisTemplate.opsForHash().values(ROUTES_KEY)
                .flatMap(json -> {
                    try {
                        RouteDefinitionDTO dto = objectMapper.readValue(json.toString(), RouteDefinitionDTO.class);
                        return Mono.just(dto);
                    } catch (Exception e) {
                        log.error("Failed to parse route", e);
                        return Mono.empty();
                    }
                });
    }

    @Override
    public Mono<Void> save(Mono<RouteDefinition> route) {
        return Mono.error(new UnsupportedOperationException("Use specific admin API"));
    }

    @Override
    public Mono<Void> delete(Mono<String> routeId) {
        return routeId
                .flatMap(id -> redisTemplate.opsForHash().remove(ROUTES_KEY, id))
                .then();
    }

    @Override
    public Mono<Void> saveDTO(RouteDefinitionDTO dto) {
        try {
            String json = objectMapper.writeValueAsString(dto);
            return redisTemplate.opsForHash().put(ROUTES_KEY, dto.getId(), json).then();
        } catch (Exception e) {
            return Mono.error(e);
        }
    }

}
