# FluxMall Server - Project Milestones

## Overview

| Milestone | Target | Priority | Status |
|-----------|--------|----------|--------|
| M1 | 인프라 및 인증 시스템 | 🔴 Critical | 🔄 In Progress |
| M2 | 상품 도메인 | 🟡 High | ⏳ Pending |
| M3 | 구매 프로세스 (장바구니/주문) | 🟡 High | ⏳ Pending |
| M4 | 사용자 경험 (위시리스트/최근본상품) | 🟢 Medium | ⏳ Pending |
| M5 | 리뷰 및 평점 시스템 | 🟢 Medium | ⏳ Pending |
| M6 | 배송지 관리 및 ADMIN 기능 | 🔵 Low | ⏳ Pending |

---

## M1: 인프라 및 인증 시스템 🔴

### 목표
JWT 기반 인증 시스템 완성 및 역할 기반 접근 제어(RBAC) 구현

### Tasks

#### 1.1 데이터베이스 스키마 마이그레이션 ✅
- [x] DB 스키마 설계 (`.claude/db.sql`)
- [x] Flyway 마이그레이션 파일 생성 (`V1__init_schema.sql`)
- [x] 11개 테이블 정의 완료

#### 1.2 JWT 인증 필터 개선 ✅
- [x] JwtUtil: 상세 예외 타입 구분 (Expired, Malformed, Unsupported)
- [x] JwtAuthenticationFilter: memberId 기반 직접 인증
- [x] ExceptionHandlerFilter: BusinessException JSON 응답 개선
- [x] AuthError: 토큰 관련 에러 코드 세분화 (AU001~AU032)

#### 1.3 회원가입 API 구현 ⏳
- [ ] `POST /api/members/register`
- [ ] 이메일/닉네임 중복 체크
- [ ] 비밀번호 BCrypt 암호화
- [ ] 기본 역할 = USER

**구현 위치**:
```
Controller: MemberController.register()
Service: MemberService.register(RegisterRequest)
Mapper: MemberMapper.insertMember(), existsByUsername(), existsByNickname()
```

#### 1.4 로그인 API 구현 ⏳
- [ ] `POST /api/auth/login`
- [ ] 사용자 인증 (username + password)
- [ ] JWT Access Token + Refresh Token 발급
- [ ] Token에 role 포함

**구현 위치**:
```
Controller: AuthController.login()
Service: MemberService.authenticate(LoginRequest)
```

#### 1.5 로그아웃 API 구현 ⏳
- [ ] `POST /api/auth/logout`
- [ ] Refresh Token Blacklist 추가 (Redis TTL)

**구현 위치**:
```
Controller: AuthController.logout()
Service: TokenBlacklistService.addToBlacklist()
```

#### 1.6 내 정보 조회/수정 API ⏳
- [ ] `GET /api/members/me`
- [ ] `PATCH /api/members/me`
- [ ] JWT에서 memberId 추출

**구현 위치**:
```
Controller: MemberController.getMyInfo(), updateMyInfo()
Service: MemberService.findById(), updateProfile()
```

### Acceptance Criteria
- [ ] 회원가입 후 로그인 시 JWT 토큰 발급
- [ ] Access Token으로 보호된 API 접근 가능
- [ ] 로그아웃 후 해당 토큰으로 접근 불가
- [ ] 역할별 API 접근 제어 동작

---

## M2: 상품 도메인 🟡

### 목표
상품 CRUD 및 검색 기능 구현 (공개 API + SELLER 전용 API)

### Tasks

#### 2.1 공개 상품 API (비로그인 가능)
- [ ] `GET /api/products` - 상품 목록 조회 (페이징, 필터링, 정렬)
- [ ] `GET /api/products/search` - 키워드 검색 (LIKE %keyword%)
- [ ] `GET /api/products/{productId}` - 상품 상세 조회 (재고, 평점)

**구현 위치**:
```
Controller: ProductController.getProducts(), searchProducts(), getProduct()
Service: ProductService.getProducts(), searchByKeyword(), getProductDetail()
Mapper: ProductMapper.selectProductsWithFilters(), searchByKeyword(), selectProductWithDetails()
```

#### 2.2 SELLER 전용 상품 관리 API
- [ ] `POST /api/seller/products` - 상품 등록 (@PreAuthorize("hasRole('SELLER')"))
- [ ] `PATCH /api/seller/products/{productId}` - 상품 수정 (본인 상품만)
- [ ] `DELETE /api/seller/products/{productId}` - 상품 삭제 (Soft Delete)
- [ ] `PATCH /api/seller/products/{productId}/status` - 품절/판매중 상태 변경

