🏗️ MyBatis 기반 백엔드 구현 워크플로우 가이드
본 문서는 MyBatis를 사용하는 환경에서 데이터 접근 계층(Persistence Layer)과 도메인 모델을 설계하고 구현할 때 준수해야 할 시니어 개발자의 표준 워크플로우를 담고 있습니다.

1. [Phase 1] 설계 및 문서화 (Pre-Coding)
   코드를 작성하기 전, SQL 중심의 설계를 명확히 하여 개발 중 발생할 수 있는 구조적 결함을 방지합니다.

1.1 요구사항 분석 및 SQL 전략 수립
기능 목록화: 구현할 기능의 정상/예외 케이스를 정의합니다.

SQL 중심 설계: 복잡한 통계나 조인이 필요한 경우, 사용할 핵심 쿼리의 초안을 먼저 작성합니다.

성능 고려: 인덱스 활용 여부와 대량 데이터 처리 시 페이징 전략을 결정합니다.

1.2 데이터 모델링 (ERD)
DB 테이블 구조를 설계하고, 테이블 간의 관계(1:1, 1:N, N:M)를 정의합니다.

Naming Convention: DB는 snake_case, Java는 camelCase 사용을 원칙으로 합니다.

2. [Phase 2] 도메인 및 레포지토리 구현 (In-Coding)
   실제 Java 코드와 MyBatis XML을 매핑하며 기능을 구현하는 단계입니다.

2.1 도메인 모델(Domain Model) 설계
POJO 준수: 특정 프레임워크에 종속되지 않는 순수 Java 객체를 설계합니다.

캡슐화: Setter 사용을 최소화하고, 도메인 내부에서 상태를 변경하는 비즈니스 메서드를 구현합니다.

타입 안정성: Null 처리가 필요한 필드는 Wrapper Class(Long, Integer)를 사용합니다.

2.2 Mapper 인터페이스 정의
서비스 계층에서 호출할 메서드를 정의합니다.

Tip: 파라미터가 2개 이상일 경우 @Param 어노테이션을 명시하거나, 전용 DTO를 사용합니다.

Java

public interface OrderMapper {
Optional<Order> findById(@Param("id") Long id);
List<Order> findAllByCondition(OrderSearchCondition condition);
int insertOrder(Order order);
}
2.3 MyBatis XML 및 ResultMap 작성
ResultMap: DB 컬럼과 객체 필드를 매핑합니다. 연관된 객체는 <association>이나 <collection>을 활용합니다.

Dynamic SQL: <where>, <if> 등을 사용하여 가독성 높은 동적 쿼리를 작성합니다.

SQL Fragments: 반복되는 컬럼 리스트나 조인 문구는 <sql id="...">로 분리하여 재사용합니다.

XML

<mapper namespace="com.example.mapper.OrderMapper">
    <sql id="orderColumns">
        id, order_no, total_price, status, created_at
    </sql>

    <select id="findById" resultMap="OrderResultMap">
        SELECT <include refid="orderColumns" />
        FROM orders
        WHERE id = #{id}
    </select>
</mapper>


요청하신 MyBatis 환경에서의 서비스 계층 TDD 구현 및 테스트 전략을 정리한 WORKFLOW_SERVICE_TDD.md 파일 내용입니다. 프로젝트의 가이드라인으로 활용하시기 좋습니다.

🛠️ MyBatis 기반 서비스 계층 TDD & 구현 전략
본 문서는 서비스 계층(Service Layer) 구현 시 **TDD(Test Driven Development)**를 적용하는 방법과, 효율적인 테스트 대상 선별 기준(Test Strategy)을 정의합니다.

1. 테스트 작성 전략 (Test Strategy)
   모든 코드를 테스트하는 것은 불가능하며 효율적이지도 않습니다. **'가성비'와 '신뢰도'**를 기준으로 대상을 선정합니다.

✅ 반드시 테스트해야 하는 대상 (High Priority)
비즈니스 규칙 (Business Rules): 정책적으로 중요한 제약 조건 (예: "할인율은 100%를 초과할 수 없다").

복잡한 분기문 (Logic Density): if-else, switch 등 로직의 흐름이 갈라지는 지점.

경계값 검증 (Boundary Analysis): 최소/최대값, 빈 문자열, 리스트의 첫 번째와 마지막 요소 등.

