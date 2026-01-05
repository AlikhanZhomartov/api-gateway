# Dynamic Spring Cloud Gateway 🚀

A reactive, high-performance API Gateway built on top of **Spring Cloud Gateway** and **Spring WebFlux**. This project demonstrates how to implement **dynamic routing** using Redis, eliminating the need for application restarts when adding or modifying routes.

## Key Features

* **Dynamic Routing:** Routes are stored in Redis and loaded dynamically via `RedisRouteDefinitionRepository`.
* **Zero-Downtime Updates:** Uses Redis Pub/Sub (`gateway-routes-topic`) to notify gateway nodes about route changes instantly.
* **Resilience:** Integrated **Resilience4j** for Circuit Breaker, Rate Limiter, and Bulkhead patterns applied per-route.
* **Security:** OAuth2 Resource Server configuration (compatible with Keycloak) with role-based access control for Admin APIs.
* **Observability:** Ready for Prometheus metrics and Zipkin distributed tracing.

## Tech Stack

* **Java 17+**
* **Spring Boot 3.x** & **Spring Cloud 2023.x**
* **Redis** (Lettuce client) - Route storage & Pub/Sub
* **Resilience4j** - Fault tolerance
* **Lombok**

## Architecture

The gateway extends the default routing mechanism by implementing a custom `RouteDefinitionRepository`.
1.  **Storage:** Routes are saved as JSON in Redis Hash (`gateway:routes`).
2.  **Synchronization:** When a route is created via the Admin API, a message is published to Redis.
3.  **Refresh:** All Gateway instances listen to the topic and trigger a context refresh to apply changes immediately.
