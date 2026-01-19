# FluxMall Server - Test Convention

## Overview

테스트 코드 작성 시 준수해야 할 컨벤션과 패턴을 정의합니다.

---

## 테스트 대상 판단 기준

### 판단 플로우차트

```
                    ┌─────────────────────────┐
                    │   이 로직을 테스트할까?   │
                    └───────────┬─────────────┘
                                │
                    ┌───────────▼─────────────┐
                    │  비즈니스 로직이 있는가?  │
                    └───────────┬─────────────┘
                           YES  │  NO → ❌ 테스트 불필요
                                │
                    ┌───────────▼─────────────┐
                    │   조건 분기가 있는가?    │
                    │  (if/else, switch 등)   │
                    └───────────┬─────────────┘
                           YES  │  NO
                                │    │
                    ┌───────────▼────┼────────┐
                    │  ✅ 테스트 필수  │        │
                    └────────────────┼────────┘
                                     │
                    ┌────────────────▼────────┐
                    │  외부 의존성 호출이       │
                    │  실패할 수 있는가?       │
                    └───────────┬─────────────┘
                           YES  │  NO
                                │    │
                    ┌───────────▼────┼────────┐
                    │  ✅ 테스트 필수  │        │
                    └────────────────┼────────┘
                                     │
                    ┌────────────────▼────────┐
                    │  데이터 변환/계산이      │
                    │  복잡한가?              │
                    └───────────┬─────────────┘
                           YES  │  NO → ❌ 테스트 불필요
                                │
                    ┌───────────▼─────────────┐
                    │      ✅ 테스트 필수       │
                    └─────────────────────────┘
```

### ✅ 테스트 필수 (MUST TEST)

| 분류 | 예시 | 이유 |
|------|------|------|
| **조건 분기 로직** | 권한 검증, 상태 체크, null 체크 | 분기마다 다른 결과 발생 |
| **비즈니스 규칙** | 재고 차감, 주문 상태 변경, 금액 계산 | 핵심 도메인 로직 |
| **예외 처리** | NOT_FOUND, FORBIDDEN, 검증 실패 | 에러 케이스 보장 |
| **복잡한 계산** | 할인 적용, 배송비 계산, 평균 평점 | 계산 오류 방지 |
| **상태 변경** | Soft Delete, 상태 전이 | 사이드 이펙트 검증 |
| **동시성 제어** | 재고 차감 (FOR UPDATE) | 레이스 컨디션 방지 |

```java
// ✅ 테스트 필수 - 조건 분기 + 비즈니스 규칙
public void deleteProduct(Long memberId, Long productId) {
    Product product = productMapper.findById(productId);
    if (product == null) {                              // 분기 1
        throw new BusinessException(ProductError.NOT_FOUND);
    }
    if (!product.getMemberId().equals(memberId)) {      // 분기 2
        throw new BusinessException(ProductError.NOT_OWNER);
    }
    if (product.getProductStatus() == DISCONTINUED) {   // 분기 3
        throw new BusinessException(ProductError.ALREADY_DISCONTINUED);
    }
    productMapper.softDelete(productId);                // 상태 변경
}
```

### ⚠️ 선택적 테스트 (SHOULD TEST)

| 분류 | 예시 | 판단 기준 |
|------|------|----------|
| **단순 CRUD** | findById → Response 변환 | 변환 로직 복잡도에 따라 |
| **리스트 조회** | findAll with 페이징 | 필터/정렬 로직 유무 |
| **단순 위임** | Controller → Service 호출 | 입력 검증 유무에 따라 |

```java
// ⚠️ 선택적 - 단순 조회 + 변환
@Transactional(readOnly = true)
public ProductResponse getProductDetail(Long productId) {
    Product product = productMapper.findById(productId);
    if (product == null) {  // ← 이 분기가 있으므로 테스트 권장
        throw new BusinessException(ProductError.NOT_FOUND);
    }
    return ProductResponse.from(product);  // 단순 변환
}
```

### ❌ 테스트 불필요 (SKIP)

| 분류 | 예시 | 이유 |
|------|------|------|
| **단순 위임** | Mapper 호출만 하는 메서드 | 로직 없음 |
| **Getter/Setter** | Entity의 getter/setter | Lombok 생성 코드 |
| **DTO 변환** | `Response.from(entity)` | 단순 매핑 |
| **프레임워크 코드** | `@Valid` 검증 | Spring이 처리 |
| **설정 클래스** | `@Configuration`, `@Bean` | 부트 시 검증됨 |

