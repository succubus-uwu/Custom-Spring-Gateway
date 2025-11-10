## Gateway API

### Description

**Gateway API** — a Java 21 and Spring Boot 3 (WebFlux) based service for routing requests between microservices, dynamically registering them, and aggregating their OpenAPI documentation.

Key features:

* Dynamic service registration and removal
* Automatic TTL-based service expiration
* Request proxying through registered routes
* Aggregation of multiple OpenAPI specs into a single JSON
* Unified Swagger UI
* Optional access restrictions for documentation

### Service Management

* `POST /internal/service` — register a new service
* `PUT /internal/service/{name}` — update service parameters
* `DELETE /internal/service/{name}` — remove a service
* `GET /internal/service` — list registered services

### Documentation

* `GET /api/v1/docs` — aggregated OpenAPI JSON
* `GET /api/v1/docs-ui` — Swagger UI