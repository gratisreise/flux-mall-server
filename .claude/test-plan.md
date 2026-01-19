# FluxMall Server - Test Plan

## Overview

테스트 컨벤션 기준에 따라 각 서비스의 메서드를 분석하고 테스트 대상을 선정합니다.

---

## 테스트 대상 분석 요약

| 서비스 | 전체 메서드 | ✅ 필수 | ⚠️ 선택 | ❌ 불필요 |
|--------|------------|---------|---------|----------|
| AuthService | 3 | 3 | 0 | 0 |
| MemberService | 6 | 4 | 1 | 1 |
| ProductService | 11 | 6 | 3 | 2 |
| CartService | 5 | 4 | 1 | 0 |
| OrderService | 5 | 5 | 0 | 0 |
| WishlistService | 4 | 2 | 2 | 0 |
| RecentViewService | 2 | 1 | 1 | 0 |
| ReviewService | 5 | 4 | 1 | 0 |
| ShippingAddressService | 5 | 4 | 1 | 0 |
| **합계** | **46** | **33** | **10** | **3** |

---

## Phase 1: 핵심 비즈니스 로직 (우선순위 1)

> 결제, 주문, 재고, 인증 관련 - **반드시 테스트**

### 1.1 OrderService (5개 메서드 - 전체 필수)

| 메서드 | 판단 | 이유 | 테스트 케이스 |
|--------|------|------|--------------|
| `createOrder` | ✅ 필수 | 재고 검증, 금액 계산, 다중 엔티티 생성 | 성공, 상품없음, 재고부족 |
| `payOrder` | ✅ 필수 | 상태 전이, 재고 차감, 동시성 제어 | 성공, 주문없음, 권한없음, 상태오류 |
| `cancelOrder` | ✅ 필수 | 상태 전이, 재고 원복, 조건 분기 | 성공, 주문없음, 권한없음, 취소불가상태 |
| `findById` | ✅ 필수 | 권한 검증 분기 | 성공, 주문없음, 권한없음 |
| `findAllByMemberId` | ⚠️ 선택 | 단순 조회 | (선택적) |

```java
@Nested
class OrderServiceTest {
    // createOrder: 3 tests
    // payOrder: 4 tests
    // cancelOrder: 4 tests
    // findById: 3 tests
    // Total: 14 tests
}
```

### 1.2 AuthService (3개 메서드 - 전체 필수)

| 메서드 | 판단 | 이유 | 테스트 케이스 |
|--------|------|------|--------------|
| `login` | ✅ 필수 | 인증 로직, 토큰 발급 | 성공, 사용자없음, 비밀번호불일치 |
| `logout` | ✅ 필수 | 토큰 블랙리스트 처리 | 성공, 유효하지않은토큰 |
| `refresh` | ✅ 필수 | 토큰 갱신, 블랙리스트 검증 | 성공, 만료토큰, 블랙리스트토큰 |

```java
@Nested
class AuthServiceTest {
    // login: 3 tests
    // logout: 2 tests
    // refresh: 3 tests
    // Total: 8 tests
}
```

### 1.3 CartService (5개 메서드 - 4개 필수)

| 메서드 | 판단 | 이유 | 테스트 케이스 |
|--------|------|------|--------------|
| `addItem` | ✅ 필수 | 재고 검증, 수량 합산 로직 | 성공, 상품없음, 재고초과, 수량합산 |
| `updateItemQuantity` | ✅ 필수 | 권한 검증, 재고 검증 | 성공, 아이템없음, 권한없음, 재고초과 |
| `removeItem` | ✅ 필수 | 권한 검증 | 성공, 아이템없음, 권한없음 |
| `removeItems` | ✅ 필수 | 다중 삭제, 권한 검증 | 성공, 일부없음 |
| `getCart` | ⚠️ 선택 | 단순 조회 + 총액 계산 | (선택적) |