```java
// ❌ 테스트 불필요 - 단순 위임
public List<Product> findAll() {
    return productMapper.findAll();  // Mapper 호출만
}

// ❌ 테스트 불필요 - 단순 DTO 변환
public static ProductResponse from(Product product) {
    return new ProductResponse(
        product.getId(),
        product.getProductName(),
        // ... 단순 필드 매핑
    );
}
```

---

### 레이어별 테스트 판단 기준

#### Service 레이어

```
┌─────────────────────────────────────────────────────────────┐
│                     Service 메서드                          │
├─────────────────────────────────────────────────────────────┤
│  ✅ 필수: 비즈니스 로직, 조건 분기, 예외 처리, 트랜잭션      │
│  ⚠️ 선택: 단순 조회 + null 체크                             │
│  ❌ 불필요: 단순 Mapper 위임                                 │
└─────────────────────────────────────────────────────────────┘
```

**테스트 필수 메서드 패턴:**
```java
// 패턴 1: 다중 조건 분기
if (condition1) throw ...
if (condition2) throw ...
if (condition3) throw ...

// 패턴 2: 상태 기반 로직
if (order.getStatus() != PENDING) throw ...
order.setStatus(PAID);

// 패턴 3: 복잡한 비즈니스 로직
int totalPrice = items.stream()
    .mapToInt(item -> item.getPrice() * item.getQuantity())
    .sum();
if (totalPrice < 0) throw ...

// 패턴 4: 여러 엔티티 조작
productMapper.decreaseStock(productId, quantity);
orderMapper.insert(order);
orderItemMapper.insertBatch(orderItems);
```

#### Controller 레이어

```
┌─────────────────────────────────────────────────────────────┐
│                    Controller 메서드                        │
├─────────────────────────────────────────────────────────────┤
│  ✅ 필수: 인증/인가 검증, 입력값 검증 (@Valid)              │
│  ⚠️ 선택: 정상 응답 구조 확인                               │
│  ❌ 불필요: Service 호출만 하는 단순 엔드포인트             │
└─────────────────────────────────────────────────────────────┘
```

**테스트 필수 케이스:**
```java
// 인증 필요 엔드포인트 → 401 테스트 필수
@PostMapping
public CommonResult create(@CurrentMemberId Long memberId, ...) { }

// 권한 필요 엔드포인트 → 403 테스트 필수
@PreAuthorize("hasRole('SELLER')")
public CommonResult sellerOnly(...) { }

// 입력 검증 → 400 테스트 필수
public CommonResult create(@Valid @RequestBody CreateRequest request) { }
```

#### Mapper 레이어

```
┌─────────────────────────────────────────────────────────────┐
│                      Mapper 메서드                          │
├─────────────────────────────────────────────────────────────┤
│  ✅ 필수: 복잡한 JOIN, 동적 쿼리, 집계 함수                 │
│  ⚠️ 선택: 기본 CRUD (insert, update, delete)               │
│  ❌ 불필요: 단순 findById, findAll                          │
└─────────────────────────────────────────────────────────────┘
```

**테스트 필수 쿼리 패턴:**
```xml
<!-- 동적 조건 → 테스트 필수 -->
<select id="findAllWithFilters">
    SELECT * FROM products
    <where>
        <if test="category != null">AND category = #{category}</if>
        <if test="minPrice != null">AND price >= #{minPrice}</if>
    </where>
</select>

<!-- 복잡한 JOIN → 테스트 필수 -->
<select id="findOrderWithItems">
    SELECT o.*, oi.*, p.*
    FROM orders o
    JOIN order_items oi ON o.id = oi.order_id
    JOIN products p ON oi.product_id = p.id
</select>
```

---

### 실무 판단 체크리스트

테스트 작성 전 아래 질문에 답하세요:

```
□ 이 메서드에 if/else 또는 switch가 있는가?
  → YES: 각 분기별 테스트 필수

□ 이 메서드가 예외를 던질 수 있는가?
  → YES: 예외 케이스 테스트 필수

□ 이 메서드가 데이터를 변경하는가? (INSERT/UPDATE/DELETE)
  → YES: 변경 결과 검증 테스트 권장

□ 이 메서드에 계산 로직이 있는가?
  → YES: 계산 정확성 테스트 필수

□ 이 메서드가 여러 외부 의존성을 조합하는가?
  → YES: 통합 시나리오 테스트 권장

□ 이 메서드가 단순히 다른 메서드를 호출만 하는가?
  → YES: 테스트 불필요 (호출되는 메서드에서 테스트)
```

---

### 테스트 우선순위

```
우선순위 1 (반드시):
  └─ 결제/주문/재고 관련 비즈니스 로직
  └─ 인증/인가 로직
  └─ 금액 계산 로직

우선순위 2 (권장):
  └─ CRUD 서비스의 예외 처리
  └─ 상태 변경 로직
  └─ 복잡한 조회 쿼리

우선순위 3 (선택):
  └─ 단순 조회 API
  └─ DTO 변환 로직
  └─ 단순 CRUD
```

---

## Test Pyramid

```
        ┌─────────────┐
        │   E2E Test  │  ← 최소한 (비용 높음)
        ├─────────────┤
        │ Integration │  ← Controller, Mapper
        ├─────────────┤
        │  Unit Test  │  ← Service (핵심)
        └─────────────┘
```

| 레벨 | 대상 | 도구 | 목적 |
|------|------|------|------|
| Unit | Service | JUnit5 + Mockito | 비즈니스 로직 검증 |
| Integration | Controller | @WebMvcTest + MockMvc | API 엔드포인트 테스트 |
| Integration | Mapper | @MybatisTest | SQL 쿼리 검증 |

---

## 1. 테스트 클래스 구조

### 1.1 패키지 구조
```
src/test/java/com/fluxmall/
├── {domain}/
│   ├── controller/
│   │   └── {Domain}ControllerTest.java
│   ├── service/
│   │   └── {Domain}ServiceTest.java
│   └── repository/
│       └── {Domain}MapperTest.java
└── support/
    └── TestFixture.java  # 공통 테스트 데이터
```

### 1.2 테스트 클래스 명명
```java
// 단위 테스트
{Domain}ServiceTest.java

// 통합 테스트
{Domain}ControllerTest.java
{Domain}MapperTest.java

// 인수 테스트 (E2E)
{Domain}AcceptanceTest.java
```

---

## 2. 테스트 메서드 명명 규칙

### 2.1 한글 메서드명 (권장)
```java
@Test
@DisplayName("존재하지 않는 상품 조회 시 NOT_FOUND 예외 발생")
void 존재하지_않는_상품_조회시_예외발생() {
    // ...
}
```

### 2.2 영문 메서드명 (대안)
```java
// 패턴: {메서드명}_{시나리오}_{예상결과}
@Test
void getProduct_WithNonExistentId_ThrowsNotFoundException() {
    // ...
}

// 패턴: should_{예상행동}_when_{조건}
@Test
void should_ThrowException_when_ProductNotFound() {
    // ...
}
```

---

## 3. 테스트 구조 (Given-When-Then)

### 3.1 기본 패턴
```java
@Test
@DisplayName("상품 등록 성공")
void 상품_등록_성공() {
    // given - 테스트 데이터 준비
    Long memberId = 1L;
    ProductCreateRequest request = new ProductCreateRequest(
        "테스트 상품",
        "상품 설명",
        "ELECTRONICS",
        10000,
        100
    );

    // when - 테스트 대상 실행
    ProductResponse result = productService.createProduct(memberId, request);

    // then - 결과 검증
    assertThat(result).isNotNull();
    assertThat(result.productName()).isEqualTo("테스트 상품");
    assertThat(result.price()).isEqualTo(10000);
}
```

### 3.2 예외 테스트 패턴
```java
@Test
@DisplayName("존재하지 않는 상품 조회 시 NOT_FOUND 예외 발생")
void 존재하지_않는_상품_조회시_예외발생() {
    // given
    Long nonExistentId = 999L;
    given(productMapper.findById(nonExistentId)).willReturn(null);

    // when & then
    assertThatThrownBy(() -> productService.getProductDetail(nonExistentId))
        .isInstanceOf(BusinessException.class)
        .extracting(e -> ((BusinessException) e).getErrorCode())
        .isEqualTo(ProductError.NOT_FOUND);
}
```

