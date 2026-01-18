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

## Code Conventions

### Package Structure (DDD)
```
com.fluxmall.{domain}/
├── controller/          # REST API endpoints
├── service/             # Business logic
├── repository/          # MyBatis mapper interfaces
├── domain/              # Entity classes
├── dto/
│   ├── request/         # Request DTOs (record)
│   └── response/        # Response DTOs (record)
└── exception/           # Domain-specific error codes
```

### Controller Convention

```java
@RestController
@RequestMapping("/api/{domain}")
@RequiredArgsConstructor
@Tag(name = "Domain", description = "도메인 API")
public class DomainController {

    private final DomainService domainService;

    // 단건 조회 → SingleResult<ResponseDto>
    @GetMapping("/{id}")
    @Operation(summary = "조회", description = "단건 조회")
    public SingleResult<DomainResponse> getOne(@PathVariable Long id) {
        return ResponseService.getSingleResult(domainService.findById(id));
    }

    // 목록 조회 → ListResult<ResponseDto>
    @GetMapping
    public ListResult<DomainResponse> getList() {
        return ResponseService.getListResult(domainService.findAll());
    }

    // 생성/수정 (데이터 반환 불필요) → CommonResult
    @PostMapping
    public CommonResult create(@Valid @RequestBody CreateRequest request) {
        domainService.create(request);
        return ResponseService.getSuccessResult();
    }

    // 생성/수정 (데이터 반환 필요) → SingleResult<ResponseDto>
    @PatchMapping("/{id}")
    public SingleResult<DomainResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRequest request
    ) {
        return ResponseService.getSingleResult(domainService.update(id, request));
    }

    // 인증된 사용자 ID 주입 → @CurrentMemberId
    @GetMapping("/me")
    public SingleResult<DomainResponse> getMine(@CurrentMemberId Long memberId) {
        return ResponseService.getSingleResult(domainService.findByMemberId(memberId));
    }
}
```

**Controller 규칙:**
- `ResponseService` 정적 메서드만 사용 (DI 금지)
- 반환 타입: `CommonResult`, `SingleResult<T>`, `ListResult<T>`
- 인증 필요 시 `@CurrentMemberId Long memberId` 사용
- `@Valid` + Request DTO로 입력 검증
- Swagger 어노테이션: `@Tag`, `@Operation`

### Service Convention

```java
@Service
@RequiredArgsConstructor
public class DomainService {

    private final DomainMapper domainMapper;

    // 조회 (읽기 전용 트랜잭션)
    @Transactional(readOnly = true)
    public DomainResponse findById(Long id) {
        Domain domain = domainMapper.findById(id);
        if (domain == null) {
            throw new BusinessException(DomainError.NOT_FOUND);
        }
        return DomainResponse.from(domain);
    }

    // 생성/수정/삭제 (쓰기 트랜잭션)
    @Transactional
    public DomainResponse create(CreateRequest request) {
        // 비즈니스 검증
        if (domainMapper.existsByName(request.name())) {
            throw new BusinessException(DomainError.DUPLICATE_NAME);
        }

        Domain domain = request.toEntity();
        domainMapper.insert(domain);
        return DomainResponse.from(domain);
    }

    // 권한 검증이 필요한 경우
    @Transactional
    public void delete(Long id, Long memberId) {
        Domain domain = domainMapper.findById(id);
        if (domain == null) {
            throw new BusinessException(DomainError.NOT_FOUND);
        }
        if (!domain.getMemberId().equals(memberId)) {
            throw new BusinessException(AuthError.FORBIDDEN);
        }
        domainMapper.delete(id);
    }
}
```

**Service 규칙:**
- 읽기: `@Transactional(readOnly = true)`
- 쓰기: `@Transactional`
- 예외: `throw new BusinessException(ErrorCode)`
- 반환: Response DTO 또는 Entity
- null 체크 후 `NOT_FOUND` 예외

### Repository (Mapper) Convention

