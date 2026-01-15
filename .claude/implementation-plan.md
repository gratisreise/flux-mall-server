# FluxMall 서버 구현 계획서

## 📋 문서 정보
- **작성일**: 2026-01-13
- **프로젝트**: FluxMall E-Commerce Backend
- **기술 스택**: Spring Boot 3.5.9, MyBatis, MySQL, Redis, JWT
- **참조 문서**:
  - PRD: `.claude/prd.txt`
  - API 명세: `.claude/api.json`

---

## 🎯 프로젝트 현황 분석

### 기 구축된 인프라
✅ **인증/보안 시스템**
- JWT 기반 인증 (JwtUtil, JwtAuthenticationFilter, ExceptionHandlerFilter)
- Spring Security 설정 (SecurityConfig)
- Token Blacklist 서비스 (Redis 기반)
- 공통 응답 구조 (ResponseService, CommonResult, SingleResult, ListResult)
- 글로벌 예외 처리 (GlobalExceptionHandler, ErrorCode 인터페이스)

✅ **기본 구조**
- 도메인별 패키지 구조 (controller, service, mapper, domain/entity, domain/dto)
- MyBatis 설정 (XML 매퍼 기반)
- Lombok, Validation 설정
- AWS S3, Redis, Caffeine 캐시 의존성 추가
- Actuator + Prometheus 모니터링 준비
- Sentry 에러 트래킹 설정

### 미구현 영역
❌ **데이터베이스 스키마**
- Flyway 마이그레이션 파일 없음
- 테이블 정의 필요

❌ **비즈니스 로직**
- 대부분의 서비스 메서드가 기본 틀만 존재
- API 엔드포인트 미구현 또는 부분 구현

❌ **역할 기반 접근 제어 (RBAC)**
- USER / SELLER / ADMIN 역할 구분 로직 필요
- @PreAuthorize 등 권한 체크 미적용

---

## 🚀 구현 우선순위 및 단계

### Phase 1: 핵심 인프라 구축 (필수 선행)
**목표**: 데이터베이스 마이그레이션 적용 및 인증 시스템 완성

#### 1.1 데이터베이스 스키마 마이그레이션 ✅ **완료**
**우선순위**: 🔴 최우선
**상태**: ✅ **완료** (2026-01-13)

**완료된 작업**:
- ✅ DB 스키마 설계 완료 (`.claude/db.sql`)
- ✅ Flyway 마이그레이션 파일 생성 (`src/main/resources/db/migration/V1__init_schema.sql`)
- ✅ 11개 테이블 정의 완료 (members, products, orders, order_item, carts, cart_item, shipping_addresses, wishlists, recent_views, reviews)
- ✅ 외래 키 제약 조건 설정 완료

**향후 고려사항** (필요시 추가 마이그레이션):
- 성능 최적화 인덱스 추가 (category, status, seller 등)
- 평균 평점 컬럼 추가 (`products.average_rating`)

---

#### 1.2 회원가입 및 인증 시스템 완성
**우선순위**: 🔴 최우선

**작업 내용**:
1. **회원가입 API 구현**
   - 엔드포인트: `POST /api/members/register`
   - 기능:
     - 이메일 중복 체크 (username UNIQUE 제약)
     - 닉네임 중복 체크
     - 비밀번호 암호화 (BCryptPasswordEncoder)
     - 기본 역할 = USER
     - @Password 커스텀 Validation 활용
   - 구현 위치:
     - Controller: `MemberController.register()`
     - Service: `MemberService.register(RegisterRequest)`
     - Mapper: `MemberMapper.insertMember()`, `existsByUsername()`, `existsByNickname()`

2. **로그인 API 구현**
   - 엔드포인트: `POST /api/auth/login`
   - 기능:
     - 사용자 인증 (username + password)
     - JWT Access Token + Refresh Token 발급
     - Token에 role 포함 (claim: "role": "USER")
   - 구현 위치:
     - Controller: `AuthController.login()`
     - Service: `MemberService.authenticate(LoginRequest)`
     - JwtUtil: 토큰에 role claim 추가

3. **로그아웃 API 구현**
   - 엔드포인트: `POST /api/auth/logout`
   - 기능:
     - Refresh Token Blacklist에 추가 (Redis TTL 설정)
   - 구현 위치:
     - Controller: `AuthController.logout()`
     - Service: `TokenBlacklistService.addToBlacklist()`