---

## 4. Service 단위 테스트

### 4.1 기본 구조
```java
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductMapper productMapper;

    // 테스트 픽스처
    private Product testProduct;
    private ProductCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
            .id(1L)
            .memberId(1L)
            .productName("테스트 상품")
            .price(10000)
            .stockQuantity(100)
            .productStatus(Product.ProductStatus.ON_SALE)
            .build();

        createRequest = new ProductCreateRequest(
            "테스트 상품", "설명", "ELECTRONICS", 10000, 100
        );
    }

    @Nested
    @DisplayName("상품 조회")
    class GetProduct {

        @Test
        @DisplayName("성공 - 존재하는 상품 조회")
        void 성공_존재하는_상품_조회() {
            // given
            given(productMapper.findById(1L)).willReturn(testProduct);

            // when
            ProductResponse result = productService.getProductDetail(1L);

            // then
            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.productName()).isEqualTo("테스트 상품");
            verify(productMapper).findById(1L);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 상품")
        void 실패_존재하지_않는_상품() {
            // given
            given(productMapper.findById(999L)).willReturn(null);

            // when & then
            assertThatThrownBy(() -> productService.getProductDetail(999L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    BusinessException be = (BusinessException) e;
                    assertThat(be.getErrorCode()).isEqualTo(ProductError.NOT_FOUND);
                });
        }
    }

    @Nested
    @DisplayName("상품 등록")
    class CreateProduct {

        @Test
        @DisplayName("성공 - 정상 등록")
        void 성공_정상_등록() {
            // given
            Long memberId = 1L;

            // when
            ProductResponse result = productService.createProduct(memberId, createRequest);

            // then
            assertThat(result.productName()).isEqualTo("테스트 상품");
            verify(productMapper).insert(any(Product.class));
        }
    }
}
```

### 4.2 Mock 설정 패턴
```java
// 단일 반환값
given(mapper.findById(1L)).willReturn(entity);

// 리스트 반환
given(mapper.findAll()).willReturn(List.of(entity1, entity2));

// void 메서드 검증
doNothing().when(mapper).delete(1L);

// 예외 발생
given(mapper.findById(999L)).willThrow(new RuntimeException());

// ArgumentCaptor 사용
ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
verify(mapper).insert(captor.capture());
assertThat(captor.getValue().getProductName()).isEqualTo("테스트 상품");
```

---

## 5. Controller 통합 테스트

### 5.1 기본 구조
```java
@WebMvcTest(ProductController.class)
@Import(SecurityConfig.class)  // Security 설정 필요시
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @Nested
    @DisplayName("GET /api/products/{productId}")
    class GetProduct {

        @Test
        @DisplayName("성공 - 200 OK")
        void 성공_200_OK() throws Exception {
            // given
            ProductResponse response = new ProductResponse(
                1L, 1L, "테스트 상품", "설명", "ELECTRONICS",
                10000, 100, "ON_SALE", LocalDateTime.now(), LocalDateTime.now()
            );
            given(productService.getProductDetail(1L)).willReturn(response);

            // when & then
            mockMvc.perform(get("/api/products/{productId}", 1L)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("S000"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.productName").value("테스트 상품"))
                .andDo(print());
        }

        @Test
        @DisplayName("실패 - 404 NOT_FOUND")
        void 실패_404_NOT_FOUND() throws Exception {
            // given
            given(productService.getProductDetail(999L))
                .willThrow(new BusinessException(ProductError.NOT_FOUND));

            // when & then
            mockMvc.perform(get("/api/products/{productId}", 999L)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())  // 에러도 200 반환 (body에 에러 코드)
                .andExpect(jsonPath("$.code").value("PD001"))
                .andDo(print());
        }
    }

    @Nested
    @DisplayName("POST /api/seller/products")
    class CreateProduct {

        @Test
        @WithMockUser(roles = "SELLER")
        @DisplayName("성공 - SELLER 권한으로 등록")
        void 성공_SELLER_권한으로_등록() throws Exception {
            // given
            ProductCreateRequest request = new ProductCreateRequest(
                "테스트 상품", "설명", "ELECTRONICS", 10000, 100
            );
            ProductResponse response = new ProductResponse(
                1L, 1L, "테스트 상품", "설명", "ELECTRONICS",
                10000, 100, "ON_SALE", LocalDateTime.now(), LocalDateTime.now()
            );
            given(productService.createProduct(anyLong(), any())).willReturn(response);

            // when & then
            mockMvc.perform(post("/api/seller/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("S000"))
                .andExpect(jsonPath("$.data.productName").value("테스트 상품"))
                .andDo(print());
        }

        @Test
        @DisplayName("실패 - 인증 없이 접근")
        void 실패_인증_없이_접근() throws Exception {
            // given
            ProductCreateRequest request = new ProductCreateRequest(
                "테스트 상품", "설명", "ELECTRONICS", 10000, 100
            );

            // when & then
            mockMvc.perform(post("/api/seller/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andDo(print());
        }
    }
}
```

