package io.github.alikhanzhomartov.gateway.core.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RedisRouteRefreshListener {

    private final ApplicationEventPublisher publisher;

    public RedisRouteRefreshListener(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void onMessage(String message) {
        log.info("Received Refresh Event from Redis: {}", message);

        publisher.publishEvent(new RefreshRoutesEvent(this));
    }
}