4. **내 정보 조회/수정**
   - 엔드포인트: `GET /api/members/me`, `PATCH /api/members/me`
   - 기능:
     - JWT에서 memberId 추출하여 회원 정보 조회
     - 닉네임 수정 (중복 체크)
   - 구현 위치:
     - Controller: `MemberController.getMyInfo()`, `updateMyInfo()`
     - Service: `MemberService.findById()`, `updateProfile()`

**예상 소요**: 2-3일

---

### Phase 2: 상품 도메인 구현
**목표**: 상품 CRUD 및 검색 기능 구현

#### 2.1 공개 상품 API (비로그인 가능)
**우선순위**: 🟡 높음

**작업 내용**:
1. **상품 목록 조회**
   - 엔드포인트: `GET /api/products`
   - 기능:
     - 페이징 (page, size)
     - 필터링 (category, minPrice, maxPrice)
     - 정렬 (latest, priceAsc, priceDesc)
     - deleted_at IS NULL 조건 (Soft Delete)
   - 구현 위치:
     - Controller: `ProductController.getProducts()`
     - Service: `ProductService.getProducts(ProductSearchRequest)`
     - Mapper: `ProductMapper.selectProductsWithFilters()`

2. **상품 키워드 검색**
   - 엔드포인트: `GET /api/products/search?keyword=노트북`
   - 기능:
     - 상품명, 설명 부분 일치 검색 (LIKE %keyword%)
     - 페이징
   - 구현 위치:
     - Controller: `ProductController.searchProducts()`
     - Service: `ProductService.searchByKeyword()`
     - Mapper: `ProductMapper.searchByKeyword()`

3. **상품 상세 조회**
   - 엔드포인트: `GET /api/products/{productId}`
   - 기능:
     - 상품 정보, 재고량, 평균 평점 반환
   - 구현 위치:
     - Controller: `ProductController.getProduct()`
     - Service: `ProductService.getProductDetail()`
     - Mapper: `ProductMapper.selectProductWithDetails()`

**예상 소요**: 2-3일

---

#### 2.2 SELLER 전용 상품 관리 API
**우선순위**: 🟡 높음

**작업 내용**:
1. **상품 등록**
   - 엔드포인트: `POST /api/seller/products`
   - 권한: @PreAuthorize("hasRole('SELLER')")
   - 기능:
     - JWT에서 memberId 추출하여 판매자 ID로 설정
     - 상품 등록 시 product_status = SALE
   - 구현 위치:
     - Controller: `ProductController.createProduct()` with @PreAuthorize
     - Service: `ProductService.createProduct(memberId, ProductCreateRequest)`
     - Mapper: `ProductMapper.insertProduct()`

2. **상품 수정**
   - 엔드포인트: `PATCH /api/seller/products/{productId}`
   - 권한: @PreAuthorize("hasRole('SELLER')")
   - 기능:
     - 본인이 등록한 상품만 수정 가능 (member_id 검증)
   - 구현 위치:
     - Controller: `ProductController.updateProduct()`
     - Service: `ProductService.updateProduct(memberId, productId, request)`
     - Mapper: `ProductMapper.updateProduct()`

3. **상품 삭제 (Soft Delete)**
   - 엔드포인트: `DELETE /api/seller/products/{productId}`
   - 권한: @PreAuthorize("hasRole('SELLER')")
   - 기능:
     - product_status = DISCONTINUED, deleted_at = NOW()
   - 구현 위치:
     - Controller: `ProductController.deleteProduct()`
     - Service: `ProductService.softDeleteProduct(memberId, productId)`
     - Mapper: `ProductMapper.softDeleteProduct()`

4. **품절/판매중 상태 변경**
   - 엔드포인트: `PATCH /api/seller/products/{productId}/status`
   - 권한: @PreAuthorize("hasRole('SELLER')")
   - 기능:
     - status: SALE / SOLD_OUT 변경
     - 재고 0일 때 자동 SOLD_OUT 로직 추가 (트리거 또는 애플리케이션 로직)
   - 구현 위치:
     - Controller: `ProductController.updateProductStatus()`
     - Service: `ProductService.updateProductStatus(memberId, productId, status)`
     - Mapper: `ProductMapper.updateProductStatus()`

