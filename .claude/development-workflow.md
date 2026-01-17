1단계: 엔티티 및 리포지토리 구현 (Domain Layer)
가장 먼저 데이터의 핵심 규칙을 코드로 옮깁니다.

Entity 구현: JPA 엔티티를 작성합니다. 이때 비즈니스 로직을 엔티티 내부에 넣는 **'객체지향적 도메인 모델'**을 지향하세요. (Setter 사용 지양, 의미 있는 메서드명 사용)

Repository 인터페이스 생성: Spring Data JPA를 사용하여 기본적인 CRUD를 만듭니다.

검증: DataJpaTest를 통해 DB 테이블이 의도대로 생성되고, 기본 저장/조회가 되는지 확인합니다.

2단계: 도메인 서비스 및 핵심 로직 구현 (Service Layer)
엔티티들이 협력하여 비즈니스 가치를 만드는 단계입니다.

비즈니스 로직 작성: @Service 레이어에서 트랜잭션(@Transactional) 범위를 설정하고 로직을 구현합니다.

도메인 단위 테스트: 외부 의존성(DB 등)을 끊고, 순수 자바 코드로 로직이 맞는지 JUnit5와 Mockito로 검증합니다.

Tip: 이 단계에서 로깅(INFO/DEBUG)을 함께 작성하세요.

3단계: API 컨트롤러 및 DTO 구현 (Presentation Layer)
외부의 요청을 받아 서비스로 넘겨주는 통로를 만듭니다.

DTO(Data Transfer Object) 설계: 엔티티를 직접 노출하지 않고, API 스펙에 맞는 전용 DTO를 만듭니다.

Validation 적용: @Valid, @NotBlank 등을 사용하여 입력 데이터의 무결성을 검증합니다.

Controller 구현: HTTP 매핑과 응답 처리(Response Entity)를 담당합니다.

4단계: 통합 테스트 (Integration Test)
모든 조각이 연결되었을 때 실제로 잘 도는지 확인합니다.
a
@SpringBootTest: 실제 빈을 다 띄우고 API 호출부터 DB 저장까지의 전체 흐름을 테스트합니다.

Testcontainers 활용: 로컬 DB가 아닌 실제 운영 환경과 유사한 Docker 컨테이너 DB에서 테스트를 수행하여 신뢰도를 높입니다.

5단계: 예외 처리 및 공통 로직 정교화 (Refining)
정상 흐름이 완성되었다면 '예외'라는 그물을 짭니다.

GlobalExceptionHandler: @RestControllerAdvice를 사용해 비즈니스 예외(Custom Exception)를 한곳에서 처리합니다.

모니터링 포인트 추가: 성능이 우려되는 구간에 StopWatch로 시간을 측정하거나, 필요한 메트릭을 심습니다.