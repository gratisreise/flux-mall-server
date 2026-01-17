# DDD Package Structure Refactoring Design

## 📊 Current State Analysis (Layered Architecture)

```
com.fluxmall/
├── config/          # 설정 (SecurityConfig, WebConfig)
├── controller/      # 모든 컨트롤러 (9개)
├── service/         # 모든 서비스 (9개)
│   └── auth/        # 인증 서비스
├── mapper/          # 모든 MyBatis 매퍼 (8개)
├── domain/
│   ├── dto/         # 모든 DTO (3개)
│   └── entity/      # 모든 엔티티 (10개)
├── filter/          # JWT 필터
├── exception/       # 글로벌 예외 처리
├── common/          # 공통 유틸, 응답
├── utils/           # 유틸리티 (JWT, S3, Redis)
└── annotations/     # 커스텀 어노테이션
```

### 현재 구조의 문제점

| 문제 | 설명 |
|------|------|
| **낮은 응집도** | 기술적 관심사로 분리되어 비즈니스 도메인이 흩어짐 |
| **높은 결합도** | 도메인 간 경계가 불명확하여 의존성 관리 어려움 |
| **확장성 제한** | 새 기능 추가 시 여러 패키지에 파일 분산 |
| **가독성 저하** | 특정 도메인 전체 파악을 위해 여러 패키지 탐색 필요 |
| **테스트 어려움** | 도메인 단위 테스트 구조화 어려움 |

---

## 🎯 Target State (DDD Package-by-Feature Architecture)