예외 처리 (Exception Handling): 의도한 예외가 적절한 상황에 발생하는지 확인.

❌ 생략해도 되는 대상 (Low Priority)
단순 CRUD 흐름: 로직 없이 단순히 Mapper를 호출하고 결과를 반환하는 코드.

프레임워크 기능: 단순 Bean 주입 확인, 단순 Getter/Setter 등.

타사 라이브러리: 이미 검증된 라이브러리 자체의 기능.

2. 구현 워크플로우 (Implementation Steps)
   Phase 1: 설계 및 테스트 시나리오 정의
   성공(Happy Path): 모든 조건이 충족되었을 때의 결과물.

실패(Edge Cases): 자원 미존재, 권한 없음, 비즈니스 규칙 위반 등의 상황.

Phase 2: Red - 실패하는 테스트 작성
Mockito를 사용하여 Mapper를 Mocking하고, 아직 구현되지 않은 서비스 메서드를 호출하는 테스트를 먼저 작성합니다.

Phase 3: Green - 도메인 위임 기반의 서비스 구현
Thin Service: 서비스는 흐름 제어(조회, 위임, 저장)만 담당합니다.

Rich Domain: 핵심 비즈니스 로직은 도메인 엔티티(POJO) 내부에서 처리합니다.

Phase 4: Refactor - 코드 정제
테스트 통과 후, 코드 중복 제거 및 가독성을 개선합니다. (MyBatis 환경에서는 Dirty Checking이 없으므로 명시적 update 호출을 잊지 마세요.)

3. 코드 구현 예시
   3.1 [Domain] 비즈니스 로직 캡슐화
   Java

public class Order {
private Long id;
private OrderStatus status;

    // 테스트 대상: 핵심 비즈니스 규칙은 엔티티 내부에서 검증
    public void cancel() {
        if (this.status == OrderStatus.SHIPPED) {
            throw new IllegalStateException("이미 배송된 주문은 취소할 수 없습니다.");
        }
        this.status = OrderStatus.CANCELLED;
    }
}
3.2 [Test] Mockito를 활용한 단위 테스트
Java

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
@Mock OrderMapper orderMapper;
@InjectMocks OrderService orderService;

    @Test
    @DisplayName("성공: 주문 취소 시 상태가 CANCELLED로 변경된다")
    void cancel_success() {
        // given
        Order order = new Order(1L, OrderStatus.ORDERED);
        given(orderMapper.findById(1L)).willReturn(Optional.of(order));

        // when
        orderService.cancelOrder(1L);

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(orderMapper).updateStatus(any()); // 업데이트 쿼리 호출 확인
    }

    @Test
    @DisplayName("실패: 배송된 주문은 취소 시 예외가 발생한다")
    void cancel_fail_shipped() {
        // given
        Order order = new Order(1L, OrderStatus.SHIPPED);
        given(orderMapper.findById(1L)).willReturn(Optional.of(order));

        // when & then
        assertThatThrownBy(() -> orderService.cancelOrder(1L))
            .isInstanceOf(IllegalStateException.class);
    }
}
3.3 [Service] 로직 위임 구현
Java

@Service
@RequiredArgsConstructor
public class OrderService {
private final OrderMapper orderMapper;

    @Transactional
    public void cancelOrder(Long id) {
        // 1. 조회 및 예외 처리
        Order order = orderMapper.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("주문 정보를 찾을 수 없습니다."));

        // 2. 비즈니스 로직 위임 (핵심 테스트 구간)
        order.cancel();

        // 3. 상태 저장 (MyBatis 명시적 업데이트)
        orderMapper.updateStatus(order);
    }
}
✅ 시니어의 최종 체크리스트
[ ] 서비스 코드가 얇게(Thin) 유지되고 있는가? (핵심 로직이 엔티티에 있는가?)

[ ] 단순 전달 코드가 아닌, 복잡한 분기문 위주로 테스트를 작성했는가?

[ ] 예외 발생 상황(Negative Test)에 대한 케이스를 포함했는가?

[ ] MyBatis의 특성상 update 메서드가 적절히 호출되었는가?


주니어 개발자님의 성장을 돕기 위해, 컨트롤러 계층(Controller Layer)의 상세 구현 가이드를 작성했습니다.