**예상 소요**: 2-3일

---

### Phase 3: 장바구니 및 주문 도메인
**목표**: 구매 프로세스 핵심 기능 구현

#### 3.1 장바구니 (Cart) API
**우선순위**: 🟡 높음

**작업 내용**:
1. **장바구니 조회**
   - 엔드포인트: `GET /api/carts`
   - 권한: @PreAuthorize("hasRole('USER')")
   - 기능:
     - 현재 장바구니 아이템 목록 + 예상 총 결제 금액
   - 구현 위치:
     - Controller: `CartController.getCart()`
     - Service: `CartService.getCartWithItems(memberId)`
     - Mapper: `CartMapper.selectCartWithItems()`

2. **장바구니에 상품 추가**
   - 엔드포인트: `POST /api/carts/items`
   - 권한: @PreAuthorize("hasRole('USER')")
   - 기능:
     - 재고 초과 방지 (quantity <= stock_quantity)
     - 동일 상품 이미 있으면 수량 합산
   - 구현 위치:
     - Controller: `CartController.addCartItem()`
     - Service: `CartService.addItem(memberId, productId, quantity)`
     - Mapper: `CartMapper.insertCartItem()`, `updateCartItemQuantity()`

3. **장바구니 수량 수정**
   - 엔드포인트: `PATCH /api/carts/items/{cartItemId}`
   - 권한: @PreAuthorize("hasRole('USER')")
   - 기능:
     - 재고 초과 방지
     - quantity >= 1
   - 구현 위치:
     - Controller: `CartController.updateCartItem()`
     - Service: `CartService.updateItemQuantity(memberId, cartItemId, quantity)`
     - Mapper: `CartMapper.updateCartItemQuantity()`

4. **장바구니 상품 삭제**
   - 엔드포인트: `DELETE /api/carts/items/{cartItemId}` (개별)
   - 엔드포인트: `DELETE /api/carts/items` (일괄, body: cartItemIds[])
   - 권한: @PreAuthorize("hasRole('USER')")
   - 구현 위치:
     - Controller: `CartController.deleteCartItem()`, `deleteCartItems()`
     - Service: `CartService.removeItem()`, `removeItems()`
     - Mapper: `CartMapper.deleteCartItem()`, `deleteCartItems()`

**예상 소요**: 2-3일

---

#### 3.2 주문 (Order) API
**우선순위**: 🟡 높음

**작업 내용**:
1. **주문 생성 (PENDING 상태)**
   - 엔드포인트: `POST /api/orders`
   - 권한: @PreAuthorize("hasRole('USER')")
   - 기능:
     - orderItems로 장바구니 전체 또는 바로구매 처리
     - 재고 확인 및 예약 (실제 차감은 결제 완료 시)
     - order_status = PENDING
     - 총 금액 계산 (unit_price * quantity)
   - 구현 위치:
     - Controller: `OrderController.createOrder()`
     - Service: `OrderService.createOrder(memberId, OrderCreateRequest)`
     - Mapper: `OrderMapper.insertOrder()`, `insertOrderItems()`

2. **결제 요청 (PENDING → PAID)**
   - 엔드포인트: `POST /api/orders/{orderId}/pay`
   - 권한: @PreAuthorize("hasRole('USER')")
   - 기능:
     - 모든 결제 성공 처리 (실제 결제 연동 없음)
     - 재고 정식 차감 (동시성 제어: 비관적 락 또는 낙관적 락)
     - order_status = PAID
   - 구현 위치:
     - Controller: `OrderController.payOrder()`
     - Service: `OrderService.processPayment(memberId, orderId)`
     - Mapper: `OrderMapper.updateOrderStatus()`, `ProductMapper.decreaseStock()`

3. **주문 목록 조회**
   - 엔드포인트: `GET /api/orders`
   - 권한: @PreAuthorize("hasRole('USER')")
   - 기능:
     - 회원별 주문 목록 (페이징)
   - 구현 위치:
     - Controller: `OrderController.getOrders()`
     - Service: `OrderService.getOrdersByMember(memberId, page, size)`
     - Mapper: `OrderMapper.selectOrdersByMember()`