```
com.fluxmall/
│
├── FluxMallServerApplication.java
│
├── global/                           # 🌐 전역 인프라 (공유)
│   ├── config/
│   │   ├── SecurityConfig.java
│   │   └── WebConfig.java
│   ├── filter/
│   │   ├── JwtAuthenticationFilter.java
│   │   └── ExceptionHandlerFilter.java
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java
│   │   ├── BusinessException.java
│   │   └── ErrorCode.java
│   ├── response/
│   │   ├── CommonResult.java
│   │   ├── SingleResult.java
│   │   ├── ListResult.java
│   │   └── ResponseService.java
│   ├── util/
│   │   ├── JwtUtil.java
│   │   ├── S3Util.java
│   │   └── RedisUtil.java
│   ├── annotation/
│   │   ├── Password.java
│   │   └── PasswordValidator.java
│   └── constant/
│       └── CommonValue.java
│
├── member/                           # 👤 회원 도메인 (Bounded Context)
│   ├── domain/
│   │   ├── Member.java               # Entity (Aggregate Root)
│   │   └── MemberRole.java           # Enum
│   ├── dto/
│   │   ├── request/
│   │   │   ├── RegisterRequest.java
│   │   │   ├── LoginRequest.java
│   │   │   └── UpdateProfileRequest.java
│   │   └── response/
│   │       ├── MemberResponse.java
│   │       └── LoginResponse.java
│   ├── repository/
│   │   └── MemberMapper.java
│   ├── service/
│   │   └── MemberService.java
│   ├── controller/
│   │   ├── MemberController.java
│   │   └── AuthController.java
│   └── exception/
│       └── MemberError.java          # 도메인 특화 에러코드
│
├── member/address/                   # 📍 배송지 (Member의 서브 도메인)
│   ├── domain/
│   │   └── ShippingAddress.java
│   ├── dto/
│   │   ├── request/
│   │   │   └── AddressCreateRequest.java
│   │   └── response/
│   │       └── AddressResponse.java
│   ├── repository/
│   │   └── ShippingAddressMapper.java
│   ├── service/
│   │   └── ShippingAddressService.java
│   └── controller/
│       └── ShippingAddressController.java
│
├── auth/                             # 🔐 인증 도메인
│   ├── service/
│   │   └── TokenBlacklistService.java
│   └── exception/
│       └── AuthError.java
│
├── product/                          # 📦 상품 도메인 (Bounded Context)
│   ├── domain/
│   │   ├── Product.java              # Entity (Aggregate Root)
│   │   └── ProductStatus.java        # Enum
│   ├── dto/
│   │   ├── request/
│   │   │   ├── ProductCreateRequest.java
│   │   │   ├── ProductUpdateRequest.java
│   │   │   └── ProductSearchRequest.java
│   │   └── response/
│   │       ├── ProductResponse.java
│   │       └── ProductDetailResponse.java
│   ├── repository/
│   │   └── ProductMapper.java
│   ├── service/
│   │   └── ProductService.java
│   ├── controller/
│   │   ├── ProductController.java        # 공개 API
│   │   └── SellerProductController.java  # SELLER 전용
│   └── exception/
│       └── ProductError.java
│
├── cart/                             # 🛒 장바구니 도메인 (Bounded Context)
│   ├── domain/
│   │   ├── Cart.java                 # Entity (Aggregate Root)
│   │   └── CartItem.java             # Entity (Aggregate 내부)
│   ├── dto/
│   │   ├── request/
│   │   │   ├── AddCartItemRequest.java
│   │   │   └── UpdateCartItemRequest.java
│   │   └── response/
│   │       ├── CartResponse.java
│   │       └── CartItemResponse.java
│   ├── repository/
│   │   └── CartMapper.java
│   ├── service/
│   │   └── CartService.java
│   ├── controller/
│   │   └── CartController.java
│   └── exception/
│       └── CartError.java
│
├── order/                            # 📋 주문 도메인 (Bounded Context)
│   ├── domain/
│   │   ├── Order.java                # Entity (Aggregate Root)
│   │   ├── OrderItem.java            # Entity (Aggregate 내부)
│   │   └── OrderStatus.java          # Enum
│   ├── dto/
│   │   ├── request/
│   │   │   ├── OrderCreateRequest.java
│   │   │   └── OrderItemRequest.java
│   │   └── response/
│   │       ├── OrderResponse.java
│   │       └── OrderDetailResponse.java
│   ├── repository/
│   │   └── OrderMapper.java
│   ├── service/
│   │   └── OrderService.java
│   ├── controller/
│   │   └── OrderController.java
│   └── exception/
│       └── OrderError.java
│
├── wishlist/                         # ❤️ 위시리스트 도메인
│   ├── domain/
│   │   └── Wishlist.java
│   ├── dto/
│   │   ├── request/
│   │   │   └── WishlistToggleRequest.java
│   │   └── response/
│   │       └── WishlistResponse.java
│   ├── repository/
│   │   └── WishlistMapper.java
│   ├── service/
│   │   └── WishlistService.java
│   ├── controller/
│   │   └── WishlistController.java
│   └── exception/
│       └── WishlistError.java
│
├── recentview/                       # 👁️ 최근 본 상품 도메인
│   ├── domain/
│   │   └── RecentView.java
│   ├── dto/
│   │   └── response/
│   │       └── RecentViewResponse.java
│   ├── repository/
│   │   └── RecentViewMapper.java
│   ├── service/
│   │   └── RecentViewService.java
│   └── controller/
│       └── RecentViewController.java
│
├── review/                           # ⭐ 리뷰 도메인 (Bounded Context)
│   ├── domain/
│   │   └── Review.java
│   ├── dto/
│   │   ├── request/
│   │   │   ├── ReviewCreateRequest.java
│   │   │   └── ReviewUpdateRequest.java
│   │   └── response/
│   │       └── ReviewResponse.java
│   ├── repository/
│   │   └── ReviewMapper.java
│   ├── service/
│   │   └── ReviewService.java
│   ├── controller/
│   │   ├── ReviewController.java       # 공개 + USER API
│   │   └── AdminReviewController.java  # ADMIN 전용
│   └── exception/
│       └── ReviewError.java
│
└── admin/                            # 🔧 관리자 도메인
    └── controller/
        └── AdminProductController.java
```

---

## 📐 DDD 패턴 적용

### 1. Bounded Context (경계 컨텍스트)

| Context | Aggregate Root | 관련 Entity | 책임 |
|---------|---------------|-------------|------|
| **Member** | Member | ShippingAddress | 회원 계정, 배송지 관리 |
| **Product** | Product | - | 상품 카탈로그, 재고 관리 |
| **Cart** | Cart | CartItem | 장바구니 관리 |
| **Order** | Order | OrderItem | 주문 생성, 결제, 취소 |
| **Review** | Review | - | 리뷰/평점 관리 |
| **Wishlist** | Wishlist | - | 찜 관리 |
| **RecentView** | RecentView | - | 최근 본 상품 추적 |

