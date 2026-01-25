# auth-service-ms – Dron Wars Org

The **authentication and identity microservice** for **Dron Wars**, a browser shoot 'em up heavily inspired by the NES classic *Abadox* (1989).

This is the first microservice in an event-driven architecture using **Spring Boot 4.x** and **Java 21+**.  
It handles user registration, login, JWT issuance, refresh tokens, and user profile management.

## Why this service first?
- Authentication is foundational for any secure app.
- Implementation of modern Java/Spring concepts: virtual threads, Kafka producers, Redis sessions, and SOLID architecture.

## Features & Tech Stack

- **Java 21**: Virtual threads enabled for high concurrency.
- **Spring Boot 4.0.x**: Using the latest state-of-the-art features.
- **Security**: JWT (access + refresh) with Spring Security 6+.
- **Event-driven**: Kafka producer for `UserLoggedIn` and `UserRegistered` events.
- **Persistence**: PostgreSQL via Spring Data JPA.
- **Caching/Sessions**: Redis for token management.
- **Mapeo**: MapStruct 1.6.3 for high-performance DTO/Entity conversion.

## Quick Start (Local Development)

### Prerequisites
- Java 21+
- Gradle 8.x
- **Minikube** (for local infrastructure)

### 1. Start Infrastructure (Minikube)
Ensure Minikube is running and apply the manifests:
```bash
minikube start
kubectl apply -f data/k8s/
```
> [!NOTE]
> Check `data/instrucciones-minikube.md` for detailed service status and NodePorts.

### 2. Run the service
The service is configured to run on port **8080**.
```bash
./gradlew bootRun
```

Or with specific profile:
```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

→ Service runs on `http://localhost:8080`

### 3. Local Environment Configuration
Configuration is managed via `src/main/resources/application-local.properties`.

Default Minikube NodePorts (assuming Minikube IP is `192.168.49.2`):
- **Postgres**: `192.168.49.2:30543`
- **Redis**: `192.168.49.2:30379`
- **Kafka**: `192.168.49.2:30092`

## Project Structure

```
auth-service-ms/
├── src/main/java/com/pablovass/authservice/
│   ├── config/          # Spring Configuration (Security, Kafka, Redis)
│   ├── controller/      # REST API Endpoints
│   ├── domain/model/    # Domain entities (User)
│   ├── repository/      # Spring Data Repositories
│   ├── service/         # Business Logic & JWT handling
│   └── AuthServiceMsApplication.java
└── src/main/resources/
    ├── application.properties
    └── application-local.properties
```

## Testing

```bash
./gradlew test
```

## Learning Notes

This service practices:
- **Clean Architecture**: Separation of concerns between layers.
- **SOLID Principles**: Focused on maintainability and testability.
- **Virtual Threads**: Mandatory for handling high volumes of concurrent auth requests.
- **MapStruct**: Best practice for immutable Records (DTOs).

See the full project documentation in the `RULES/` directory of the organization.

---

MIT License – part of my personal learning/portfolio  
Pablo – Don Torcuato, Buenos Aires – 2026