4. **주문 상세 조회**
   - 엔드포인트: `GET /api/orders/{orderId}`
   - 권한: @PreAuthorize("hasRole('USER')")
   - 기능:
     - 주문 상세 정보, 주문 아이템 목록
   - 구현 위치:
     - Controller: `OrderController.getOrder()`
     - Service: `OrderService.getOrderDetail(memberId, orderId)`
     - Mapper: `OrderMapper.selectOrderWithItems()`

5. **주문 전체 취소**
   - 엔드포인트: `POST /api/orders/{orderId}/cancel`
   - 권한: @PreAuthorize("hasRole('USER')")
   - 기능:
     - order_status = CANCELLED
     - 모든 상품 재고 원복 (order_item.quantity만큼 증가)
   - 구현 위치:
     - Controller: `OrderController.cancelOrder()`
     - Service: `OrderService.cancelOrder(memberId, orderId)`
     - Mapper: `OrderMapper.updateOrderStatus()`, `ProductMapper.increaseStock()`

**예상 소요**: 3-4일

---

### Phase 4: 위시리스트 및 최근 본 상품
**목표**: 사용자 경험 향상 기능 구현

#### 4.1 위시리스트 (Wishlist) API
**우선순위**: 🟢 중간

**작업 내용**:
1. **위시리스트 조회**
   - 엔드포인트: `GET /api/wishlists`
   - 권한: @PreAuthorize("hasRole('USER')")
   - 기능:
     - 찜한 상품 목록 (페이징, 정렬: latest, priceAsc)
   - 구현 위치:
     - Controller: `WishlistController.getWishlists()`
     - Service: `WishlistService.getWishlists(memberId, page, size, sort)`
     - Mapper: `WishlistMapper.selectWishlistsByMember()`

2. **상품 찜 추가/해제 (토글)**
   - 엔드포인트: `POST /api/wishlists`
   - 권한: @PreAuthorize("hasRole('USER')")
   - 기능:
     - 이미 찜한 상품이면 해제 (DELETE), 아니면 추가 (INSERT)
     - UNIQUE KEY(member_id, product_id) 활용
   - 구현 위치:
     - Controller: `WishlistController.toggleWishlist()`
     - Service: `WishlistService.toggleWishlist(memberId, productId)`
     - Mapper: `WishlistMapper.existsWishlist()`, `insertWishlist()`, `deleteWishlist()`

3. **상품 찜 해제 (명시적 삭제)**
   - 엔드포인트: `DELETE /api/wishlists/{productId}`
   - 권한: @PreAuthorize("hasRole('USER')")
   - 구현 위치:
     - Controller: `WishlistController.deleteWishlist()`
     - Service: `WishlistService.removeWishlist(memberId, productId)`
     - Mapper: `WishlistMapper.deleteWishlist()`

**참고**: 재입고 알림 신청 기능은 Phase 6 (선택 기능)으로 연기

**예상 소요**: 1-2일

---

#### 4.2 최근 본 상품 (Recent View) API
**우선순위**: 🟢 중간

**작업 내용**:
1. **최근 본 상품 기록**
   - 엔드포인트: `POST /api/recent-views`
   - 권한: @PreAuthorize("hasRole('USER')")
   - 기능:
     - 상품 상세 페이지 방문 시 프론트에서 자동 호출
     - 이미 기록된 상품이면 viewed_at만 업데이트 (최신으로 이동)
     - UNIQUE KEY(member_id, product_id) 활용
     - INSERT ON DUPLICATE KEY UPDATE viewed_at = NOW()
   - 구현 위치:
     - Controller: `RecentViewController.recordRecentView()`
     - Service: `RecentViewService.recordView(memberId, productId)`
     - Mapper: `RecentViewMapper.upsertRecentView()`

2. **최근 본 상품 목록 조회**
   - 엔드포인트: `GET /api/recent-views?limit=20`
   - 권한: @PreAuthorize("hasRole('USER')")
   - 기능:
     - viewed_at DESC 정렬 (최근 본 순)
     - limit 제한 (기본 20, 최대 50)
     - 상품 상세 정보(name, price, image 등) 포함
   - 구현 위치:
     - Controller: `RecentViewController.getRecentViews()`
     - Service: `RecentViewService.getRecentViews(memberId, limit)`
     - Mapper: `RecentViewMapper.selectRecentViewsByMember()`

