# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

flux-mall-server is a Java Spring Boot e-commerce backend service built with Gradle. The project uses Java 17 and Spring Boot 3.5.10.

## Build & Test Commands

```bash
# Build the project
./gradlew build

# Run the application
./gradlew bootRun

# Run tests
./gradlew test

# Run a specific test class
./gradlew test --tests FluxMallServerApplicationTests

# Clean build
./gradlew clean build
```

## Technology Stack

- **Framework**: Spring Boot 3.5.10 (Web, Security, Validation, Redis, OAuth2 Client)
- **ORM**: MyBatis (not JPA/Hibernate) - uses XML mappers and annotations
- **Database**: MySQL with mysql-connector-j
- **Authentication**: Spring Security + JWT (JJWT 0.12.3) + OAuth2 Client
- **Caching**: Redis (spring-boot-starter-data-redis)
- **File Storage**: AWS S3 SDK 2.21.0
- **API Documentation**: SpringDoc OpenAPI 3.0.1 (Swagger UI)
- **Utilities**: Lombok for boilerplate reduction
- **Monitoring**: Sentry (BOM configured, ready for integration)
- **Build**: Gradle with Java 17 toolchain

## Configuration Structure

The application uses Spring Boot profile-based configuration:

- `application.yaml` - Main configuration (currently has encoding issues)
- `application-dev.yaml` - Development environment configuration
- `application-prod.yaml` - Production environment configuration
- `src/main/resources/db/migration/` - Flyway database migrations (empty, ready for use)

## Package Structure

The project follows standard Spring Boot conventions under `com.fluxmallserver`:

```
com.fluxmallserver/
├── FluxMallServerApplication.java  # Main entry point (@SpringBootApplication)
└── (additional packages to be organized)
```

When adding new features, follow typical Spring Boot layering:
- `controller/` - REST endpoints
- `service/` - Business logic
- `repository/` or `mapper/` - MyBatis mappers for database access
- `model/` or `dto/` - Data transfer objects
- `entity/` - Domain entities
- `config/` - Spring configuration classes
- `security/` - Security/JWT configurations
- `exception/` - Custom exceptions and handlers
- `util/` - Utility classes

## Important Architecture Notes

1. **MyBatis ORM**: This project uses MyBatis (not JPA/Hibernate). Database operations use:
   - XML mapper files in `src/main/resources/mapper/` (typical location)
   - `@Mapper` annotated interfaces
   - SQL queries defined in XML or annotations

2. **JWT Authentication**: JJWT library version 0.12.3 is used with separate API, impl, and Jackson modules.

3. **AWS S3**: Uses AWS SDK v2 (2.21.0) - not v1. Be aware of the API differences.

4. **Redis**: Configured via spring-boot-starter-data-redis.

5. **Flyway**: Database migrations directory exists at `src/main/resources/db/migration/` but is currently empty.

## Running with Profiles

```bash
# Development profile
./gradlew bootRun --args='--spring.profiles.active=dev'

# Production profile
./gradlew bootRun --args='--spring.profiles.active=prod'
```

## API Documentation

SpringDoc OpenAPI provides Swagger UI. Once the application is running, access documentation at:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Common Patterns

When adding new features:
1. Create entity class with appropriate annotations
2. Create MyBatis mapper interface (`@Mapper`)
3. Create XML mapper file in `src/main/resources/mapper/` for SQL queries
4. Create service layer with `@Service`
5. Create controller with `@RestController` for REST endpoints
6. Use Lombok annotations (`@Data`, `@Builder`, etc.) for POJOs
7. Apply validation using `javax.validation` annotations
