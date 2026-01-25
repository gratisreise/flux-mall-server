# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with this repository.

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

```bash
# Build the project
./gradlew build

# Run tests
./gradlew test

# Run a single test class
./gradlew test --tests com.fluxmall.member.service.MemberServiceTest

# Run a single test method
./gradlew test --tests com.fluxmall.member.service.MemberServiceTest.Register.회원가입_성공

# Run the application (dev profile)
./gradlew bootRun --args='--spring.profiles.active=dev'

# Clean build
./gradlew clean build
```

## Environment Setup

- **Dev profile**: Uses local MySQL (localhost:3306/shopdb) and Redis (localhost:6379)
- **Required environment variables**:
  - `JWT_ACCESS_SECRET`: Secret key for access token signing
  - `JWT_REFRESH_SECRET`: Secret key for refresh token signing

## Architecture

### Package Structure (DDD)
```
com.fluxmall/
├── global/                      # 공통 모듈
│   ├── config/                  # Security, Web 설정
│   ├── filter/                  # JWT 필터
│   ├── response/                # 공통 응답 (CommonResult, ResponseService)
│   ├── exception/               # 공통 예외 (BusinessException, GlobalExceptionHandler)
│   ├── annotation/              # 커스텀 어노테이션 (@CurrentMemberId)
│   ├── resolver/                # Argument Resolver
│   └── util/                    # 유틸리티 (JwtUtil, RedisUtil, S3Util)
├── member/                      # 회원 도메인
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── domain/
│   └── dto/request, dto/response
├── auth/                        # 인증 도메인
├── product/                     # 상품 도메인
├── order/                       # 주문 도메인
├── cart/                        # 장바구니 도메인
├── wishlist/                    # 위시리스트 도메인
├── review/                      # 리뷰 도메인
├── address/                     # 배송지 도메인
└── recentview/                  # 최근 본 상품 도메인
```

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
- **Mapper interfaces** in `com.fluxmall.{domain}.repository` package
- **Entity classes** in `com.fluxmall.{domain}.domain` package
- **DTOs** in `com.fluxmall.{domain}.dto` package
- MyBatis config enables camelCase ↔ snake_case auto-mapping

## Testing

### Test Organization
- **Unit Tests**: `src/test/java/com/fluxmall/{domain}/service/*ServiceTest.java` - Use `@ExtendWith(MockitoExtension.class)` with `@Mock` for Mappers
- **Integration Tests**: Extend `IntegrationTestBase` for full context tests with Testcontainers MySQL + MockMvc
- **Test Utilities**:
  - `com.fluxmall.support.JwtTestUtil` for JWT token generation (use `createAuthHeader(token)` for Bearer token)
  - `com.fluxmall.support.TestFixture` for test data generation

### Integration Test Base
`IntegrationTestBase` provides:
- Testcontainers MySQL 8.0 container with UTF-8 support
- MockMvc for HTTP request simulation
- @Transactional for automatic rollback
- Dynamic properties for datasource, Redis (disabled), JWT configuration

## Code Conventions

### Controller Convention
- Use `ResponseService` static methods only (not dependency injection)
- Return types: `CommonResult`, `SingleResult<T>`, `ListResult<T>`
- Use `@CurrentMemberId Long memberId` annotation for authenticated user ID injection
- Use `@Valid` + Request DTO for input validation
- Swagger annotations: `@Tag`, `@Operation`

### Service Convention
- Read operations: `@Transactional(readOnly = true)`
- Write operations: `@Transactional`
- Throw `BusinessException(ErrorCode)` for business logic errors
- Return Response DTO or Entity
- Always check for null and throw `NOT_FOUND` exception

### Repository (Mapper) Convention
- Mapper interfaces in `{domain}/repository/` package
- Use `@Param` annotation for multiple parameters
- Mapper XML files in `src/main/resources/mappers/{domain}/`
- XML namespace: `com.fluxmall.{domain}.repository.{Domain}Mapper`
- ResultMap naming: `{domain}ResultMap`

### DTO Convention
- Request DTOs: `{Entity}{Action}Request` (e.g., `ProductCreateRequest`)
- Response DTOs: `{Entity}Response` (e.g., `ProductResponse`)
- Use `record` type with validation annotations
- Request DTOs should have `toEntity()` method
- Response DTOs should have static `from()` factory method

### Exception Convention
- Domain-specific error enums in `{domain}/exception/` package
- Implement `ErrorCode` interface with `code` and `message`
- Error code pattern: `[DOMAIN 2자리][번호 3자리]`
- Domain codes: AU(Auth), MB(Member), PD(Product), OD(Order), CT(Cart), WL(Wishlist), RV(Review), AD(Address)