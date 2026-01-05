package io.github.alikhanzhomartov.gateway.infrastructure.repository;

import io.github.alikhanzhomartov.gateway.api.dto.RouteDefinitionDTO;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomRouteDefinitionRepository extends RouteDefinitionRepository {
    Flux<RouteDefinitionDTO> getRoutes();
    Mono<Void> saveDTO(RouteDefinitionDTO dto);
}