### 2. Aggregate 규칙

```java
// ✅ Good: Aggregate Root를 통한 접근
Order order = orderService.findById(orderId);
order.addItem(product, quantity);  // Order가 OrderItem 관리

// ❌ Bad: Aggregate 내부 직접 접근
OrderItem item = orderItemRepository.findById(itemId);  // 금지
```

### 3. 도메인 간 의존성

```
┌─────────────────────────────────────────────────────────────┐
│                       global (공유)                          │
└─────────────────────────────────────────────────────────────┘
                              ↑
    ┌─────────┬─────────┬─────┴───┬─────────┬─────────┐
    │         │         │         │         │         │
 member    product    cart     order    review   wishlist
    │         │         │         │         │
    │         ↓         ↓         ↓         ↓
    │      product ← cart → order → review
    │         ↑                     │
    └─────────┴─────────────────────┘
              (member_id 참조)
```

**의존성 규칙**:
- `global` → 어디서든 참조 가능
- 도메인 간 직접 Entity 참조 금지 → ID로만 참조
- 도메인 간 통신 → Application Service 또는 Domain Event

---

## 🔄 Migration Mapping (현재 → DDD)

### Controllers

| 현재 위치 | DDD 위치 |
|-----------|----------|
| `controller/MemberController.java` | `member/controller/MemberController.java` |
| `controller/AuthController.java` | `member/controller/AuthController.java` |
| `controller/ProductController.java` | `product/controller/ProductController.java` |
| `controller/CartController.java` | `cart/controller/CartController.java` |
| `controller/OrderController.java` | `order/controller/OrderController.java` |
| `controller/ReviewController.java` | `review/controller/ReviewController.java` |
| `controller/WishlistController.java` | `wishlist/controller/WishlistController.java` |
| `controller/RecentViewController.java` | `recentview/controller/RecentViewController.java` |
| `controller/ShippingAddressController.java` | `member/address/controller/ShippingAddressController.java` |

### Services

| 현재 위치 | DDD 위치 |
|-----------|----------|
| `service/MemberService.java` | `member/service/MemberService.java` |
| `service/auth/TokenBlacklistService.java` | `auth/service/TokenBlacklistService.java` |
| `service/ProductService.java` | `product/service/ProductService.java` |
| `service/CartService.java` | `cart/service/CartService.java` |
| `service/OrderService.java` | `order/service/OrderService.java` |
| `service/ReviewService.java` | `review/service/ReviewService.java` |
| `service/WishlistService.java` | `wishlist/service/WishlistService.java` |
| `service/RecentViewService.java` | `recentview/service/RecentViewService.java` |
| `service/ShippingAddressService.java` | `member/address/service/ShippingAddressService.java` |

### Entities

| 현재 위치 | DDD 위치 |
|-----------|----------|
| `domain/entity/Member.java` | `member/domain/Member.java` |
| `domain/entity/Product.java` | `product/domain/Product.java` |
| `domain/entity/Cart.java` | `cart/domain/Cart.java` |
| `domain/entity/CartItem.java` | `cart/domain/CartItem.java` |
| `domain/entity/Order.java` | `order/domain/Order.java` |
| `domain/entity/OrderItem.java` | `order/domain/OrderItem.java` |
| `domain/entity/Review.java` | `review/domain/Review.java` |
| `domain/entity/Wishlist.java` | `wishlist/domain/Wishlist.java` |
| `domain/entity/RecentView.java` | `recentview/domain/RecentView.java` |
| `domain/entity/ShippingAddress.java` | `member/address/domain/ShippingAddress.java` |

### DTOs

| 현재 위치 | DDD 위치 |
|-----------|----------|
| `domain/dto/RegisterRequest.java` | `member/dto/request/RegisterRequest.java` |
| `domain/dto/LoginRequest.java` | `member/dto/request/LoginRequest.java` |
| `domain/dto/LoginResponse.java` | `member/dto/response/LoginResponse.java` |