3. **오래된 기록 자동 삭제**
   - 방법 1: 애플리케이션 로직에서 50개 초과 시 가장 오래된 삭제
   - 방법 2: Spring @Scheduled 배치로 주기적으로 삭제
   - 구현 위치:
     - Service: `RecentViewService.cleanupOldViews(memberId)` (조회 시 호출)
     - 또는 Scheduled Task: `@Scheduled(cron = "0 0 2 * * ?")` (매일 새벽 2시)
     - Mapper: `RecentViewMapper.deleteOldestViews(memberId, limit)`

**예상 소요**: 1-2일

---

### Phase 5: 리뷰 및 평점 도메인
**목표**: 상품 리뷰 및 평점 시스템 구현

#### 5.1 리뷰 (Review) API
**우선순위**: 🟢 중간

**작업 내용**:
1. **리뷰 목록 조회 (공개)**
   - 엔드포인트: `GET /api/products/{productId}/reviews`
   - 기능:
     - 비로그인/모든 역할 가능
     - 필터: hasImage=true (사진 있는 리뷰만)
     - 정렬: latest(최신순), ratingDesc(평점 높은 순)
     - is_deleted = 0 조건
     - 페이징
   - 구현 위치:
     - Controller: `ReviewController.getReviews()`
     - Service: `ReviewService.getReviews(productId, hasImage, sort, page, size)`
     - Mapper: `ReviewMapper.selectReviewsByProduct()`

2. **리뷰 작성**
   - 엔드포인트: `POST /api/products/{productId}/reviews`
   - 권한: @PreAuthorize("hasRole('USER')")
   - 기능:
     - orderItemId로 구매 확정 검증 (해당 주문의 상품이어야 함)
     - rating: 1~5 (validation)
     - imageUrls: 최대 5장 (배열, 선택사항)
     - 리뷰 작성 후 상품 평균 평점 재계산 및 업데이트
   - 구현 위치:
     - Controller: `ReviewController.createReview()`
     - Service: `ReviewService.createReview(memberId, productId, ReviewCreateRequest)`
     - Mapper: `ReviewMapper.insertReview()`, `ProductMapper.updateAverageRating()`

3. **리뷰 수정 (본인만)**
   - 엔드포인트: `PATCH /api/reviews/{reviewId}`
   - 권한: @PreAuthorize("hasRole('USER')")
   - 기능:
     - 본인 리뷰만 수정 가능 (member_id 검증)
     - 평균 평점 재계산
   - 구현 위치:
     - Controller: `ReviewController.updateReview()`
     - Service: `ReviewService.updateReview(memberId, reviewId, ReviewUpdateRequest)`
     - Mapper: `ReviewMapper.updateReview()`

4. **리뷰 삭제 (Soft Delete, 본인만)**
   - 엔드포인트: `DELETE /api/reviews/{reviewId}`
   - 권한: @PreAuthorize("hasRole('USER')")
   - 기능:
     - is_deleted = 1
     - 평균 평점 재계산
   - 구현 위치:
     - Controller: `ReviewController.deleteReview()`
     - Service: `ReviewService.softDeleteReview(memberId, reviewId)`
     - Mapper: `ReviewMapper.softDeleteReview()`

**예상 소요**: 2-3일

---

### Phase 6: 배송지 관리 및 ADMIN 기능
**목표**: 부가 기능 및 관리자 기능 구현

#### 6.1 배송지 관리 (Shipping Address) API
**우선순위**: 🟡 높음

**작업 내용**:
1. **배송지 목록 조회**
   - 엔드포인트: `GET /api/members/me/addresses`
   - 권한: @PreAuthorize("hasRole('USER')")
   - 구현 위치:
     - Controller: `ShippingAddressController.getAddresses()`
     - Service: `ShippingAddressService.getAddressesByMember(memberId)`
     - Mapper: `ShippingAddressMapper.selectAddressesByMember()`

2. **배송지 등록**
   - 엔드포인트: `POST /api/members/me/addresses`
   - 권한: @PreAuthorize("hasRole('USER')")
   - 기능:
     - isDefault=true 시 기존 기본 배송지 해제 (is_default = 0)
   - 구현 위치:
     - Controller: `ShippingAddressController.createAddress()`
     - Service: `ShippingAddressService.createAddress(memberId, AddressCreateRequest)`
     - Mapper: `ShippingAddressMapper.insertAddress()`, `unsetDefaultAddress()`