컨트롤러는 애플리케이션의 **'관문'**입니다. 단순히 요청을 전달하는 것을 넘어, 들어오는 데이터가 유효한지 검증하고 내부의 도메인 모델이 외부로 유출되지 않도록 보호하는 역할을 수행해야 합니다.

🛡️ 컨트롤러 계층(Controller Layer) 구현 가이드
본 문서는 **입력값 검증(Validation)**과 **데이터 변환(Mapping)**을 중심으로 클린한 컨트롤러를 작성하는 표준 워크플로우를 정의합니다.

1. 입력값 검증 (Validation)
   클라이언트는 항상 우리가 의도하지 않은 데이터를 보낼 수 있습니다. 시스템의 안정성을 위해 가장 앞단에서 데이터를 걸러내야 합니다.

1.1 @Valid와 Bean Validation 활용
어노테이션 기반 검증: @NotBlank, @Min, @Max, @Size, @Email 등을 사용하여 선언적으로 검증합니다.

커스텀 검증: 비즈니스 규칙이 복잡한 경우 ConstraintValidator를 상속받아 커스텀 어노테이션을 만듭니다.

1.2 그룹 검증 및 예외 처리
@Validated: 등록(Create)과 수정(Update) 시 검증 규칙이 다를 경우 그룹핑을 통해 제어합니다.

Global Exception Handling: 검증 실패 시 발생하는 MethodArgumentNotValidException을 @RestControllerAdvice에서 가공하여 클라이언트에게 일관된 에러 응답을 보냅니다.

2. 데이터 매핑 (Mapping)
   엔티티(Entity)를 컨트롤러에서 직접 노출하는 것은 **'보안 사고'**이자 **'강한 결합'**의 원인입니다. 반드시 DTO를 사용해야 합니다.

2.1 DTO 사용 원칙
Request DTO: 클라이언트로부터 받는 데이터. 서비스 계층으로 넘어가기 전 필요한 데이터만 포함합니다.

Response DTO: 클라이언트에게 돌려주는 데이터. 엔티티의 모든 필드가 아닌, 화면에 필요한 필드만 선택적으로 담습니다.

2.2 매핑 전략 (Manual vs Library)
수동 매핑: 생성자나 정적 팩토리 메서드(of, from)를 사용하여 변환합니다. 로직이 명확하지만 필드가 많아지면 코드가 비대해집니다.

MapStruct 활용: 컴파일 시점에 매핑 코드를 생성해주는 라이브러리입니다. 성능이 뛰어나고 반복적인 코드를 획기적으로 줄여줍니다. (시니어 추천)

3. 상세 구현 예시 (MyBatis 환경 기준)
   3.1 [Request DTO] 검증 로직 포함
   Java

@Getter
@NoArgsConstructor
public class OrderCreateRequest {
@NotBlank(message = "주문 상품명은 필수입니다.")
private String productName;

    @Min(value = 1, message = "주문 수량은 최소 1개 이상이어야 합니다.")
    private int quantity;

    @NotNull(message = "배송지 정보는 필수입니다.")
    private AddressDto address;
}
3.2 [Controller] 깔끔한 엔드포인트
Java

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
private final OrderService orderService;
private final OrderMapperDto orderMapperDto; // MapStruct 활용 시

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody @Valid OrderCreateRequest request) {
        // 1. DTO를 도메인/서비스용 객체로 변환 (필요 시)
        // 2. 서비스 호출
        Long orderId = orderService.placeOrder(request.toServiceDto());
        
        // 3. 결과를 Response DTO로 감싸서 반환
        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(new OrderResponse(orderId, "주문이 완료되었습니다."));
    }
}
3.3 [MapStruct] 매퍼 인터페이스 (선택 사항)
Java

@Mapper(componentModel = "spring")
public interface OrderMapperDto {
// Entity -> Response DTO 변환 정의
@Mapping(source = "id", target = "orderId")
OrderResponse toResponse(Order order);
}
✅ 컨트롤러 계층 체크리스트
[ ] @Valid를 통해 모든 입력값의 유효성을 검증했는가?

[ ] 엔티티(Entity)가 @RestController의 파라미터나 반환값으로 직접 노출되지 않는가?

[ ] 검증 실패 시 클라이언트에게 명확한 에러 메시지(Field, Message)를 반환하는가?