```java
@Nested
class CartServiceTest {
    // addItem: 4 tests
    // updateItemQuantity: 4 tests
    // removeItem: 3 tests
    // removeItems: 2 tests
    // Total: 13 tests
}
```

---

## Phase 2: CRUD 비즈니스 로직 (우선순위 2)

> 상품, 리뷰, 배송지 등 일반 CRUD + 비즈니스 규칙

### 2.1 ProductService (11개 메서드 - 6개 필수)

| 메서드 | 판단 | 이유 | 테스트 케이스 |
|--------|------|------|--------------|
| `createProduct` | ✅ 필수 | 상품 등록 로직 | 성공 |
| `updateProduct` | ✅ 필수 | 권한 검증, 상태 검증, 재고→품절 처리 | 성공, 상품없음, 권한없음, 이미삭제됨, 재고0품절 |
| `updateProductStatus` | ✅ 필수 | 권한 검증, 상태 전이 | 성공, 상품없음, 권한없음, 이미삭제됨 |
| `deleteProduct` | ✅ 필수 | 권한 검증, Soft Delete | 성공, 상품없음, 권한없음, 이미삭제됨 |
| `forceDeleteProduct` | ✅ 필수 | 존재 검증 | 성공, 상품없음 |
| `getProductDetail` | ✅ 필수 | null 체크 예외 | 성공, 상품없음 |
| `getProducts` | ❌ 불필요 | 단순 위임 | - |
| `getProductCount` | ❌ 불필요 | 단순 위임 | - |
| `searchProducts` | ⚠️ 선택 | 단순 조회 | (선택적) |
| `getSearchCount` | ⚠️ 선택 | 단순 위임 | (선택적) |
| `getMyProducts` | ⚠️ 선택 | 단순 조회 | (선택적) |

```java
@Nested
class ProductServiceTest {
    // createProduct: 1 test
    // updateProduct: 5 tests
    // updateProductStatus: 4 tests
    // deleteProduct: 4 tests
    // forceDeleteProduct: 2 tests
    // getProductDetail: 2 tests
    // Total: 18 tests
}
```

### 2.2 ReviewService (5개 메서드 - 4개 필수)

| 메서드 | 판단 | 이유 | 테스트 케이스 |
|--------|------|------|--------------|
| `createReview` | ✅ 필수 | 구매 검증, 중복 검증, 배송완료 검증 | 성공, 상품없음, 주문없음, 배송미완료, 이미작성함 |
| `updateReview` | ✅ 필수 | 권한 검증, 삭제 여부 검증 | 성공, 리뷰없음, 권한없음 |
| `deleteReview` | ✅ 필수 | 권한 검증, Soft Delete | 성공, 리뷰없음, 권한없음 |
| `forceDeleteReview` | ✅ 필수 | 존재 검증 | 성공, 리뷰없음 |
| `getReviewsByProductId` | ⚠️ 선택 | 상품 존재 검증 | (선택적) |

```java
@Nested
class ReviewServiceTest {
    // createReview: 5 tests
    // updateReview: 3 tests
    // deleteReview: 3 tests
    // forceDeleteReview: 2 tests
    // Total: 13 tests
}
```

### 2.3 ShippingAddressService (5개 메서드 - 4개 필수)

| 메서드 | 판단 | 이유 | 테스트 케이스 |
|--------|------|------|--------------|
| `createAddress` | ✅ 필수 | 최대 개수 제한, 첫 배송지 기본 설정 | 성공, 최대초과, 첫배송지자동기본 |
| `updateAddress` | ✅ 필수 | 권한 검증 | 성공, 배송지없음, 권한없음 |
| `deleteAddress` | ✅ 필수 | 권한 검증, 기본배송지 재설정 | 성공, 배송지없음, 권한없음, 기본배송지삭제시재설정 |
| `setDefaultAddress` | ✅ 필수 | 권한 검증, 기존 기본 해제 | 성공, 배송지없음, 권한없음 |
| `getAddresses` | ⚠️ 선택 | 단순 조회 | (선택적) |