3. **배송지 수정**
   - 엔드포인트: `PATCH /api/members/me/addresses/{addressId}`
   - 권한: @PreAuthorize("hasRole('USER')")
   - 기능:
     - 본인 배송지만 수정 가능
     - isDefault=true 시 기존 기본 배송지 해제
   - 구현 위치:
     - Controller: `ShippingAddressController.updateAddress()`
     - Service: `ShippingAddressService.updateAddress(memberId, addressId, request)`
     - Mapper: `ShippingAddressMapper.updateAddress()`

4. **배송지 삭제**
   - 엔드포인트: `DELETE /api/members/me/addresses/{addressId}`
   - 권한: @PreAuthorize("hasRole('USER')")
   - 구현 위치:
     - Controller: `ShippingAddressController.deleteAddress()`
     - Service: `ShippingAddressService.deleteAddress(memberId, addressId)`
     - Mapper: `ShippingAddressMapper.deleteAddress()`

5. **기본 배송지 설정**
   - 엔드포인트: `PATCH /api/members/me/addresses/{addressId}/default`
   - 권한: @PreAuthorize("hasRole('USER')")
   - 기능:
     - 기존 기본 배송지 자동 해제
   - 구현 위치:
     - Controller: `ShippingAddressController.setDefaultAddress()`
     - Service: `ShippingAddressService.setDefaultAddress(memberId, addressId)`
     - Mapper: `ShippingAddressMapper.unsetDefaultAddress()`, `setDefaultAddress()`

**예상 소요**: 1-2일

---

#### 6.2 ADMIN 전용 API
**우선순위**: 🔵 낮음

**작업 내용**:
1. **상품 강제 삭제**
   - 엔드포인트: `DELETE /api/admin/products/{productId}`
   - 권한: @PreAuthorize("hasRole('ADMIN')")
   - 기능:
     - 모든 상품 강제 삭제 (Hard Delete 또는 특수 처리)
   - 구현 위치:
     - Controller: `AdminProductController.forceDeleteProduct()`
     - Service: `ProductService.forceDeleteProduct(productId)`
     - Mapper: `ProductMapper.hardDeleteProduct()`

2. **부적절 리뷰 강제 삭제**
   - 엔드포인트: `DELETE /api/admin/reviews/{reviewId}`
   - 권한: @PreAuthorize("hasRole('ADMIN')")
   - 기능:
     - 신고된 부적절 리뷰 강제 삭제
   - 구현 위치:
     - Controller: `AdminReviewController.forceDeleteReview()`
     - Service: `ReviewService.forceDeleteReview(reviewId)`
     - Mapper: `ReviewMapper.hardDeleteReview()`

**예상 소요**: 0.5-1일

---

## 🔧 기술적 고려사항

### 1. 동시성 제어
**문제**: 재고 차감 시 동시 요청으로 인한 과다 차감

**해결 방안**:
- **비관적 락 (Pessimistic Lock)**: MyBatis에서 `SELECT ... FOR UPDATE` 사용
  ```sql
  SELECT stock_quantity FROM product WHERE product_id = #{productId} FOR UPDATE
  ```
- **낙관적 락 (Optimistic Lock)**: version 컬럼 추가 및 UPDATE 시 version 체크
- **추천**: 비관적 락 (간단하고 확실함)

---

### 2. 역할 기반 접근 제어 (RBAC)
**구현 방법**:
1. **Member Entity에 role 필드 추가** (ENUM: USER, SELLER, ADMIN)
2. **JWT 토큰에 role claim 포함**:
   ```java
   // JwtUtil.createAccessToken()
   .claim("role", member.getRole().name())
   ```
3. **SecurityConfig에서 역할 기반 필터 설정**:
   ```java
   .requestMatchers("/api/seller/**").hasRole("SELLER")
   .requestMatchers("/api/admin/**").hasRole("ADMIN")
   ```
4. **컨트롤러 메서드에 @PreAuthorize 적용**:
   ```java
   @PreAuthorize("hasRole('SELLER')")
   public ResponseEntity<?> createProduct(...) { }
   ```

---