### 5.2 인증이 필요한 테스트
```java
// JWT 토큰 모킹이 필요한 경우
@Test
@WithMockUser(username = "1", roles = "USER")  // memberId = 1
void 인증된_사용자_테스트() throws Exception {
    // @CurrentMemberId가 "1"을 반환하도록 처리 필요
}

// 커스텀 SecurityContext 사용
@Test
void 커스텀_인증_테스트() throws Exception {
    mockMvc.perform(get("/api/members/me")
            .header("Authorization", "Bearer test-token")
            .with(user("1").roles("USER")))
        .andExpect(status().isOk());
}
```

---

## 6. Mapper 테스트

### 6.1 기본 구조
```java
@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "/sql/test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class ProductMapperTest {

    @Autowired
    private ProductMapper productMapper;

    @Test
    @DisplayName("상품 ID로 조회")
    void findById() {
        // given
        Long productId = 1L;  // test-data.sql에서 생성된 ID

        // when
        Product result = productMapper.findById(productId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getProductName()).isEqualTo("테스트 상품");
    }

    @Test
    @DisplayName("상품 등록")
    void insert() {
        // given
        Product product = Product.builder()
            .memberId(1L)
            .productName("신규 상품")
            .description("설명")
            .category("ELECTRONICS")
            .price(10000)
            .stockQuantity(100)
            .productStatus(Product.ProductStatus.ON_SALE)
            .build();

        // when
        productMapper.insert(product);

        // then
        assertThat(product.getId()).isNotNull();

        Product saved = productMapper.findById(product.getId());
        assertThat(saved.getProductName()).isEqualTo("신규 상품");
    }
}
```

### 6.2 테스트 데이터 SQL
```sql
-- src/test/resources/sql/test-data.sql
INSERT INTO members (id, username, password, nickname, role, created_at, updated_at)
VALUES (1, 'testuser', 'password', '테스트유저', 'USER', NOW(), NOW());

INSERT INTO products (id, member_id, product_name, description, category, price, stock_quantity, product_status, created_at, updated_at)
VALUES (1, 1, '테스트 상품', '상품 설명', 'ELECTRONICS', 10000, 100, 'ON_SALE', NOW(), NOW());

-- src/test/resources/sql/cleanup.sql
DELETE FROM products;
DELETE FROM members;
```

---

## 7. 테스트 픽스처 (Test Fixture)

### 7.1 공통 픽스처 클래스
```java
// src/test/java/com/fluxmall/support/TestFixture.java
public class TestFixture {

    public static Member createMember() {
        return Member.builder()
            .id(1L)
            .username("testuser")
            .password("encoded_password")
            .nickname("테스트유저")
            .role(Member.Role.USER)
            .build();
    }

    public static Member createMember(Long id, String username) {
        return Member.builder()
            .id(id)
            .username(username)
            .password("encoded_password")
            .nickname("테스트유저" + id)
            .role(Member.Role.USER)
            .build();
    }

    public static Product createProduct() {
        return Product.builder()
            .id(1L)
            .memberId(1L)
            .productName("테스트 상품")
            .description("상품 설명")
            .category("ELECTRONICS")
            .price(10000)
            .stockQuantity(100)
            .productStatus(Product.ProductStatus.ON_SALE)
            .build();
    }

    public static Product createProduct(Long id, String name, int price) {
        return Product.builder()
            .id(id)
            .memberId(1L)
            .productName(name)
            .description("상품 설명")
            .category("ELECTRONICS")
            .price(price)
            .stockQuantity(100)
            .productStatus(Product.ProductStatus.ON_SALE)
            .build();
    }

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

### 7.2 픽스처 사용
```java
class ProductServiceTest {