**구현 위치**:
```
Controller: ProductController (with @PreAuthorize)
Service: ProductService.createProduct(), updateProduct(), softDeleteProduct(), updateProductStatus()
Mapper: ProductMapper.insertProduct(), updateProduct(), softDeleteProduct(), updateProductStatus()
```

### Acceptance Criteria
- [ ] 비로그인 사용자가 상품 목록/검색/상세 조회 가능
- [ ] SELLER만 상품 등록/수정/삭제 가능
- [ ] 재고 0 시 자동 품절 처리
- [ ] Soft Delete 시 목록에서 제외

---

## M3: 구매 프로세스 (장바구니/주문) 🟡

### 목표
장바구니 및 주문 핵심 기능 구현

### Tasks

#### 3.1 장바구니 API
- [ ] `GET /api/carts` - 장바구니 조회 (예상 총액 포함)
- [ ] `POST /api/carts/items` - 상품 추가 (재고 초과 방지, 수량 합산)
- [ ] `PATCH /api/carts/items/{cartItemId}` - 수량 수정
- [ ] `DELETE /api/carts/items/{cartItemId}` - 개별 삭제
- [ ] `DELETE /api/carts/items` - 일괄 삭제 (body: cartItemIds[])

**구현 위치**:
```
Controller: CartController
Service: CartService.getCartWithItems(), addItem(), updateItemQuantity(), removeItem(), removeItems()
Mapper: CartMapper
```

#### 3.2 주문 API
- [ ] `POST /api/orders` - 주문 생성 (PENDING 상태)
- [ ] `POST /api/orders/{orderId}/pay` - 결제 요청 (PENDING → PAID)
- [ ] `GET /api/orders` - 주문 목록 조회 (페이징)
- [ ] `GET /api/orders/{orderId}` - 주문 상세 조회
- [ ] `POST /api/orders/{orderId}/cancel` - 주문 전체 취소 (재고 원복)

**동시성 제어**:
```sql
-- 비관적 락 (FOR UPDATE)
SELECT stock_quantity FROM product WHERE product_id = #{productId} FOR UPDATE
```

**구현 위치**:
```
Controller: OrderController
Service: OrderService.createOrder(), processPayment(), getOrdersByMember(), getOrderDetail(), cancelOrder()
Mapper: OrderMapper, ProductMapper.decreaseStock(), increaseStock()
```

### Acceptance Criteria
- [ ] 장바구니에 상품 추가/수정/삭제 가능
- [ ] 재고 초과 시 에러 반환
- [ ] 주문 생성 → 결제 → 재고 차감 플로우 동작
- [ ] 주문 취소 시 재고 원복
- [ ] 동시성 제어로 재고 과다 차감 방지

---

## M4: 사용자 경험 (위시리스트/최근본상품) 🟢

### 목표
사용자 경험 향상을 위한 부가 기능 구현

### Tasks

#### 4.1 위시리스트 API
- [ ] `GET /api/wishlists` - 위시리스트 조회 (페이징, 정렬)
- [ ] `POST /api/wishlists` - 찜 추가/해제 (토글)
- [ ] `DELETE /api/wishlists/{productId}` - 찜 해제 (명시적)

**구현 위치**:
```
Controller: WishlistController
Service: WishlistService.getWishlists(), toggleWishlist(), removeWishlist()
Mapper: WishlistMapper.selectWishlistsByMember(), existsWishlist(), insertWishlist(), deleteWishlist()
```

#### 4.2 최근 본 상품 API
- [ ] `POST /api/recent-views` - 최근 본 상품 기록 (UPSERT)
- [ ] `GET /api/recent-views` - 최근 본 상품 목록 (limit 20~50)
- [ ] 오래된 기록 자동 삭제 (50개 초과 시)

**구현 위치**:
```
Controller: RecentViewController
Service: RecentViewService.recordView(), getRecentViews(), cleanupOldViews()
Mapper: RecentViewMapper.upsertRecentView(), selectRecentViewsByMember(), deleteOldestViews()
```

### Acceptance Criteria
- [ ] 상품 찜 토글 동작
- [ ] 최근 본 상품 자동 기록 및 조회
- [ ] 중복 제거 및 최대 개수 제한 동작

---

## M5: 리뷰 및 평점 시스템 🟢

### 목표
상품 리뷰 및 평점 시스템 구현

### Tasks

#### 5.1 리뷰 API
- [ ] `GET /api/products/{productId}/reviews` - 리뷰 목록 조회 (공개)
  - 필터: hasImage=true (사진 있는 리뷰만)
  - 정렬: latest, ratingDesc
- [ ] `POST /api/products/{productId}/reviews` - 리뷰 작성 (구매 확정 검증)
  - rating: 1~5
  - imageUrls: 최대 5장
