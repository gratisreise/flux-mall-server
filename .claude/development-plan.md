# FluxMall Server 개발계획서

## 📋 문서 정보

| 항목 | 내용 |
|-----|------|
| **프로젝트명** | FluxMall E-Commerce Backend |
| **작성일** | 2026-01-16 |
| **버전** | v2.0 (DDD 아키텍처 전환 후) |
| **기술 스택** | Spring Boot 3.5.9, MyBatis, MySQL, Redis, JWT |

---

## 🎯 프로젝트 개요

### 비전
온라인 쇼핑몰의 핵심 백엔드 기능을 제공하는 RESTful API 서버 구축

### 핵심 목표
1. **안정적인 인증 시스템**: JWT 기반 Stateless 인증
2. **역할 기반 접근 제어**: USER / SELLER / ADMIN RBAC
3. **완전한 쇼핑 프로세스**: 상품 → 장바구니 → 주문 → 결제 → 리뷰
4. **확장 가능한 아키텍처**: DDD 패키지 구조 적용

---

## 🏗️ 아키텍처 현황

### 적용된 DDD 패키지 구조 ✅

```
com.fluxmall/
├── global/                    # 공통 인프라
│   ├── config/               # SecurityConfig, WebConfig
│   ├── filter/               # JwtAuthenticationFilter, ExceptionHandlerFilter
│   ├── exception/            # GlobalExceptionHandler, BusinessException, ErrorCode
│   ├── response/             # CommonResult, SingleResult, ListResult, ResponseService
│   ├── util/                 # JwtUtil, S3Util, RedisUtil
│   ├── annotation/           # @Password, PasswordValidator
│   └── constant/             # CommonValue
│
├── auth/                      # 인증 도메인
│   ├── service/              # TokenBlacklistService
│   └── exception/            # AuthError
│
├── member/                    # 회원 도메인
│   ├── controller/           # MemberController, AuthController
│   ├── service/              # MemberService
│   ├── repository/           # MemberMapper
│   ├── domain/               # Member
│   ├── dto/request/          # RegisterRequest, LoginRequest
│   ├── dto/response/         # LoginResponse
│   └── address/              # 배송지 서브도메인
│       ├── controller/       # ShippingAddressController
│       ├── service/          # ShippingAddressService
│       ├── repository/       # ShippingAddressMapper
│       └── domain/           # ShippingAddress
│
├── product/                   # 상품 도메인
│   ├── controller/           # ProductController
│   ├── service/              # ProductService
│   ├── repository/           # ProductMapper
│   └── domain/               # Product
│
├── cart/                      # 장바구니 도메인
│   ├── controller/           # CartController
│   ├── service/              # CartService
│   ├── repository/           # CartMapper
│   └── domain/               # Cart, CartItem
│
├── order/                     # 주문 도메인
│   ├── controller/           # OrderController
│   ├── service/              # OrderService
│   ├── repository/           # OrderMapper
│   └── domain/               # Order, OrderItem
│
├── review/                    # 리뷰 도메인
│   ├── controller/           # ReviewController
│   ├── service/              # ReviewService
│   ├── repository/           # ReviewMapper
│   └── domain/               # Review
│
├── wishlist/                  # 찜 목록 도메인
│   ├── controller/           # WishlistController
│   ├── service/              # WishlistService
│   ├── repository/           # WishlistMapper
│   └── domain/               # Wishlist
│
└── recentview/                # 최근 본 상품 도메인
    ├── controller/           # RecentViewController
    ├── service/              # RecentViewService
    ├── repository/           # RecentViewMapper
    └── domain/               # RecentView
```

### 기 구축 현황

| 영역 | 상태 | 세부 내용 |
|-----|------|---------|
| **프로젝트 구조** | ✅ 완료 | DDD 패키지 구조 전환 완료 |
| **JWT 인증 필터** | ✅ 완료 | 상세 예외 처리, memberId 기반 인증 |
| **에러 처리** | ✅ 완료 | GlobalExceptionHandler, ErrorCode 인터페이스 |
| **응답 구조** | ✅ 완료 | ResponseService, CommonResult 체계 |
| **DB 스키마** | ✅ 완료 | Flyway V1 마이그레이션, 11개 테이블 |
| **비즈니스 로직** | ⏳ 진행중 | API 구현 필요 |

---

## 📊 데이터베이스 스키마

### ERD 요약