    @Test
    void 테스트() {
        // given
        Product product = TestFixture.createProduct();
        given(productMapper.findById(1L)).willReturn(product);

        // when & then
        // ...
    }
}
```

---

## 8. Assertion 패턴

### 8.1 AssertJ 사용 (권장)
```java
// 기본 검증
assertThat(result).isNotNull();
assertThat(result.getId()).isEqualTo(1L);
assertThat(result.getProductName()).isEqualTo("테스트 상품");

// 컬렉션 검증
assertThat(results).hasSize(3);
assertThat(results).extracting("productName")
    .containsExactly("상품1", "상품2", "상품3");

// 예외 검증
assertThatThrownBy(() -> service.method())
    .isInstanceOf(BusinessException.class)
    .hasMessageContaining("찾을 수 없습니다");

// 예외 없음 검증
assertThatCode(() -> service.method())
    .doesNotThrowAnyException();
```

### 8.2 JSON Path 검증 (Controller)
```java
mockMvc.perform(get("/api/products"))
    .andExpect(jsonPath("$.code").value("S000"))
    .andExpect(jsonPath("$.data").isArray())
    .andExpect(jsonPath("$.data.length()").value(3))
    .andExpect(jsonPath("$.data[0].productName").value("상품1"));
```

---

## 9. 테스트 어노테이션

| 어노테이션 | 용도 |
|-----------|------|
| `@Test` | 테스트 메서드 지정 |
| `@DisplayName` | 테스트 설명 (한글 가능) |
| `@Nested` | 테스트 그룹화 |
| `@BeforeEach` | 각 테스트 전 실행 |
| `@AfterEach` | 각 테스트 후 실행 |
| `@Disabled` | 테스트 비활성화 |
| `@ParameterizedTest` | 파라미터화 테스트 |
| `@WithMockUser` | 인증된 사용자 모킹 |

---

## 10. 파라미터화 테스트

```java
@ParameterizedTest
@DisplayName("유효하지 않은 평점 검증")
@ValueSource(ints = {0, -1, 6, 100})
void 유효하지_않은_평점_검증(int invalidRating) {
    // given
    ReviewCreateRequest request = new ReviewCreateRequest(
        1L, invalidRating, "리뷰 내용", null
    );

    // when & then
    assertThatThrownBy(() -> reviewService.createReview(1L, 1L, request))
        .isInstanceOf(BusinessException.class);
}

@ParameterizedTest
@DisplayName("다양한 정렬 옵션 테스트")
@CsvSource({
    "latest, created_at DESC",
    "priceAsc, price ASC",
    "priceDesc, price DESC"
})
void 정렬_옵션_테스트(String sort, String expectedOrder) {
    // ...
}
```

---

## 11. 테스트 실행

### 11.1 전체 테스트
```bash
./gradlew test
```

### 11.2 특정 클래스 테스트
```bash
./gradlew test --tests "ProductServiceTest"
```

### 11.3 특정 메서드 테스트
```bash
./gradlew test --tests "ProductServiceTest.상품_조회_성공"
```

### 11.4 커버리지 리포트
```bash
./gradlew test jacocoTestReport
# 리포트 위치: build/reports/jacoco/test/html/index.html
```

---

## 12. 체크리스트

### 테스트 작성 시 확인사항
- [ ] `@DisplayName`으로 테스트 목적 명시
- [ ] Given-When-Then 구조 준수
- [ ] 하나의 테스트에 하나의 검증 (단일 책임)
- [ ] 테스트 간 독립성 보장 (공유 상태 없음)
- [ ] 경계값 테스트 포함
- [ ] 예외 케이스 테스트 포함
- [ ] Mock 검증 (`verify`) 적절히 사용

### 테스트 커버리지 목표
- Service 레이어: 80% 이상
- Controller 레이어: 70% 이상
- 핵심 비즈니스 로직: 90% 이상

---

## References

- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [AssertJ Documentation](https://assertj.github.io/doc/)
- [Spring Testing Documentation](https://docs.spring.io/spring-framework/reference/testing.html)

---

**Last Updated**: 2026-01-19
