# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Flux Mall Server is a Spring Boot-based e-commerce backend application using:
- **Framework**: Spring Boot 3.5.9 with Java 17
- **Database**: MySQL with MyBatis for ORM
- **Security**: JWT-based stateless authentication with Spring Security
- **Caching**: Caffeine (in-memory) and Redis
- **Migration**: Flyway for database version control
- **Storage**: AWS S3 for product/review images
- **API Documentation**: Swagger/OpenAPI (springdoc-openapi)
- **Monitoring**: Spring Boot Actuator with Prometheus metrics
- **Error Tracking**: Sentry integration

## Build and Run Commands

### Development
```bash
# Build the project
./gradlew build

# Run tests
./gradlew test

# Run tests with coverage report
./gradlew test jacocoTestReport

# Run the application (dev profile)
./gradlew bootRun --args='--spring.profiles.active=dev'

# Clean build
./gradlew clean build
```

### Environment Setup
- **Dev profile**: Uses local MySQL (localhost:3306/shopdb) and Redis (localhost:6379)
- **Required environment variables**:
  - `JWT_ACCESS_SECRET`: Secret key for access token signing
  - `JWT_REFRESH_SECRET`: Secret key for refresh token signing

## Architecture

### Authentication Flow
The application uses **dual-token JWT authentication**:
1. **JwtAuthenticationFilter** intercepts requests and validates JWT tokens
2. **ExceptionHandlerFilter** wraps the JWT filter to handle authentication exceptions
3. **TokenBlacklistService** manages logout tokens using Redis TTL
4. Tokens are validated using separate keys (access vs refresh) via **JwtUtil**
5. Security configuration in **SecurityConfig** defines filter chain and password encoding

### Response Structure
All API responses follow a standardized format via **ResponseService**:
- **CommonResult**: Base response with `code` and `message`
- **SingleResult<T>**: For single object responses (adds `data` field)
- **ListResult<T>**: For list responses (adds `data` array)

Error handling is centralized in **GlobalExceptionHandler** which catches:
- **BusinessException**: Custom business logic errors with enum-based error codes
- Standard Spring validation errors
- Authentication/authorization failures

### Error Code System
Errors are defined as enums implementing the **ErrorCode** interface:
- **AuthError**: Authentication-related errors (e.g., `AU001` for invalid token)
- Each error has a code (string) and message (string)
- Error codes follow pattern: `[DOMAIN][NUMBER]` (e.g., `AU001`)

### Data Access Layer
- **MyBatis mappers** in `src/main/resources/mappers/` for SQL queries
- **Mapper interfaces** in `com.fluxmall.mapper` package
- **Entity classes** in `com.fluxmall.domain.entity`
- **DTOs** in `com.fluxmall.domain.dto`
- MyBatis config enables camelCase ↔ snake_case auto-mapping

### Key Packages
- `config`: Security and web configuration
- `controller`: REST API endpoints
- `service`: Business logic layer
- `mapper`: MyBatis interfaces for database access
- `domain.entity`: Database entity classes
- `domain.dto`: Data transfer objects
- `filter`: Custom servlet filters (JWT auth, exception handling)
- `utils`: Utility classes (JwtUtil, S3Util, RedisUtil)
- `exception`: Global exception handling and error definitions
- `common`: Shared constants and response models
- `annotations`: Custom validation annotations (e.g., @Password)

### Service Layer Pattern
Services are organized by domain:
- **MemberService**: User account management
- **ProductService**: Product catalog and inventory
- **OrderService**: Order processing and management
- **CartService**: Shopping cart operations
- **WishlistService**: Wishlist functionality
- **ReviewService**: Product reviews
- **ShippingAddressService**: Delivery address management
- **RecentViewService**: Recently viewed products tracking
- **TokenBlacklistService**: JWT token invalidation (in `service.auth`)

## Development Guidelines

### Adding New Features
1. Define entity in `domain.entity` package
2. Create DTO classes in `domain.dto` if needed
3. Create MyBatis mapper interface in `mapper` package
4. Write SQL queries in `src/main/resources/mappers/[EntityName]Mapper.xml`
5. Implement business logic in service layer
6. Create REST controller in `controller` package
7. Add error codes to appropriate enum in `exception.errors`

### Security Considerations
- JWT secrets MUST be externalized (environment variables)
- Access tokens expire in 30 minutes (configurable via `jwt.access-expiration`)
- Refresh tokens expire in 7 days (configurable via `jwt.refresh-expiration`)
- Tokens contain `subject` (username) and `memberId` claims
- SecurityConfig defines public endpoints (auth-related paths are permitAll)

### Testing
- Test classes in `src/test/java/com/fluxmall/`
- Integration tests should use `@SpringBootTest`
- Security tests should use `@AutoConfigureMockMvc` and Spring Security Test
- MyBatis mapper tests should use `@MybatisTest`

### Database Migrations
- Flyway migrations in `src/main/resources/db/migration/`
- Follow naming convention: `V[version]__[description].sql`
- Baseline-on-migrate is enabled for existing databases
- Never modify committed migrations; create new ones

### API Documentation
- Swagger UI available at `/swagger-ui.html` when running
- OpenAPI spec at `/v3/api-docs`
- Use standard Spring annotations for documentation

## Common Constants
- **CommonValue.AUTH_PREFIX**: "Bearer " (used for Authorization header parsing)

## Deployment (Currently Disabled)
The CI/CD pipeline in `.github/workflows/deploy.yaml` is commented out but shows intended deployment strategy:
- Build Docker image from GCP Artifact Registry
- Run tests before deployment
- Deploy to GCP VM via SSH
- Uses GitHub secrets for credentials and configuration