[ ] 컨트롤러에 비즈니스 로직(계산, DB 접근 등)이 포함되어 있지 않는가? (컨트롤러는 얇게 유지!)



🔍 통합 테스트 및 시스템 최적화 (Verification) 가이드본 문서는 실제 운영 환경과 유사한 조건에서 시스템을 검증하고, SQL 실행 효율을 최적화하여 고성능 백엔드를 구축하기 위한 워크플로우를 담고 있습니다.1. 통합 테스트 (Integration Test)단위 테스트가 '함수 단위'의 정확성을 본다면, 통합 테스트는 **"Spring + MyBatis + DB"**가 한 팀으로 잘 돌아가는지 확인합니다.1.1 Testcontainers 활용 (시니어 추천)Why: 로컬 DB나 H2는 실제 운영 환경(MySQL/PostgreSQL)과 문법이나 동작이 다를 수 있습니다.How: Docker를 이용해 실제 DB 컨테이너를 띄워 테스트를 수행합니다.장점: SCHEMA 차이나 특정 DB 전용 함수 사용 시 발생하는 오류를 완벽히 잡아낼 수 있습니다.1.2 @SpringBootTest 전략전체 컨텍스트 로드: 모든 Bean을 올리고 API 호출부터 DB 저장까지의 전체 흐름(End-to-End)을 테스트합니다.트랜잭션 관리: @Transactional을 테스트 코드에 붙여 테스트가 끝난 후 데이터가 자동으로 롤백되도록 설정합니다.Java@SpringBootTest
@Testcontainers
@Transactional
class OrderIntegrationTest {

    @Autowired OrderService orderService;
    @Autowired OrderMapper orderMapper;

    @Test
    @DisplayName("주문 생성부터 조회까지 전체 흐름 검증")
    void order_full_cycle_test() {
        // 1. Given: 주문 생성 DTO 준비
        OrderCreateRequest request = new OrderCreateRequest("MacBook", 1);

        // 2. When: 주문 실행
        Long orderId = orderService.placeOrder(request);

        // 3. Then: DB에 실제 저장되었는지 확인
        Order savedOrder = orderMapper.findById(orderId).orElseThrow();
        assertThat(savedOrder.getProductName()).isEqualTo("MacBook");
    }
}
2. SQL 및 성능 최적화 (Optimization)
   MyBatis 환경에서는 JPA의 N+1 문제와는 성격이 다르지만, 불필요한 쿼리 호출과 데이터 로딩 전략에 대한 최적화가 반드시 필요합니다.

2.1 MyBatis에서의 N+1 문제 해결
현상: 1번의 주문 조회 후, 각 주문 아이템을 가져오기 위해 N번의 추가 SELECT가 발생하는 경우.

해결책 (Join 활용): <resultMap>의 <collection>이나 <association>을 사용하되, Nested Select 방식이 아닌 Nested Results(Join SQL) 방식을 사용하여 쿼리 한 번에 데이터를 가져옵니다.

2.2 SQL 로그 분석 (p6spy 활용)
로그 시각화: MyBatis는 기본적으로 ? 파라미터로 로그가 찍혀 분석이 어렵습니다. p6spy 라이브러리를 사용해 실제 바인딩된 파라미터와 쿼리 실행 시간을 확인합니다.

Slow Query 식별: 실행 시간이 긴 쿼리를 찾아 인덱스(Index) 설정을 검토합니다.

2.3 대량 데이터 페이징 최적화
Count 쿼리 분리: MyBatis의 collection 조인 페이징은 메모리에서 처리될 위험이 있습니다. 대량 데이터의 경우 전체 개수를 구하는 count 쿼리와 실제 데이터를 가져오는 limit 쿼리를 분리하여 최적화합니다.

항목,체크 포인트,비고
Join 전략,"Lazy Loading이 필요한가, Eager(Join)가 필요한가?",MyBatis는 Join 조회가 유리
Index,WHERE 절과 ORDER BY 절에 인덱스가 걸려 있는가?,EXPLAIN으로 실행 계획 확인
Connection Pool,HikariCP 설정(Maximum Pool Size 등)이 적절한가?,부하 테스트를 통한 산출
Batch Insert,대량 데이터 삽입 시 foreach를 통한 일괄 처리를 하는가?,네트워크 왕복 횟수 감소