### 3. Soft Delete 패턴
**구현 방법**:
- `deleted_at` 컬럼 추가 (TIMESTAMP, nullable)
- 조회 시 `WHERE deleted_at IS NULL` 조건 추가
- 삭제 시 `UPDATE table SET deleted_at = NOW() WHERE id = ?`

---

### 4. 재고 0 시 자동 품절 처리
**구현 방법**:
- **애플리케이션 로직**: 재고 차감 후 `if (stock_quantity == 0) { updateStatus(SOLD_OUT) }`
- **데이터베이스 트리거**: MySQL 트리거로 stock_quantity = 0 시 product_status = 'SOLD_OUT' 자동 변경
- **추천**: 애플리케이션 로직 (명시적이고 제어 가능)

---

### 5. 평균 평점 계산
**구현 방법**:
- **리뷰 작성/수정/삭제 시 평균 평점 재계산**:
  ```sql
  UPDATE product
  SET average_rating = (
    SELECT AVG(rating) FROM review
    WHERE product_id = #{productId} AND is_deleted = 0
  )
  WHERE product_id = #{productId}
  ```
- **또는**: 집계 테이블 사용 (product_stats: review_count, total_rating)

---

### 6. 이미지 업로드 (AWS S3)
**구현 방법**:
- **S3Util.uploadImage()** 메서드 활용
- 리뷰 작성 시 프론트에서 이미지 업로드 → S3 URL 반환 → 리뷰 저장 시 URL 배열 저장
- image_urls: JSON 타입 또는 TEXT 타입 (쉼표 구분)

---

### 7. 최근 본 상품 자동 정리
**구현 방법**:
- **조회 시 정리**: `RecentViewService.getRecentViews()` 내부에서 50개 초과 시 오래된 기록 삭제
- **배치 작업**: `@Scheduled(cron = "0 0 2 * * ?")` 매일 새벽 2시 실행
  ```java
  @Scheduled(cron = "0 0 2 * * ?")
  public void cleanupOldRecentViews() {
    recentViewMapper.deleteOldestViewsForAllMembers(50);
  }
  ```
- **추천**: 조회 시 정리 (간단하고 즉각적)

---

## 🧪 테스트 전략

### 1. 단위 테스트 (Unit Test)
- **Service 레이어 테스트**:
  - Mockito로 Mapper 모킹
  - 비즈니스 로직 검증 (재고 체크, 권한 검증, 중복 체크 등)
- **예시**:
  ```java
  @ExtendWith(MockitoExtension.class)
  class MemberServiceTest {
    @Mock
    private MemberMapper memberMapper;

    @InjectMocks
    private MemberService memberService;

    @Test
    void testRegisterWithDuplicateUsername() {
      when(memberMapper.existsByUsername(any())).thenReturn(true);
      assertThrows(BusinessException.class, () -> {
        memberService.register(new RegisterRequest(...));
      });
    }
  }
  ```

---

### 2. 통합 테스트 (Integration Test)
- **@SpringBootTest + @AutoConfigureMockMvc**:
  - 실제 데이터베이스 연동 (H2 또는 Testcontainers MySQL)
  - API 엔드포인트 테스트
- **예시**:
  ```java
  @SpringBootTest
  @AutoConfigureMockMvc
  class MemberControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void testRegisterSuccess() throws Exception {
      mockMvc.perform(post("/api/members/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"username\":\"test@example.com\", ...}"))
        .andExpect(status().isOk());
    }
  }
  ```

---

### 3. MyBatis Mapper 테스트
- **@MybatisTest**:
  - Mapper 인터페이스 및 XML 쿼리 테스트
- **예시**:
  ```java
  @MybatisTest
  class MemberMapperTest {
    @Autowired
    private MemberMapper memberMapper;

    @Test
    void testInsertMember() {
      Member member = new Member(...);
      memberMapper.insertMember(member);
      assertNotNull(member.getId());
    }
  }
  ```

---

### 4. Security 테스트
- **@WithMockUser**:
  - 역할 기반 접근 제어 테스트
- **예시**:
  ```java
  @Test
  @WithMockUser(roles = "SELLER")
  void testSellerCanCreateProduct() throws Exception {
    mockMvc.perform(post("/api/seller/products")
      .contentType(MediaType.APPLICATION_JSON)
      .content("{...}"))
      .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(roles = "USER")
  void testUserCannotCreateProduct() throws Exception {
    mockMvc.perform(post("/api/seller/products")
      .contentType(MediaType.APPLICATION_JSON)
      .content("{...}"))
      .andExpect(status().isForbidden());
  }
  ```