### Mappers

| 현재 위치 | DDD 위치 |
|-----------|----------|
| `mapper/MemberMapper.java` | `member/repository/MemberMapper.java` |
| `mapper/ProductMapper.java` | `product/repository/ProductMapper.java` |
| `mapper/CartMapper.java` | `cart/repository/CartMapper.java` |
| `mapper/OrderMapper.java` | `order/repository/OrderMapper.java` |
| `mapper/ReviewMapper.java` | `review/repository/ReviewMapper.java` |
| `mapper/WishlistMapper.java` | `wishlist/repository/WishlistMapper.java` |
| `mapper/RecentViewMapper.java` | `recentview/repository/RecentViewMapper.java` |
| `mapper/ShippingAddressMapper.java` | `member/address/repository/ShippingAddressMapper.java` |

### Global (공유)

| 현재 위치 | DDD 위치 |
|-----------|----------|
| `config/*` | `global/config/*` |
| `filter/*` | `global/filter/*` |
| `exception/GlobalExceptionHandler.java` | `global/exception/GlobalExceptionHandler.java` |
| `exception/BusinessException.java` | `global/exception/BusinessException.java` |
| `exception/errors/ErrorCode.java` | `global/exception/ErrorCode.java` |
| `exception/errors/AuthError.java` | `auth/exception/AuthError.java` |
| `common/response/*` | `global/response/*` |
| `common/CommonValue.java` | `global/constant/CommonValue.java` |
| `utils/*` | `global/util/*` |
| `annotations/*` | `global/annotation/*` |

---

## 📁 MyBatis Mapper XML Migration

```
src/main/resources/mappers/
├── member/
│   ├── MemberMapper.xml
│   └── ShippingAddressMapper.xml
├── product/
│   └── ProductMapper.xml
├── cart/
│   └── CartMapper.xml
├── order/
│   └── OrderMapper.xml
├── review/
│   └── ReviewMapper.xml
├── wishlist/
│   └── WishlistMapper.xml
└── recentview/
    └── RecentViewMapper.xml
```

**application.yml 설정 변경**:
```yaml
mybatis:
  mapper-locations: classpath:mappers/**/*.xml
```

---

## ✅ Refactoring Benefits

| 측면 | Before (Layered) | After (DDD) |
|------|------------------|-------------|
| **응집도** | 낮음 (기술 기준 분리) | 높음 (도메인 기준 분리) |
| **결합도** | 높음 (도메인 경계 불명확) | 낮음 (명확한 Bounded Context) |
| **가독성** | 낮음 (여러 패키지 탐색) | 높음 (도메인 한 곳에 집중) |
| **확장성** | 제한적 | 도메인별 독립 확장 가능 |
| **테스트** | 어려움 | 도메인 단위 테스트 용이 |
| **팀 협업** | 충돌 가능성 높음 | 도메인별 책임 명확 |
| **MSA 전환** | 어려움 | Bounded Context 단위 분리 용이 |

---

## 🚀 Implementation Steps

### Phase 1: 기반 작업
1. `global/` 패키지 생성 및 공유 클래스 이동
2. MyBatis 설정 업데이트 (`mapper-locations`)
3. 빌드 및 테스트 확인

### Phase 2: 도메인별 이동
1. `member/` 도메인 생성 (Entity, DTO, Mapper, Service, Controller)
2. `product/` 도메인 생성
3. `cart/` 도메인 생성
4. `order/` 도메인 생성
5. `review/` 도메인 생성
6. `wishlist/`, `recentview/` 도메인 생성

### Phase 3: 정리
1. 기존 빈 패키지 삭제
2. import 문 정리
3. 전체 테스트 실행
4. 문서 업데이트

---

## ⚠️ 주의사항

1. **패키지 변경 시 import 전체 수정 필요**
2. **MyBatis namespace 변경** (Mapper XML의 namespace 속성)
3. **Component Scan 경로 확인** (기본 `com.fluxmall` 하위 자동 스캔)
4. **IDE Refactor 기능 활용 권장** (IntelliJ: Refactor → Move)

---

**Last Updated**: 2026-01-15
**Author**: Claude Code (System Architect Mode)