```java
@Mapper
public interface DomainMapper {

    // 조회
    Domain findById(Long id);
    List<Domain> findAll();
    List<Domain> findByMemberId(Long memberId);

    // 존재 확인
    boolean existsById(Long id);
    boolean existsByName(String name);

    // 생성/수정/삭제
    void insert(Domain domain);
    void update(Domain domain);
    void delete(Long id);

    // 다중 파라미터 → @Param
    List<Domain> findByCondition(
        @Param("memberId") Long memberId,
        @Param("status") String status
    );
}
```

**Mapper XML 규칙:**
- namespace: `com.fluxmall.{domain}.repository.{Domain}Mapper`
- resultMap: `{domain}ResultMap`
- 파일 위치: `src/main/resources/mappers/{domain}/{Domain}Mapper.xml`

### DTO Convention

```java
// Request DTO (record + validation)
public record CreateRequest(
    @NotBlank(message = "이름은 필수입니다")
    @Size(max = 50, message = "이름은 50자 이하입니다")
    String name,

    @NotNull(message = "가격은 필수입니다")
    @Positive(message = "가격은 양수여야 합니다")
    Integer price
) {
    public Domain toEntity() {
        return Domain.builder()
            .name(name)
            .price(price)
            .build();
    }
}

// Response DTO (record + factory method)
public record DomainResponse(
    Long id,
    String name,
    Integer price,
    LocalDateTime createdAt
) {
    public static DomainResponse from(Domain domain) {
        return new DomainResponse(
            domain.getId(),
            domain.getName(),
            domain.getPrice(),
            domain.getCreatedAt()
        );
    }
}
```

### Exception Convention

```java
// 도메인별 에러 코드 정의
@Getter
@AllArgsConstructor
public enum DomainError implements ErrorCode {

    // 조회 관련 에러 (XX0XX)
    NOT_FOUND("XX001", "해당 항목을 찾을 수 없습니다."),

    // 검증 관련 에러 (XX01X)
    DUPLICATE_NAME("XX010", "이미 사용 중인 이름입니다."),
    INVALID_STATUS("XX011", "유효하지 않은 상태입니다."),

    // 권한 관련 에러 (XX02X)
    NOT_OWNER("XX020", "해당 항목에 대한 권한이 없습니다.");

    private final String code;
    private final String message;
}
```

**에러 코드 규칙:**
- 패턴: `[도메인 2자리][번호 3자리]`
- 도메인 코드: AU(Auth), MB(Member), PD(Product), OD(Order), CT(Cart), WL(Wishlist), RV(Review), AD(Address)

### Response Structure

```json
// 성공 (CommonResult)
{
  "code": "S000",
  "message": "성공하였습니다."
}

// 성공 + 단건 데이터 (SingleResult)
{
  "code": "S000",
  "message": "성공하였습니다.",
  "data": { ... }
}

// 성공 + 목록 데이터 (ListResult)
{
  "code": "S000",
  "message": "성공하였습니다.",
  "data": [ ... ]
}

// 실패 (BusinessException)
{
  "code": "AU001",
  "message": "유효하지 않은 토큰입니다."
}
```

## Development Guidelines

### Development Workflow Reference
**상세 개발 워크플로우는 `.claude/development-workflow.md` 파일을 참고하세요.**

이 문서에는 다음 내용이 포함되어 있습니다:
- **Phase 1**: 설계 및 문서화 (Pre-Coding) - SQL 전략 수립, 데이터 모델링
- **Phase 2**: 도메인 및 레포지토리 구현 - MyBatis XML, ResultMap 작성
- **Phase 3**: 서비스 계층 TDD - 테스트 전략, Red-Green-Refactor
- **Phase 4**: 컨트롤러 계층 구현 - 입력값 검증, DTO 매핑
- **Phase 5**: 통합 테스트 및 최적화 - Testcontainers, SQL 성능 최적화

### Adding New Features
1. Define entity in `{domain}/domain/` package
2. Create Request/Response DTOs in `{domain}/dto/request/`, `{domain}/dto/response/`
3. Create MyBatis mapper interface in `{domain}/repository/`
4. Write SQL queries in `src/main/resources/mappers/{domain}/{Domain}Mapper.xml`
5. Implement business logic in `{domain}/service/`
6. Create REST controller in `{domain}/controller/`
7. Add error codes in `{domain}/exception/{Domain}Error.java`

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