```
┌─────────────┐       ┌─────────────┐       ┌─────────────┐
│   members   │◄──────│   products  │◄──────│   reviews   │
│  (회원)     │   1:N │  (상품)     │   1:N │  (리뷰)     │
└─────────────┘       └─────────────┘       └─────────────┘
      │                      │                      │
      │1:N                   │1:N                   │
      ▼                      ▼                      │
┌─────────────┐       ┌─────────────┐              │
│   orders    │       │   carts     │              │
│  (주문)     │       │ (장바구니)  │              │
└─────────────┘       └─────────────┘              │
      │                      │                      │
      │1:N                   │1:N                   │
      ▼                      ▼                      │
┌─────────────┐       ┌─────────────┐              │
│ order_item  │       │  cart_item  │              │
│ (주문항목)  │       │(장바구니항목)│              │
└─────────────┘       └─────────────┘              │
                                                   │
┌─────────────┐       ┌─────────────┐              │
│  wishlists  │       │recent_views │◄─────────────┘
│ (찜 목록)   │       │(최근본상품)  │
└─────────────┘       └─────────────┘

┌──────────────────┐
│shipping_addresses│
│   (배송지)       │
└──────────────────┘
```

### 테이블 목록

| 테이블명 | 설명 | 주요 컬럼 |
|---------|------|---------|
| `members` | 회원 정보 | username, password, nickname, role |
| `products` | 상품 정보 | product_name, price, stock_quantity, product_status |
| `orders` | 주문 정보 | order_number, total_price, order_status |
| `order_item` | 주문 상품 | product_id, quantity, price |
| `carts` | 장바구니 | member_id |
| `cart_item` | 장바구니 상품 | product_id, quantity |
| `shipping_addresses` | 배송지 | recipient_name, phone, address, is_default |
| `wishlists` | 찜 목록 | member_id, product_id |
| `recent_views` | 최근 본 상품 | member_id, product_id, viewed_at |
| `reviews` | 리뷰 | rating, content, image_urls, is_deleted |

---

## 🚀 개발 마일스톤

### 전체 로드맵

```
M1 ──────► M2 ──────► M3 ──────► M4 ──────► M5 ──────► M6
인프라/인증   상품      장바구니/주문   위시리스트    리뷰      배송지/Admin
  🔴         🟡         🟡           🟢         🟢         🔵
Critical    High       High        Medium     Medium      Low
```

---

## M1: 인프라 및 인증 시스템 🔴 Critical

### 목표
JWT 기반 인증 시스템 완성 및 역할 기반 접근 제어(RBAC) 구현

### 완료된 작업 ✅

| Task | 상태 | 설명 |
|------|------|------|
| DB 스키마 마이그레이션 | ✅ | Flyway V1, 11개 테이블 |
| JWT 인증 필터 개선 | ✅ | 상세 예외 타입 구분 |
| AuthError 에러 코드 | ✅ | AU001~AU032 세분화 |

### 남은 작업 ⏳

#### Task 1.1: 회원가입 API
```yaml
Endpoint: POST /api/members/register
권한: permitAll

Request:
  username: string (email)
  password: string
  nickname: string

Response:
  code: 200
  message: "회원가입이 완료되었습니다"

구현 위치:
  Controller: member/controller/MemberController.register()
  Service: member/service/MemberService.register()
  Mapper: member/repository/MemberMapper.insertMember()
```

**상세 구현 내용**:
- 이메일(username) 중복 체크
- 닉네임 중복 체크
- BCryptPasswordEncoder로 비밀번호 암호화
- 기본 역할 = USER
- @Password 커스텀 Validation 적용

#### Task 1.2: 로그인 API
```yaml
Endpoint: POST /api/auth/login
권한: permitAll

Request:
  username: string (email)
  password: string

Response:
  code: 200
  data:
    accessToken: string
    refreshToken: string

구현 위치:
  Controller: member/controller/AuthController.login()
  Service: member/service/MemberService.authenticate()
```

**상세 구현 내용**:
- username + password 검증
- JWT Access Token 발급 (30분 만료)
- JWT Refresh Token 발급 (7일 만료)
- Token에 memberId, role claim 포함

#### Task 1.3: 로그아웃 API
```yaml
Endpoint: POST /api/auth/logout
권한: authenticated

Headers:
  Authorization: Bearer {accessToken}
  x-refresh-token: Bearer {refreshToken}

Response:
  code: 200
  message: "로그아웃되었습니다"

구현 위치:
  Controller: member/controller/AuthController.logout()
  Service: auth/service/TokenBlacklistService.addToBlacklist()
```

