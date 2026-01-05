package io.github.alikhanzhomartov.gateway.api.controller;

import io.github.alikhanzhomartov.gateway.api.dto.RouteDefinitionDTO;
import io.github.alikhanzhomartov.gateway.core.service.RouteManagementService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/admin/routes")
public class RouteAdminController {

    private final RouteManagementService routeManagementService;

    public RouteAdminController(RouteManagementService routeManagementService) {
        this.routeManagementService = routeManagementService;
    }

    @GetMapping
    public Flux<RouteDefinitionDTO> getAll() {
        return routeManagementService.getRoutes();
    }

    @PostMapping
    public Mono<Void> create(@RequestBody @Valid RouteDefinitionDTO dto) {
        return routeManagementService.create(dto);
    }

    @DeleteMapping("/{id}")
    public Mono<Void> delete(@PathVariable(name = "id") String id) {
        return routeManagementService.delete(id);
    }

}
