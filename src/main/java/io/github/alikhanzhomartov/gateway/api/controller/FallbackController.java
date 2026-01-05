package io.github.alikhanzhomartov.gateway.api.controller;

import io.github.alikhanzhomartov.gateway.api.dto.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping
    public Mono<ResponseEntity<ApiErrorResponse>> fallback() {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiErrorResponse.builder()
                        .status(503)
                        .error("Service Unavailable")
                        .message("Service is taking too long to respond or is down.")
                        .timestamp(LocalDateTime.now())
                        .build()));
    }
}