**상세 구현 내용**:
- Refresh Token을 Redis Blacklist에 추가
- TTL = 토큰 남은 만료시간

#### Task 1.4: 내 정보 조회/수정 API
```yaml
# 조회
Endpoint: GET /api/members/me
권한: authenticated

Response:
  code: 200
  data:
    id: number
    username: string
    nickname: string
    role: string

# 수정
Endpoint: PATCH /api/members/me
권한: authenticated

Request:
  nickname: string

구현 위치:
  Controller: member/controller/MemberController.getMyInfo(), updateMyInfo()
  Service: member/service/MemberService.findById(), updateProfile()
```

### Acceptance Criteria
- [ ] 회원가입 후 로그인 시 JWT 토큰 발급
- [ ] Access Token으로 보호된 API 접근 가능
- [ ] 로그아웃 후 해당 Refresh Token으로 재발급 불가
- [ ] 역할별 API 접근 제어 동작 (USER/SELLER/ADMIN)

---

## M2: 상품 도메인 🟡 High

### 목표
상품 CRUD 및 검색 기능 구현

### Task 2.1: 공개 상품 API (비로그인 가능)

```yaml
# 상품 목록 조회
Endpoint: GET /api/products
Parameters:
  page: int (default: 0)
  size: int (default: 20)
  category: string (optional)
  minPrice: int (optional)
  maxPrice: int (optional)
  sort: string (latest|priceAsc|priceDesc)

# 상품 검색
Endpoint: GET /api/products/search
Parameters:
  keyword: string
  page: int
  size: int

# 상품 상세 조회
Endpoint: GET /api/products/{productId}
Response:
  data:
    id, productName, description, category
    price, stockQuantity, productStatus
    averageRating, reviewCount
```

### Task 2.2: SELLER 전용 상품 관리 API

```yaml
# 상품 등록
Endpoint: POST /api/seller/products
권한: @PreAuthorize("hasRole('SELLER')")

# 상품 수정
Endpoint: PATCH /api/seller/products/{productId}
권한: @PreAuthorize("hasRole('SELLER')")
조건: 본인 상품만 수정 가능

# 상품 삭제 (Soft Delete)
Endpoint: DELETE /api/seller/products/{productId}
권한: @PreAuthorize("hasRole('SELLER')")
처리: product_status = DISCONTINUED

# 상품 상태 변경
Endpoint: PATCH /api/seller/products/{productId}/status
권한: @PreAuthorize("hasRole('SELLER')")
Values: SALE | SOLD_OUT
```

### Acceptance Criteria
- [ ] 비로그인 사용자가 상품 목록/검색/상세 조회 가능
- [ ] SELLER만 상품 등록/수정/삭제 가능
- [ ] 재고 0 시 자동 품절 처리
- [ ] Soft Delete 시 목록에서 제외

---

## M3: 구매 프로세스 🟡 High

### 목표
장바구니 및 주문 핵심 기능 구현

### Task 3.1: 장바구니 API

```yaml
# 장바구니 조회
GET /api/carts
Response: 아이템 목록 + 예상 총액

# 상품 추가
POST /api/carts/items
Request: { productId, quantity }
검증: 재고 초과 방지, 동일 상품 수량 합산

# 수량 수정
PATCH /api/carts/items/{cartItemId}
Request: { quantity }
검증: quantity >= 1, 재고 초과 방지

# 개별 삭제
DELETE /api/carts/items/{cartItemId}

# 일괄 삭제
DELETE /api/carts/items
Request: { cartItemIds: [] }
```

### Task 3.2: 주문 API

```yaml
# 주문 생성
POST /api/orders
Request: { orderItems: [{productId, quantity}], shippingAddressId }
처리: order_status = PENDING

# 결제 처리
POST /api/orders/{orderId}/pay
처리: PENDING → PAID, 재고 차감 (동시성 제어)

# 주문 목록
GET /api/orders
Parameters: page, size

# 주문 상세
GET /api/orders/{orderId}

# 주문 취소
POST /api/orders/{orderId}/cancel
처리: order_status = CANCELLED, 재고 원복
```

### 동시성 제어 전략
```sql
-- 비관적 락 (FOR UPDATE)
SELECT stock_quantity FROM products
WHERE id = #{productId} FOR UPDATE;

-- 재고 차감
UPDATE products
SET stock_quantity = stock_quantity - #{quantity}
WHERE id = #{productId} AND stock_quantity >= #{quantity};
```

