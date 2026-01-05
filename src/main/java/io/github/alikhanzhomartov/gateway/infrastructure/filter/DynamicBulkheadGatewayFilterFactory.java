package io.github.alikhanzhomartov.gateway.infrastructure.filter;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.reactor.bulkhead.operator.BulkheadOperator;
import lombok.Data;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class DynamicBulkheadGatewayFilterFactory extends AbstractGatewayFilterFactory<DynamicBulkheadGatewayFilterFactory.Config> {

    private final BulkheadRegistry bulkheadRegistry;

    public DynamicBulkheadGatewayFilterFactory(BulkheadRegistry bulkheadRegistry) {
        super(Config.class);
        this.bulkheadRegistry = bulkheadRegistry;
    }

    @Override
    public GatewayFilter apply(Config config) {
        BulkheadConfig bulkheadConfig = BulkheadConfig.custom()
                .maxConcurrentCalls(config.getMaxConcurrentCalls())
                .maxWaitDuration(Duration.ofMillis(0))
                .build();

        Bulkhead bulkhead = bulkheadRegistry.bulkhead(config.getName(), bulkheadConfig);
        bulkhead.changeConfig(bulkheadConfig);

        return (exchange, chain) -> chain.filter(exchange)
                .transformDeferred(BulkheadOperator.of(bulkhead))
                .onErrorResume(BulkheadFullException.class, e -> {
                    exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
                    return exchange.getResponse().setComplete();
                });
    }

    @Data
    public static class Config {
        private String name;
        private int maxConcurrentCalls;
    }
}