```java
@Nested
class ShippingAddressServiceTest {
    // createAddress: 3 tests
    // updateAddress: 3 tests
    // deleteAddress: 4 tests
    // setDefaultAddress: 3 tests
    // Total: 13 tests
}
```

### 2.4 MemberService (6개 메서드 - 4개 필수)

| 메서드 | 판단 | 이유 | 테스트 케이스 |
|--------|------|------|--------------|
| `register` | ✅ 필수 | 중복 검증 (username, nickname) | 성공, 아이디중복, 닉네임중복 |
| `getMyInfo` | ✅ 필수 | 존재 검증 | 성공, 회원없음 |
| `updateProfile` | ✅ 필수 | 존재 검증, 닉네임 중복 검증 | 성공, 회원없음, 닉네임중복 |
| `changePassword` | ✅ 필수 | 존재 검증 | 성공, 회원없음 |
| `findById` | ❌ 불필요 | 단순 위임 | - |
| `findByUsername` | ⚠️ 선택 | 단순 조회 | (선택적) |

```java
@Nested
class MemberServiceTest {
    // register: 3 tests
    // getMyInfo: 2 tests
    // updateProfile: 3 tests
    // changePassword: 2 tests
    // Total: 10 tests
}
```

---

## Phase 3: 부가 기능 (우선순위 3)

> 위시리스트, 최근 본 상품 등 부가 기능

### 3.1 WishlistService (4개 메서드 - 2개 필수)

| 메서드 | 판단 | 이유 | 테스트 케이스 |
|--------|------|------|--------------|
| `toggleWishlist` | ✅ 필수 | 토글 로직, 상품 검증 | 추가성공, 제거성공, 상품없음 |
| `removeWishlist` | ✅ 필수 | 존재 검증 | 성공, 위시리스트없음 |
| `getWishlists` | ⚠️ 선택 | 단순 조회 | (선택적) |
| `isWishlisted` | ⚠️ 선택 | 단순 조회 | (선택적) |

```java
@Nested
class WishlistServiceTest {
    // toggleWishlist: 3 tests
    // removeWishlist: 2 tests
    // Total: 5 tests
}
```

### 3.2 RecentViewService (2개 메서드 - 1개 필수)

| 메서드 | 판단 | 이유 | 테스트 케이스 |
|--------|------|------|--------------|
| `recordView` | ✅ 필수 | UPSERT 로직, 자동 정리 | 신규기록, 기존업데이트, 50개초과시정리 |
| `getRecentViews` | ⚠️ 선택 | 단순 조회 | (선택적) |

```java
@Nested
class RecentViewServiceTest {
    // recordView: 3 tests
    // Total: 3 tests
}
```

---

## 테스트 구현 계획

### Phase 1: 핵심 비즈니스 로직 (예상 35 tests)

```
├── OrderServiceTest.java        (14 tests)
├── AuthServiceTest.java         (8 tests)
└── CartServiceTest.java         (13 tests)
```

**커밋 단위:**
1. `test(order): OrderService 단위 테스트 구현`
2. `test(auth): AuthService 단위 테스트 구현`
3. `test(cart): CartService 단위 테스트 구현`

### Phase 2: CRUD 비즈니스 로직 (예상 54 tests)

```
├── ProductServiceTest.java      (18 tests)
├── ReviewServiceTest.java       (13 tests)
├── ShippingAddressServiceTest.java (13 tests)
└── MemberServiceTest.java       (10 tests)
```

**커밋 단위:**
1. `test(product): ProductService 단위 테스트 구현`
2. `test(review): ReviewService 단위 테스트 구현`
3. `test(address): ShippingAddressService 단위 테스트 구현`
4. `test(member): MemberService 단위 테스트 구현`

### Phase 3: 부가 기능 (예상 8 tests)

```
├── WishlistServiceTest.java     (5 tests)
└── RecentViewServiceTest.java   (3 tests)
```

