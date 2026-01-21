# 통합 테스트 구현 계획

## 1. 통합 테스트 전략

### 1.1 목표
- **Controller 레이어 검증**: REST API 엔드포인트의 정상 동작 확인
- **전체 레이어 통합**: Controller → Service → Repository → DB 전체 플로우 검증
- **인증/인가 통합**: JWT 기반 보안 설정의 실제 동작 확인
- **트랜잭션 일관성**: 실제 DB 트랜잭션 처리의 정확성 검증

### 1.2 테스트 범위
- ✅ **포함**: Controller, Service, Repository, DB, Security
- ❌ **제외**: 외부 API 연동 (S3, Redis - 필요시 Mock)

### 1.3 테스트 환경
- **@SpringBootTest**: 전체 Spring Context 로드
- **@AutoConfigureMockMvc**: MockMvc를 통한 HTTP 요청 시뮬레이션
- **Testcontainers**: 실제 MySQL 컨테이너 사용
- **@Transactional**: 테스트 후 자동 롤백 (데이터 격리)

## 2. 테스트 환경 설정

### 2.1 Testcontainers 설정

```java
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public abstract class IntegrationTestBase {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;
}
```

### 2.2 JWT 테스트 유틸리티

```java
public class JwtTestUtil {

    public static String generateAccessToken(JwtUtil jwtUtil, Long memberId, String role) {
        return jwtUtil.createAccessToken(memberId, role);
    }

    public static String generateRefreshToken(JwtUtil jwtUtil, Long memberId) {
        return jwtUtil.createRefreshToken(memberId);
    }

    public static String createAuthHeader(String token) {
        return "Bearer " + token;
    }
}
```

### 2.3 테스트 데이터 빌더

```java
public class IntegrationTestFixture {

    // 회원 등록 요청
    public static RegisterRequest createRegisterRequest(String username, String password, String nickname) {
        return new RegisterRequest(username, password, nickname);
    }

    // 로그인 요청
    public static LoginRequest createLoginRequest(String username, String password) {
        return new LoginRequest(username, password);
    }

    // 상품 등록 요청
    public static ProductCreateRequest createProductRequest() {
        return new ProductCreateRequest(
            "테스트 상품",
            "상품 설명",
            "ELECTRONICS",
            10000,
            100
        );
    }
}
```

## 3. Phase별 통합 테스트 목록

### Phase 1: 인증/회원 통합 테스트 (12개)

#### AuthControllerIntegrationTest (6개)
- ✅ **POST /api/auth/login**: 로그인 성공
- ✅ **POST /api/auth/login**: 잘못된 자격증명
- ✅ **POST /api/auth/logout**: 로그아웃 성공
- ✅ **POST /api/auth/logout**: 유효하지 않은 토큰
- ✅ **POST /api/auth/refresh**: 토큰 재발급 성공
- ✅ **POST /api/auth/refresh**: 만료된 Refresh Token

#### MemberControllerIntegrationTest (6개)
- ✅ **POST /api/members/register**: 회원가입 성공
- ✅ **POST /api/members/register**: 중복 이메일
- ✅ **POST /api/members/register**: 중복 닉네임
- ✅ **GET /api/members/me**: 내 정보 조회 성공
- ✅ **PATCH /api/members/profile**: 프로필 수정 성공
- ✅ **PATCH /api/members/password**: 비밀번호 변경 성공

### Phase 2: 상품/리뷰 통합 테스트 (18개)

#### ProductControllerIntegrationTest (10개)
- ✅ **GET /api/products/{id}**: 상품 상세 조회 성공
- ✅ **GET /api/products/{id}**: 상품 없음
- ✅ **GET /api/products**: 상품 목록 조회 (페이징)
- ✅ **POST /api/products**: 상품 등록 성공 (판매자)
- ✅ **POST /api/products**: 인증 없이 등록 시도 (401)
- ✅ **PATCH /api/products/{id}**: 상품 수정 성공 (소유자)
- ✅ **PATCH /api/products/{id}**: 권한 없는 사용자 수정 시도 (403)
- ✅ **DELETE /api/products/{id}**: 상품 삭제 성공 (소유자)
- ✅ **DELETE /api/products/{id}**: 권한 없는 사용자 삭제 시도 (403)
- ✅ **PATCH /api/products/{id}/status**: 상품 상태 변경 성공