### Acceptance Criteria
- [ ] 장바구니 CRUD 동작
- [ ] 재고 초과 시 에러 반환
- [ ] 주문 생성 → 결제 → 재고 차감 플로우
- [ ] 주문 취소 시 재고 원복
- [ ] 동시성 제어로 재고 과다 차감 방지

---

## M4: 사용자 경험 기능 🟢 Medium

### Task 4.1: 위시리스트 API

```yaml
# 위시리스트 조회
GET /api/wishlists
Parameters: page, size, sort

# 찜 토글 (추가/해제)
POST /api/wishlists
Request: { productId }

# 찜 해제
DELETE /api/wishlists/{productId}
```

### Task 4.2: 최근 본 상품 API

```yaml
# 기록 (UPSERT)
POST /api/recent-views
Request: { productId }
처리: 이미 있으면 viewed_at 업데이트

# 조회
GET /api/recent-views
Parameters: limit (default: 20, max: 50)
정렬: viewed_at DESC

# 자동 정리
50개 초과 시 오래된 기록 자동 삭제
```

### Acceptance Criteria
- [ ] 상품 찜 토글 동작
- [ ] 최근 본 상품 자동 기록 및 조회
- [ ] 중복 제거 및 최대 개수 제한 동작

---

## M5: 리뷰 및 평점 시스템 🟢 Medium

### Task 5.1: 리뷰 API

```yaml
# 리뷰 목록 (공개)
GET /api/products/{productId}/reviews
Parameters:
  hasImage: boolean (사진 있는 리뷰만)
  sort: latest | ratingDesc
  page, size

# 리뷰 작성
POST /api/products/{productId}/reviews
Request:
  orderItemId: number (구매 검증용)
  rating: 1~5
  content: string
  imageUrls: string[] (최대 5장)
검증: 구매 확정된 상품만 작성 가능

# 리뷰 수정
PATCH /api/reviews/{reviewId}
조건: 본인 리뷰만

# 리뷰 삭제 (Soft Delete)
DELETE /api/reviews/{reviewId}
조건: 본인 리뷰만
```

### 평균 평점 계산
```sql
UPDATE products SET average_rating = (
  SELECT AVG(rating) FROM reviews
  WHERE product_id = #{productId} AND is_deleted = 0
) WHERE id = #{productId};
```

### Acceptance Criteria
- [ ] 구매 확정 후 리뷰 작성 가능
- [ ] 리뷰 CRUD 시 평균 평점 재계산
- [ ] 사진 리뷰 필터링 동작
- [ ] 본인 리뷰만 수정/삭제 가능

---

## M6: 배송지 관리 및 ADMIN 🔵 Low

### Task 6.1: 배송지 관리 API

```yaml
# 배송지 목록
GET /api/members/me/addresses

# 배송지 등록
POST /api/members/me/addresses
Request: { recipientName, phone, postcode, address1, address2, isDefault }
처리: isDefault=true 시 기존 기본 배송지 해제

# 배송지 수정
PATCH /api/members/me/addresses/{addressId}

# 배송지 삭제
DELETE /api/members/me/addresses/{addressId}

# 기본 배송지 설정
PATCH /api/members/me/addresses/{addressId}/default
```

### Task 6.2: ADMIN 전용 API

```yaml
# 상품 강제 삭제
DELETE /api/admin/products/{productId}
권한: @PreAuthorize("hasRole('ADMIN')")
처리: Hard Delete

# 부적절 리뷰 강제 삭제
DELETE /api/admin/reviews/{reviewId}
권한: @PreAuthorize("hasRole('ADMIN')")
```

### Acceptance Criteria
- [ ] 다중 배송지 CRUD 동작
- [ ] 기본 배송지 설정 시 기존 해제
- [ ] ADMIN만 강제 삭제 가능

---

## 🔧 기술 명세

### 역할 기반 접근 제어 (RBAC)

```java
// SecurityConfig.java
http.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/auth/**", "/api/products/**").permitAll()
    .requestMatchers("/api/seller/**").hasRole("SELLER")
    .requestMatchers("/api/admin/**").hasRole("ADMIN")
    .anyRequest().authenticated()
);

// Controller
@PreAuthorize("hasRole('SELLER')")
public ResponseEntity<?> createProduct(...) { }
```

### JWT Token 구조

```json
// Access Token Payload
{
  "sub": "buyer@example.com",
  "memberId": 1,
  "role": "USER",
  "iat": 1736000000,
  "exp": 1736001800
}
```

