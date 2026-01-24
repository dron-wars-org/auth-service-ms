# auth-service – Dron Wars Org

The **authentication and identity microservice** for **Dron Wars**, a browser shoot 'em up heavily inspired by the NES classic *Abadox* (1989).

This is the first microservice in an event-driven architecture using Spring Boot 3.x and Java 21+.  
It handles user registration, login, JWT issuance, refresh tokens, basic OAuth2 (Google), and user profile management.

## Why this service first?
- Authentication is foundational for any secure app (especially with leaderboards, persistent progress, and in-game economy).
- Allows learning core modern Java/Spring concepts early: virtual threads, Kafka producers, Redis sessions, JWT best practices.

## Features & Learning Objectives

- **Java 21+ features**: Records for DTOs, pattern matching in switch, virtual threads for high-concurrency endpoints
- **Spring Boot 3.x**: Security 6+, JWT with refresh tokens, Redis-backed sessions (Spring Session)
- **Event-driven**: Kafka producer for `UserLoggedIn` and `UserRegistered` events (decouples from other services)
- **Security**: JWT (access + refresh), OAuth2 login (Google), password hashing (BCrypt), rate limiting basics
- **Persistence**: PostgreSQL via Spring Data JPA + Flyway migrations
- **Observability**: Actuator endpoints + Micrometer metrics (Prometheus ready)
- **Testing**: JUnit 5, Mockito, Testcontainers (Postgres + Kafka + Redis)

## Tech Stack

- Java 21
| POST   | `/api/auth/register`      | Register new user                    | No            |
| POST   | `/api/auth/login`         | Login + return JWT + refresh token   | No            |
| POST   | `/api/auth/refresh`       | Refresh access token                 | Refresh token |
| GET    | `/api/auth/profile`       | Get current user profile             | JWT           |
| POST   | `/api/auth/google`        | OAuth2 callback (Google login)       | No            |

## Quick Start (Local Development)

### Prerequisites
- Java 21+
- Gradle
- Docker & Docker Compose (for Postgres, Redis, Kafka)

### 1. Start infrastructure
From the root of the monorepo (or Organization):
```bash
docker-compose up -d postgres redis kafka zookeeper
```

### 2. Run the service
```bash
cd auth-service
./gradlew clean build
./gradlew bootRun
```

Or with virtual threads enabled (Java 21+):
```bash
java --enable-preview -XX:+UseZGC -jar build/libs/auth-service-0.0.1-SNAPSHOT.jar
```

→ Service runs on `http://localhost:8081` (configurable via `application.yml`)

### 3. Environment variables / application.yml example
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/dron_wars
    username: postgres
    password: postgres
  redis:
    host: localhost
    port: 6379
  kafka:
    bootstrap-servers: localhost:9092
jwt:
  secret: your-very-long-secret-key-here
  expiration-ms: 900000   # 15 min access token
  refresh-expiration-ms: 604800000  # 7 days
```

## Project Structure

```
auth-service/
├── src/
│   ├── main/
│   │   ├── java/com/dronwars/auth/
│   │   │   ├── config/          # SecurityConfig, KafkaConfig, RedisConfig
│   │   │   ├── controller/      # AuthController
│   │   │   ├── dto/             # RegisterRequest, LoginRequest, JwtResponse (records!)
│   │   │   ├── entity/          # User entity
│   │   │   ├── repository/      # UserRepository
│   │   │   ├── service/         # AuthService, JwtService
│   │   │   └── event/           # UserEvent (for Kafka)
│   │   └── resources/
│   │       ├── application.yml
│   │       └── db/migration/    # Flyway scripts
├── build.gradle
└── Dockerfile
```

## Testing

```bash
./gradlew test
```

- Unit tests: Services, JWT utils
- Integration: Testcontainers for Postgres, Redis, Kafka

## Learning Notes

This service is designed to practice:
- Virtual threads for scalable endpoints
- Kafka producers with JsonSerializer
- Modern JWT handling (no sessions in DB, Redis only)
- Spring Security method-level security
- Clean architecture (controllers → services → repositories)

See full project in **dron-wars-org** Organization: https://github.com/dron-wars-org

---

MIT License – part of my personal learning/portfolio  
Pablo – Don Torcuato, Buenos Aires – 2026