- [ ] `PATCH /api/reviews/{reviewId}` - 리뷰 수정 (본인만)
- [ ] `DELETE /api/reviews/{reviewId}` - 리뷰 삭제 (Soft Delete, 본인만)

**평균 평점 계산**:
```sql
UPDATE product SET average_rating = (
  SELECT AVG(rating) FROM review
  WHERE product_id = #{productId} AND is_deleted = 0
) WHERE product_id = #{productId}
```

**구현 위치**:
```
Controller: ReviewController
Service: ReviewService.getReviews(), createReview(), updateReview(), softDeleteReview()
Mapper: ReviewMapper, ProductMapper.updateAverageRating()
```

### Acceptance Criteria
- [ ] 구매 확정 후 리뷰 작성 가능
- [ ] 리뷰 작성/수정/삭제 시 평균 평점 재계산
- [ ] 사진 리뷰 필터링 동작
- [ ] 본인 리뷰만 수정/삭제 가능

---

## M6: 배송지 관리 및 ADMIN 기능 🔵

### 목표
배송지 관리 및 관리자 전용 기능 구현

### Tasks

#### 6.1 배송지 관리 API
- [ ] `GET /api/members/me/addresses` - 배송지 목록 조회
- [ ] `POST /api/members/me/addresses` - 배송지 등록
- [ ] `PATCH /api/members/me/addresses/{addressId}` - 배송지 수정
- [ ] `DELETE /api/members/me/addresses/{addressId}` - 배송지 삭제
- [ ] `PATCH /api/members/me/addresses/{addressId}/default` - 기본 배송지 설정

**구현 위치**:
```
Controller: ShippingAddressController
Service: ShippingAddressService
Mapper: ShippingAddressMapper
```

#### 6.2 ADMIN 전용 API
- [ ] `DELETE /api/admin/products/{productId}` - 상품 강제 삭제 (Hard Delete)
- [ ] `DELETE /api/admin/reviews/{reviewId}` - 부적절 리뷰 강제 삭제

**구현 위치**:
```
Controller: AdminProductController, AdminReviewController
Service: ProductService.forceDeleteProduct(), ReviewService.forceDeleteReview()
```

### Acceptance Criteria
- [ ] 다중 배송지 CRUD 동작
- [ ] 기본 배송지 설정 시 기존 해제
- [ ] ADMIN만 강제 삭제 가능

---

## Technical Specifications

### 동시성 제어
```java
// MyBatis: SELECT ... FOR UPDATE
@Select("SELECT * FROM product WHERE id = #{id} FOR UPDATE")
Product selectForUpdate(Long id);
```

### Soft Delete 패턴
```java
// Entity
private LocalDateTime deletedAt;

// Mapper (조회 시)
WHERE deleted_at IS NULL
```

### 역할 기반 접근 제어 (RBAC)
```java
// SecurityConfig
.requestMatchers("/api/seller/**").hasRole("SELLER")
.requestMatchers("/api/admin/**").hasRole("ADMIN")

// Controller
@PreAuthorize("hasRole('SELLER')")
public ResponseEntity<?> createProduct(...) { }
```

### 재고 0 자동 품절 처리
```java
// ProductService
if (product.getStockQuantity() == 0) {
    productMapper.updateProductStatus(productId, ProductStatus.SOLD_OUT);
}
```

---

## Development Workflow (Per Feature)

```
1️⃣ Entity/DTO 구현 → 2️⃣ Mapper 인터페이스 + XML → 3️⃣ Service 로직 → 4️⃣ Controller API → 5️⃣ 테스트
```

### 테스트 전략
| 레벨 | 도구 | 목적 |
|------|-----|------|
| Unit | JUnit5 + Mockito | Service 비즈니스 로직 검증 |
| Integration | @SpringBootTest + MockMvc | API 엔드포인트 테스트 |
| Mapper | @MybatisTest | SQL 쿼리 검증 |
| Security | @WithMockUser | 역할 기반 접근 제어 검증 |

---

## Progress Tracking

### Current Status
- **Milestone 1**: 🔄 In Progress (JWT 필터 개선 완료, API 구현 대기)
- **Milestone 2-6**: ⏳ Pending

### Next Actions
1. `MemberMapper.xml` 작성 (회원 CRUD SQL)
2. `MemberService.register()` 구현
3. `AuthController.login()` 구현
4. 단위 테스트 작성

---

## References
- PRD: `.claude/prd.txt`
- API 명세: `.claude/api.json`
- DB 스키마: `.claude/db.sql`
- 구현 계획: `.claude/implementation-plan.md`
- 개발 워크플로우: `.claude/development-workflow.md`

---

**Last Updated**: 2026-01-15
**Author**: Claude Code