### Soft Delete 패턴

```java
// Entity
private LocalDateTime deletedAt;

// Mapper XML
<select id="findAll">
  SELECT * FROM products WHERE deleted_at IS NULL
</select>

<update id="softDelete">
  UPDATE products SET deleted_at = NOW() WHERE id = #{id}
</update>
```

### 에러 코드 체계

| 도메인 | 코드 범위 | 예시 |
|-------|---------|------|
| Auth | AU001~AU099 | AU001: 유효하지 않은 토큰 |
| Member | ME001~ME099 | ME001: 중복 이메일 |
| Product | PD001~PD099 | PD001: 상품 없음 |
| Order | OD001~OD099 | OD001: 재고 부족 |
| Cart | CT001~CT099 | CT001: 수량 초과 |

---

## 🧪 테스트 전략

### 테스트 레벨

| 레벨 | 도구 | 대상 | 목표 |
|-----|------|-----|------|
| Unit | JUnit5 + Mockito | Service | 비즈니스 로직 검증 |
| Mapper | @MybatisTest | Mapper | SQL 쿼리 검증 |
| Integration | @SpringBootTest | API | 전체 플로우 검증 |
| Security | @WithMockUser | Controller | RBAC 검증 |

### 테스트 코드 예시

```java
// Service Unit Test
@ExtendWith(MockitoExtension.class)
class MemberServiceTest {
    @Mock MemberMapper memberMapper;
    @InjectMocks MemberService memberService;

    @Test
    void registerWithDuplicateEmail_throwsException() {
        when(memberMapper.existsByUsername(any())).thenReturn(true);

        assertThrows(BusinessException.class, () -> {
            memberService.register(new RegisterRequest(...));
        });
    }
}

// Security Test
@WebMvcTest(ProductController.class)
class ProductControllerSecurityTest {
    @Test
    @WithMockUser(roles = "USER")
    void userCannotCreateProduct() throws Exception {
        mockMvc.perform(post("/api/seller/products"))
            .andExpect(status().isForbidden());
    }
}
```

---

## 📝 개발 워크플로우

### Feature 개발 순서

```
1️⃣ Domain/Entity → 2️⃣ Repository/Mapper → 3️⃣ Service → 4️⃣ Controller → 5️⃣ Test
```

### 상세 단계

| 단계 | 작업 | 산출물 |
|-----|------|-------|
| 1 | Entity 작성 | `domain/*.java` |
| 2 | DTO 작성 | `dto/request/*.java`, `dto/response/*.java` |
| 3 | Mapper 인터페이스 | `repository/*Mapper.java` |
| 4 | Mapper XML | `resources/mappers/**/*.xml` |
| 5 | Service 구현 | `service/*Service.java` |
| 6 | Controller 구현 | `controller/*Controller.java` |
| 7 | 단위 테스트 | `test/**/*Test.java` |
| 8 | 통합 테스트 | `test/**/*IntegrationTest.java` |

### 브랜치 전략

```
main
  └── develop
        ├── feat(auth)     ← 현재 브랜치
        ├── feat(product)
        ├── feat(cart)
        ├── feat(order)
        └── ...
```

---

## 📅 진행 상황

### 현재 상태

| Milestone | 진행률 | 상태 |
|-----------|-------|------|
| M1 | 60% | 🔄 진행중 |
| M2 | 0% | ⏳ 대기 |
| M3 | 0% | ⏳ 대기 |
| M4 | 0% | ⏳ 대기 |
| M5 | 0% | ⏳ 대기 |
| M6 | 0% | ⏳ 대기 |

### 다음 작업

1. **즉시**: MemberMapper.xml SQL 작성
2. **다음**: MemberService.register() 구현
3. **이후**: AuthController.login() 구현

---

## 📚 참조 문서

| 문서 | 경로 | 설명 |
|-----|------|------|
| PRD | `.claude/prd.txt` | 제품 요구사항 |
| API 명세 | `.claude/api.json` | OpenAPI 스펙 |
| DB 스키마 | `.claude/db.sql` | 테이블 정의 |
| 마일스톤 | `.claude/milestones.md` | 상세 마일스톤 |
| DDD 설계 | `.claude/ddd-refactoring-design.md` | 아키텍처 설계 |
| Git 컨벤션 | `.claude/gitconventtions.md` | 커밋 규칙 |

---

**작성자**: Claude Code
**최종 수정일**: 2026-01-16
**버전**: v2.0