#### ReviewControllerIntegrationTest (8개)
- ✅ **GET /api/products/{productId}/reviews**: 리뷰 목록 조회
- ✅ **POST /api/products/{productId}/reviews**: 리뷰 작성 성공
- ✅ **POST /api/products/{productId}/reviews**: 인증 없이 작성 시도 (401)
- ✅ **POST /api/products/{productId}/reviews**: 구매하지 않은 상품 리뷰 시도 (400)
- ✅ **PATCH /api/reviews/{id}**: 리뷰 수정 성공 (작성자)
- ✅ **PATCH /api/reviews/{id}**: 권한 없는 사용자 수정 시도 (403)
- ✅ **DELETE /api/reviews/{id}**: 리뷰 삭제 성공 (작성자)
- ✅ **DELETE /api/reviews/{id}**: 권한 없는 사용자 삭제 시도 (403)

### Phase 3: 주문/장바구니 통합 테스트 (16개)

#### OrderControllerIntegrationTest (8개)
- ✅ **POST /api/orders**: 주문 생성 성공
- ✅ **POST /api/orders**: 재고 부족 주문 시도 (400)
- ✅ **POST /api/orders**: 인증 없이 주문 시도 (401)
- ✅ **GET /api/orders/{id}**: 주문 상세 조회 (소유자)
- ✅ **GET /api/orders/{id}**: 권한 없는 사용자 조회 시도 (403)
- ✅ **GET /api/orders**: 내 주문 목록 조회
- ✅ **POST /api/orders/{id}/pay**: 주문 결제 성공
- ✅ **POST /api/orders/{id}/cancel**: 주문 취소 성공

#### CartControllerIntegrationTest (8개)
- ✅ **GET /api/cart**: 장바구니 조회 성공
- ✅ **POST /api/cart/items**: 장바구니 상품 추가 성공
- ✅ **POST /api/cart/items**: 재고 초과 추가 시도 (400)
- ✅ **POST /api/cart/items**: 인증 없이 추가 시도 (401)
- ✅ **PATCH /api/cart/items/{id}**: 장바구니 수량 변경 성공
- ✅ **PATCH /api/cart/items/{id}**: 권한 없는 사용자 수정 시도 (403)
- ✅ **DELETE /api/cart/items/{id}**: 장바구니 상품 삭제 성공
- ✅ **DELETE /api/cart/items**: 장바구니 다건 삭제 성공

### Phase 4: 배송지/위시리스트 통합 테스트 (14개)

#### ShippingAddressControllerIntegrationTest (8개)
- ✅ **GET /api/shipping-addresses**: 배송지 목록 조회
- ✅ **POST /api/shipping-addresses**: 배송지 추가 성공
- ✅ **POST /api/shipping-addresses**: 최대 개수 초과 시도 (400)
- ✅ **POST /api/shipping-addresses**: 인증 없이 추가 시도 (401)
- ✅ **PATCH /api/shipping-addresses/{id}**: 배송지 수정 성공
- ✅ **DELETE /api/shipping-addresses/{id}**: 배송지 삭제 성공
- ✅ **PATCH /api/shipping-addresses/{id}/default**: 기본 배송지 설정
- ✅ **PATCH /api/shipping-addresses/{id}**: 권한 없는 사용자 수정 시도 (403)

#### WishlistControllerIntegrationTest (6개)
- ✅ **GET /api/wishlist**: 위시리스트 조회
- ✅ **POST /api/wishlist/toggle**: 위시리스트 토글 (추가)
- ✅ **POST /api/wishlist/toggle**: 위시리스트 토글 (삭제)
- ✅ **POST /api/wishlist/toggle**: 인증 없이 토글 시도 (401)
- ✅ **DELETE /api/wishlist/{productId}**: 위시리스트 삭제 성공
- ✅ **GET /api/wishlist/check/{productId}**: 위시리스트 여부 확인

### Phase 5: E2E 시나리오 테스트 (6개)

#### E2EScenarioTest
- ✅ **시나리오 1**: 회원가입 → 로그인 → 상품 조회 → 장바구니 추가 → 주문 → 결제
- ✅ **시나리오 2**: 로그인 → 상품 검색 → 위시리스트 추가 → 장바구니 이동 → 주문
- ✅ **시나리오 3**: 로그인 → 상품 구매 → 배송 완료 → 리뷰 작성
- ✅ **시나리오 4**: 판매자 로그인 → 상품 등록 → 상품 수정 → 재고 관리
- ✅ **시나리오 5**: 로그인 → 배송지 등록 → 기본 배송지 설정 → 주문
- ✅ **시나리오 6**: 로그인 → 장바구니 추가 → 수량 변경 → 일부 삭제 → 주문

