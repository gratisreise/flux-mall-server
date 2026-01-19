# DTO Naming Convention

## 기본 원칙

DTO 네이밍은 **Entity + 행위 + 타입** 패턴을 따릅니다.

```
{Entity}{Action}{Type}
```

- **Entity**: 도메인 엔티티 이름 (Product, Order, Cart, Member 등)
- **Action**: 수행하는 행위 (Create, Update, Delete, Login, Register 등)
- **Type**: Request 또는 Response

---

## Request DTO 네이밍

### 패턴: `{Entity}{Action}Request`

| 행위 | 네이밍 예시 | 설명 |
|------|------------|------|
| 생성 | `ProductCreateRequest` | 상품 생성 요청 |
| 수정 | `ProductUpdateRequest` | 상품 수정 요청 |
| 삭제 | `CartItemRemoveRequest` | 장바구니 아이템 삭제 요청 |
| 상태 변경 | `ProductStatusUpdateRequest` | 상품 상태 변경 요청 |
| 추가 | `CartItemAddRequest` | 장바구니 아이템 추가 요청 |

### 예외 케이스: 인증/회원 도메인

인증 관련 DTO는 **행위 + Request** 패턴을 사용합니다.

| 네이밍 | 설명 |
|--------|------|
| `LoginRequest` | 로그인 요청 |
| `RegisterRequest` | 회원가입 요청 |
| `TokenRefreshRequest` | 토큰 갱신 요청 |

---

## Response DTO 네이밍

### 단건 조회: `{Entity}Response`

| 네이밍 | 설명 |
|--------|------|
| `ProductResponse` | 상품 단건 응답 |
| `OrderResponse` | 주문 상세 응답 |
| `MemberResponse` | 회원 정보 응답 |

### 목록 조회: `{Entity}ListResponse` (선택적)

복잡한 목록 응답이 단건과 다른 필드를 가질 때 사용합니다.

| 네이밍 | 설명 |
|--------|------|
| `OrderListResponse` | 주문 목록 응답 (간략 정보) |
| `ProductListResponse` | 상품 목록 응답 (간략 정보) |

### 복합 응답: `{Container}Response`

여러 아이템을 포함하는 컨테이너 응답입니다.

| 네이밍 | 설명 |
|--------|------|
| `CartResponse` | 장바구니 전체 응답 (CartItemResponse 포함) |

### 행위 결과 응답: `{Action}Response`

특정 행위의 결과를 반환할 때 사용합니다.

| 네이밍 | 설명 |
|--------|------|
| `LoginResponse` | 로그인 결과 (토큰 포함) |
| `RegisterResponse` | 회원가입 결과 |

---

## 중첩/하위 Entity DTO

복합 Entity의 경우 상위 Entity를 포함하여 명명합니다.

### 패턴: `{ParentEntity}{ChildEntity}{Action}{Type}`

| 네이밍 | 설명 |
|--------|------|
| `OrderItemCreateRequest` | 주문 시 포함되는 아이템 요청 |
| `CartItemAddRequest` | 장바구니 아이템 추가 요청 |
| `CartItemUpdateRequest` | 장바구니 아이템 수량 수정 요청 |
| `CartItemResponse` | 장바구니 아이템 응답 |
| `OrderItemResponse` | 주문 아이템 응답 |

---

## 패키지 구조

```
com.fluxmall.{domain}/
├── dto/
│   ├── request/
│   │   ├── {Entity}{Action}Request.java
│   │   └── ...
│   └── response/
│       ├── {Entity}Response.java
│       └── ...
```

---

## 요약 테이블

| 상황 | 패턴 | 예시 |
|------|------|------|
| 엔티티 생성 요청 | `{Entity}CreateRequest` | `ProductCreateRequest` |
| 엔티티 수정 요청 | `{Entity}UpdateRequest` | `ProductUpdateRequest` |
| 엔티티 삭제 요청 | `{Entity}RemoveRequest` | `CartItemRemoveRequest` |
| 하위 엔티티 추가 | `{Parent}{Child}AddRequest` | `CartItemAddRequest` |
| 상태 변경 요청 | `{Entity}StatusUpdateRequest` | `ProductStatusUpdateRequest` |
| 단건 응답 | `{Entity}Response` | `ProductResponse` |
| 목록 응답 (간략) | `{Entity}ListResponse` | `OrderListResponse` |
| 컨테이너 응답 | `{Container}Response` | `CartResponse` |
| 인증 요청 | `{Action}Request` | `LoginRequest` |
| 인증 응답 | `{Action}Response` | `LoginResponse` |

---

## 체크리스트

새 DTO 생성 시 확인 사항:

- [ ] Entity 이름이 앞에 오는가? (인증 제외)
- [ ] 행위가 명확하게 표현되어 있는가?
- [ ] Request/Response 타입이 명시되어 있는가?
- [ ] 패키지 위치가 올바른가? (`dto/request/` 또는 `dto/response/`)
- [ ] record 타입으로 정의되었는가?
- [ ] Validation 어노테이션이 적절히 적용되었는가? (Request)
- [ ] `from()` 정적 팩토리 메서드가 있는가? (Response)
