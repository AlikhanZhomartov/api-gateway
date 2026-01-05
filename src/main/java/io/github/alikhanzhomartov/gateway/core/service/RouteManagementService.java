package io.github.alikhanzhomartov.gateway.core.service;

import io.github.alikhanzhomartov.gateway.api.dto.RouteDefinitionDTO;
import io.github.alikhanzhomartov.gateway.infrastructure.config.RedisPubSubConfig;
import io.github.alikhanzhomartov.gateway.infrastructure.repository.CustomRouteDefinitionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class RouteManagementService {

    private final CustomRouteDefinitionRepository routeRepository;
    private final ReactiveStringRedisTemplate reactiveRedisTemplate;

    public RouteManagementService(CustomRouteDefinitionRepository routeRepository,
                                  ReactiveStringRedisTemplate reactiveRedisTemplate) {
        this.routeRepository = routeRepository;
        this.reactiveRedisTemplate = reactiveRedisTemplate;
    }

    public Flux<RouteDefinitionDTO> getRoutes() {
        return routeRepository.getRoutes();
    }

    public Mono<Void> create(RouteDefinitionDTO dto) {
        return routeRepository.saveDTO(dto)
                .then(reactiveRedisTemplate.convertAndSend(RedisPubSubConfig.ROUTES_TOPIC, "refresh"))
                .then();
    }

    public Mono<Void> delete(String id) {
        return routeRepository.delete(Mono.just(id))
                .then(reactiveRedisTemplate.convertAndSend(RedisPubSubConfig.ROUTES_TOPIC, "refresh"))
                .then();
    }
}