## 4. 테스트 작성 패턴

### 4.1 기본 패턴

```java
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProductControllerIntegrationTest extends IntegrationTestBase {

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    @DisplayName("상품 등록 성공")
    void 상품_등록_성공() throws Exception {
        // given
        String accessToken = generateAccessToken(2L, "SELLER");
        ProductCreateRequest request = createProductRequest();

        // when & then
        mockMvc.perform(post("/api/products")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("S000"))
                .andExpect(jsonPath("$.data.name").value("테스트 상품"));
    }
}
```

### 4.2 인증 테스트 패턴

```java
@Test
@DisplayName("인증 없이 요청 시 401")
void 인증_없이_요청() throws Exception {
    mockMvc.perform(post("/api/products")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
}

@Test
@DisplayName("권한 없는 사용자 요청 시 403")
void 권한_없음() throws Exception {
    String accessToken = generateAccessToken(1L, "CUSTOMER");

    mockMvc.perform(patch("/api/products/{id}", productId)
            .header("Authorization", "Bearer " + accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden());
}
```

### 4.3 페이징 테스트 패턴

```java
@Test
@DisplayName("페이징 조회 성공")
void 페이징_조회() throws Exception {
    mockMvc.perform(get("/api/products")
            .param("page", "0")
            .param("size", "10")
            .param("sort", "createdAt,desc"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(lessThanOrEqualTo(10)));
}
```

## 5. 구현 순서

### 5.1 Phase 1 (인증/회원)
1. IntegrationTestBase 작성
2. JwtTestUtil 작성
3. AuthControllerIntegrationTest
4. MemberControllerIntegrationTest

### 5.2 Phase 2 (상품/리뷰)
1. ProductControllerIntegrationTest
2. ReviewControllerIntegrationTest

### 5.3 Phase 3 (주문/장바구니)
1. CartControllerIntegrationTest
2. OrderControllerIntegrationTest

### 5.4 Phase 4 (배송지/위시리스트)
1. ShippingAddressControllerIntegrationTest
2. WishlistControllerIntegrationTest

### 5.5 Phase 5 (E2E)
1. E2EScenarioTest

## 6. 의존성 추가 (필요시)

```gradle
dependencies {
    // Testcontainers
    testImplementation 'org.testcontainers:testcontainers:1.19.0'
    testImplementation 'org.testcontainers:mysql:1.19.0'
    testImplementation 'org.testcontainers:junit-jupiter:1.19.0'
}
```

## 7. 성공 기준

- ✅ 모든 통합 테스트 통과
- ✅ API 엔드포인트별 정상/비정상 케이스 커버
- ✅ 인증/인가 시나리오 검증 완료
- ✅ E2E 시나리오 6개 검증 완료
- ✅ 총 66개 통합 테스트 작성 완료

## 8. 주의사항

### 8.1 테스트 데이터 격리
- 각 테스트는 독립적으로 실행되어야 함
- `@Transactional`로 자동 롤백 보장
- 테스트 간 데이터 의존성 제거

### 8.2 성능 고려
- Testcontainers는 최초 1회만 시작 (클래스 레벨 static)
- 불필요한 전체 Context 로드 지양
- 필요시 `@WebMvcTest` + Mock 조합 활용

### 8.3 실제 환경 차이
- Redis 캐시는 테스트에서 비활성화 또는 Embedded 사용
- S3 업로드는 Mock 처리
- 외부 API 호출은 WireMock 활용

## 9. 테스트 실행

```bash
# 전체 통합 테스트 실행
./gradlew integrationTest

# 특정 통합 테스트 실행
./gradlew test --tests "*IntegrationTest"

# 통합 테스트 + 커버리지
./gradlew test jacocoTestReport
```

## 10. 다음 단계

1. Phase 1 통합 테스트 구현
2. Phase 2-4 순차 구현
3. E2E 시나리오 테스트 구현
4. 테스트 커버리지 확인 및 보완
5. CI/CD 파이프라인 통합