---

## 📅 전체 일정 요약

| Phase | 작업 내용 | 우선순위 | 상태 | 예상 소요 |
|-------|----------|---------|------|----------|
| Phase 1.1 | 데이터베이스 스키마 마이그레이션 | 🔴 최우선 | ✅ 설계 완료 | 0.5일 (Flyway 파일만) |
| Phase 1.2 | 회원가입 및 인증 시스템 완성 | 🔴 최우선 | ⏳ 대기 중 | 2-3일 |
| Phase 2.1 | 공개 상품 API (목록/검색/상세) | 🟡 높음 | 2-3일 |
| Phase 2.2 | SELLER 전용 상품 관리 API | 🟡 높음 | 2-3일 |
| Phase 3.1 | 장바구니 API | 🟡 높음 | 2-3일 |
| Phase 3.2 | 주문 API | 🟡 높음 | 3-4일 |
| Phase 4.1 | 위시리스트 API | 🟢 중간 | 1-2일 |
| Phase 4.2 | 최근 본 상품 API | 🟢 중간 | 1-2일 |
| Phase 5.1 | 리뷰 및 평점 API | 🟢 중간 | 2-3일 |
| Phase 6.1 | 배송지 관리 API | 🟡 높음 | 1-2일 |
| Phase 6.2 | ADMIN 전용 API | 🔵 낮음 | 0.5-1일 |
| **전체 예상** | | | | **19-29일** |

---

## 🎯 다음 단계 (즉시 시작 가능)

### 1. Flyway 마이그레이션 파일 생성 (Phase 1.1 완료)
```bash
# 디렉토리 생성
mkdir -p src/main/resources/db/migration

# 마이그레이션 파일 생성
# .claude/db.sql 내용을 아래 파일로 복사
touch src/main/resources/db/migration/V1__init_schema.sql
```

**작업 순서**:
1. `.claude/db.sql` 파일 내용을 복사
2. `src/main/resources/db/migration/V1__init_schema.sql` 파일에 붙여넣기
3. 애플리케이션 실행 시 Flyway가 자동으로 마이그레이션 실행

### 2. Phase 1.2 구현 시작 (회원가입 및 인증)
**우선순위 작업**:
1. ✅ **Entity 작성**: `Member.java` (id, username, password, nickname, role, createdAt, updatedAt)
2. ✅ **DTO 작성**: `RegisterRequest.java`, `LoginRequest.java`, `LoginResponse.java`
3. ✅ **Mapper 인터페이스**: `MemberMapper.java` (insertMember, existsByUsername, existsByNickname, findByUsername)
4. ✅ **Mapper XML**: `MemberMapper.xml` (SQL 쿼리 작성)
5. ✅ **Service 구현**: `MemberService.register()`, `authenticate()`
6. ✅ **Controller 구현**: `MemberController.register()`, `AuthController.login()`
7. ✅ **JwtUtil 수정**: role claim 추가
8. ✅ **SecurityConfig 수정**: RBAC 설정

### 3. 지속적 테스트 및 검증
- 각 Phase 완료 시마다 단위 테스트 및 통합 테스트 작성
- Swagger UI에서 API 동작 확인
- Postman 또는 curl로 엔드포인트 테스트

### 4. 문서 업데이트
- API 구현 완료 시 Swagger 문서 확인 및 업데이트
- 각 Phase 완료 후 implementation-plan.md 상태 업데이트

---

## 📝 참고사항

- **코드 스타일**: CLAUDE.md 참조 (기존 패키지 구조 및 네이밍 컨벤션 준수)
- **에러 코드 정의**: 각 도메인별 ErrorCode enum 추가 (예: ProductError, OrderError)
- **Validation**: @Valid 및 커스텀 Validation 적극 활용
- **로깅**: SLF4J 로거로 중요 비즈니스 로직 로깅
- **트랜잭션**: @Transactional 적절히 적용 (특히 주문 생성, 결제 처리, 재고 차감)

---

**작성자**: Claude Code
**최종 수정일**: 2026-01-13