**커밋 단위:**
1. `test(wishlist,recentview): 부가 기능 단위 테스트 구현`

---

## 테스트 파일 구조

```
src/test/java/com/fluxmall/
├── support/
│   └── TestFixture.java              # 공통 테스트 픽스처
├── auth/
│   └── service/
│       └── AuthServiceTest.java
├── member/
│   └── service/
│       └── MemberServiceTest.java
├── product/
│   └── service/
│       └── ProductServiceTest.java
├── cart/
│   └── service/
│       └── CartServiceTest.java
├── order/
│   └── service/
│       └── OrderServiceTest.java
├── wishlist/
│   └── service/
│       └── WishlistServiceTest.java
├── recentview/
│   └── service/
│       └── RecentViewServiceTest.java
├── review/
│   └── service/
│       └── ReviewServiceTest.java
└── address/
    └── service/
        └── ShippingAddressServiceTest.java
```

---

## 테스트 케이스 상세

### OrderService 테스트 케이스

```java
@Nested
@DisplayName("createOrder - 주문 생성")
class CreateOrder {
    @Test void 성공_정상_주문_생성() { }
    @Test void 실패_존재하지_않는_상품() { }
    @Test void 실패_재고_부족() { }
}

@Nested
@DisplayName("payOrder - 결제 처리")
class PayOrder {
    @Test void 성공_결제_처리_및_재고_차감() { }
    @Test void 실패_존재하지_않는_주문() { }
    @Test void 실패_권한_없음() { }
    @Test void 실패_PENDING_상태가_아님() { }
}

@Nested
@DisplayName("cancelOrder - 주문 취소")
class CancelOrder {
    @Test void 성공_PENDING_상태_취소() { }
    @Test void 성공_PAID_상태_취소_및_재고_원복() { }
    @Test void 실패_존재하지_않는_주문() { }
    @Test void 실패_권한_없음() { }
    @Test void 실패_취소_불가_상태() { }
}

@Nested
@DisplayName("findById - 주문 상세 조회")
class FindById {
    @Test void 성공_주문_상세_조회() { }
    @Test void 실패_존재하지_않는_주문() { }
    @Test void 실패_권한_없음() { }
}
```

### AuthService 테스트 케이스

```java
@Nested
@DisplayName("login - 로그인")
class Login {
    @Test void 성공_로그인_및_토큰_발급() { }
    @Test void 실패_존재하지_않는_사용자() { }
    @Test void 실패_비밀번호_불일치() { }
}

@Nested
@DisplayName("logout - 로그아웃")
class Logout {
    @Test void 성공_로그아웃_및_토큰_블랙리스트() { }
    @Test void 실패_유효하지_않은_토큰() { }
}

@Nested
@DisplayName("refresh - 토큰 갱신")
class Refresh {
    @Test void 성공_토큰_갱신() { }
    @Test void 실패_만료된_토큰() { }
    @Test void 실패_블랙리스트_토큰() { }
}
```

---

## 실행 순서

```
1. TestFixture.java 생성 (공통 테스트 데이터)
2. Phase 1 순차 구현
   - OrderServiceTest.java
   - AuthServiceTest.java
   - CartServiceTest.java
3. Phase 2 순차 구현
   - ProductServiceTest.java
   - ReviewServiceTest.java
   - ShippingAddressServiceTest.java
   - MemberServiceTest.java
4. Phase 3 순차 구현
   - WishlistServiceTest.java
   - RecentViewServiceTest.java
5. 전체 테스트 실행 및 커버리지 확인
```

---

## 예상 결과

| 항목 | 수치 |
|------|------|
| 총 테스트 수 | ~97 tests |
| Phase 1 | 35 tests |
| Phase 2 | 54 tests |
| Phase 3 | 8 tests |
| 예상 Service 커버리지 | 80%+ |

---

**Last Updated**: 2026-01-19
