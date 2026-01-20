package com.fluxmall.order.service;

import com.fluxmall.global.exception.BusinessException;
import com.fluxmall.order.domain.Order;
import com.fluxmall.order.domain.Order.OrderStatus;
import com.fluxmall.order.domain.OrderItem;
import com.fluxmall.order.dto.request.OrderCreateRequest;
import com.fluxmall.order.dto.request.OrderItemCreateRequest;
import com.fluxmall.order.dto.response.OrderResponse;
import com.fluxmall.order.exception.OrderError;
import com.fluxmall.order.repository.OrderMapper;
import com.fluxmall.product.domain.Product;
import com.fluxmall.product.exception.ProductError;
import com.fluxmall.product.repository.ProductMapper;
import com.fluxmall.support.TestFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService 테스트")
class OrderServiceTest {

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private OrderService orderService;

    @Nested
    @DisplayName("createOrder: 주문 생성")
    class CreateOrder {

        @Test
        @DisplayName("성공: 정상적인 주문 생성")
        void 주문_생성_성공() {
            // given
            Long memberId = 1L;
            Product product = TestFixture.createProduct(1L, "테스트 상품", 10000, 100);
            OrderItemCreateRequest itemRequest = new OrderItemCreateRequest(1L, 2);
            OrderCreateRequest request = new OrderCreateRequest(
                    List.of(itemRequest),
                    "서울시 강남구 테스트로 123"
            );

            Order savedOrder = TestFixture.createOrder(1L, memberId, OrderStatus.PENDING);
            List<OrderItem> savedOrderItems = List.of(TestFixture.createOrderItem(1L, 1L, 1L));

            given(productMapper.findById(1L)).willReturn(product);

            // insertOrder 호출 시 id를 설정하도록 stubbing
            willAnswer(invocation -> {
                Order order = invocation.getArgument(0);
                setFieldValue(order, "id", 1L);
                return null;
            }).given(orderMapper).insertOrder(any(Order.class));

            given(orderMapper.findById(1L)).willReturn(savedOrder);
            given(orderMapper.findOrderItemsByOrderId(1L)).willReturn(savedOrderItems);

            // when
            OrderResponse response = orderService.createOrder(memberId, request);

            // then
            assertThat(response).isNotNull();
            verify(orderMapper).insertOrder(any(Order.class));
            verify(orderMapper).insertOrderItem(any(OrderItem.class));
        }

        private void setFieldValue(Object object, String fieldName, Object value) {
            try {
                Field field = object.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(object, value);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Test
        @DisplayName("실패: 주문 상품이 비어있음")
        void 주문_상품_비어있음() {
            // given
            Long memberId = 1L;
            OrderCreateRequest request = new OrderCreateRequest(
                    List.of(),
                    "서울시 강남구 테스트로 123"
            );

            // when & then
            assertThatThrownBy(() -> orderService.createOrder(memberId, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderError.EMPTY_ORDER_ITEMS);
        }

        @Test
        @DisplayName("실패: 상품이 존재하지 않음")
        void 상품_존재하지_않음() {
            // given
            Long memberId = 1L;
            OrderItemCreateRequest itemRequest = new OrderItemCreateRequest(999L, 2);
            OrderCreateRequest request = new OrderCreateRequest(
                    List.of(itemRequest),
                    "서울시 강남구 테스트로 123"
            );

            given(productMapper.findById(999L)).willReturn(null);

            // when & then
            assertThatThrownBy(() -> orderService.createOrder(memberId, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ProductError.NOT_FOUND);
        }

        @Test
        @DisplayName("실패: 판매 중이 아닌 상품")
        void 판매_중이_아닌_상품() {
            // given
            Long memberId = 1L;
            Product soldOutProduct = TestFixture.createSoldOutProduct();
            OrderItemCreateRequest itemRequest = new OrderItemCreateRequest(soldOutProduct.getId(), 2);
            OrderCreateRequest request = new OrderCreateRequest(
                    List.of(itemRequest),
                    "서울시 강남구 테스트로 123"
            );

            given(productMapper.findById(soldOutProduct.getId())).willReturn(soldOutProduct);

            // when & then
            assertThatThrownBy(() -> orderService.createOrder(memberId, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderError.CANNOT_PAY);
        }

        @Test
        @DisplayName("실패: 재고 부족")
        void 재고_부족() {
            // given
            Long memberId = 1L;
            Product product = TestFixture.createProduct(1L, "테스트 상품", 10000, 5);
            OrderItemCreateRequest itemRequest = new OrderItemCreateRequest(1L, 10); // 재고보다 많이 주문
            OrderCreateRequest request = new OrderCreateRequest(
                    List.of(itemRequest),
                    "서울시 강남구 테스트로 123"
            );

            given(productMapper.findById(1L)).willReturn(product);

            // when & then
            assertThatThrownBy(() -> orderService.createOrder(memberId, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderError.INSUFFICIENT_STOCK);
        }
    }

    @Nested
    @DisplayName("payOrder: 주문 결제")
    class PayOrder {

        @Test
        @DisplayName("성공: 정상 결제")
        void 주문_결제_성공() {
            // given
            Long memberId = 1L;
            Long orderId = 1L;
            Order order = TestFixture.createOrder(orderId, memberId, OrderStatus.PENDING);
            Product product = TestFixture.createProduct(1L, "테스트 상품", 10000, 100);
            OrderItem orderItem = TestFixture.createOrderItem(1L, orderId, 1L);

            given(orderMapper.findByIdForUpdate(orderId)).willReturn(order);
            given(orderMapper.findOrderItemsByOrderId(orderId)).willReturn(List.of(orderItem));
            given(productMapper.findByIdForUpdate(1L)).willReturn(product);

            // when
            orderService.payOrder(memberId, orderId);

            // then
            verify(productMapper).updateStock(eq(1L), anyInt());
            verify(orderMapper).updateStatus(orderId, OrderStatus.PAID.name());
        }

        @Test
        @DisplayName("실패: 주문이 존재하지 않음")
        void 주문_존재하지_않음() {
            // given
            Long memberId = 1L;
            Long orderId = 999L;

            given(orderMapper.findByIdForUpdate(orderId)).willReturn(null);

            // when & then
            assertThatThrownBy(() -> orderService.payOrder(memberId, orderId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderError.NOT_FOUND);
        }

        @Test
        @DisplayName("실패: 주문 소유자가 아님")
        void 주문_소유자_아님() {
            // given
            Long memberId = 1L;
            Long otherMemberId = 2L;
            Long orderId = 1L;
            Order order = TestFixture.createOrder(orderId, otherMemberId, OrderStatus.PENDING);

            given(orderMapper.findByIdForUpdate(orderId)).willReturn(order);

            // when & then
            assertThatThrownBy(() -> orderService.payOrder(memberId, orderId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderError.NOT_ORDER_OWNER);
        }

        @Test
        @DisplayName("실패: 이미 결제된 주문")
        void 이미_결제된_주문() {
            // given
            Long memberId = 1L;
            Long orderId = 1L;
            Order paidOrder = TestFixture.createPaidOrder(orderId, memberId);

            given(orderMapper.findByIdForUpdate(orderId)).willReturn(paidOrder);

            // when & then
            assertThatThrownBy(() -> orderService.payOrder(memberId, orderId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderError.ALREADY_PAID);
        }

        @Test
        @DisplayName("실패: 결제 불가 상태")
        void 결제_불가_상태() {
            // given
            Long memberId = 1L;
            Long orderId = 1L;
            Order cancelledOrder = TestFixture.createCancelledOrder(orderId, memberId);

            given(orderMapper.findByIdForUpdate(orderId)).willReturn(cancelledOrder);

            // when & then
            assertThatThrownBy(() -> orderService.payOrder(memberId, orderId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderError.CANNOT_PAY);
        }
    }

    @Nested
    @DisplayName("cancelOrder: 주문 취소")
    class CancelOrder {

        @Test
        @DisplayName("성공: PENDING 상태 주문 취소")
        void 대기_상태_주문_취소_성공() {
            // given
            Long memberId = 1L;
            Long orderId = 1L;
            Order order = TestFixture.createOrder(orderId, memberId, OrderStatus.PENDING);

            given(orderMapper.findByIdForUpdate(orderId)).willReturn(order);

            // when
            orderService.cancelOrder(memberId, orderId);

            // then
            verify(orderMapper).updateStatus(orderId, OrderStatus.CANCELLED.name());
            verify(productMapper, never()).updateStock(anyLong(), anyInt()); // 재고 복원 없음
        }

        @Test
        @DisplayName("성공: PAID 상태 주문 취소 (재고 복원)")
        void 결제_상태_주문_취소_재고_복원() {
            // given
            Long memberId = 1L;
            Long orderId = 1L;
            Order paidOrder = TestFixture.createPaidOrder(orderId, memberId);
            Product product = TestFixture.createProduct(1L, "테스트 상품", 10000, 97);
            OrderItem orderItem = TestFixture.createOrderItem(1L, orderId, 1L);

            given(orderMapper.findByIdForUpdate(orderId)).willReturn(paidOrder);
            given(orderMapper.findOrderItemsByOrderId(orderId)).willReturn(List.of(orderItem));
            given(productMapper.findByIdForUpdate(1L)).willReturn(product);

            // when
            orderService.cancelOrder(memberId, orderId);

            // then
            verify(orderMapper).updateStatus(orderId, OrderStatus.CANCELLED.name());
            verify(productMapper).updateStock(eq(1L), eq(100)); // 97 + 3 = 100 (orderItem quantity is 3)
        }

        @Test
        @DisplayName("실패: 주문이 존재하지 않음")
        void 주문_존재하지_않음() {
            // given
            Long memberId = 1L;
            Long orderId = 999L;

            given(orderMapper.findByIdForUpdate(orderId)).willReturn(null);

            // when & then
            assertThatThrownBy(() -> orderService.cancelOrder(memberId, orderId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderError.NOT_FOUND);
        }

        @Test
        @DisplayName("실패: 주문 소유자가 아님")
        void 주문_소유자_아님() {
            // given
            Long memberId = 1L;
            Long otherMemberId = 2L;
            Long orderId = 1L;
            Order order = TestFixture.createOrder(orderId, otherMemberId, OrderStatus.PENDING);

            given(orderMapper.findByIdForUpdate(orderId)).willReturn(order);

            // when & then
            assertThatThrownBy(() -> orderService.cancelOrder(memberId, orderId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderError.NOT_ORDER_OWNER);
        }

        @Test
        @DisplayName("실패: 이미 취소된 주문")
        void 이미_취소된_주문() {
            // given
            Long memberId = 1L;
            Long orderId = 1L;
            Order cancelledOrder = TestFixture.createCancelledOrder(orderId, memberId);

            given(orderMapper.findByIdForUpdate(orderId)).willReturn(cancelledOrder);

            // when & then
            assertThatThrownBy(() -> orderService.cancelOrder(memberId, orderId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderError.ALREADY_CANCELLED);
        }

        @Test
        @DisplayName("실패: 배송 중인 주문 취소 불가")
        void 배송_중_취소_불가() {
            // given
            Long memberId = 1L;
            Long orderId = 1L;
            Order shippedOrder = TestFixture.createOrder(orderId, memberId, OrderStatus.SHIPPED);

            given(orderMapper.findByIdForUpdate(orderId)).willReturn(shippedOrder);

            // when & then
            assertThatThrownBy(() -> orderService.cancelOrder(memberId, orderId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderError.CANNOT_CANCEL);
        }
    }

    @Nested
    @DisplayName("findById: 주문 상세 조회")
    class FindById {

        @Test
        @DisplayName("성공: 주문 상세 조회")
        void 주문_상세_조회_성공() {
            // given
            Long memberId = 1L;
            Long orderId = 1L;
            Order order = TestFixture.createOrder(orderId, memberId, OrderStatus.PENDING);
            List<OrderItem> orderItems = List.of(TestFixture.createOrderItem(1L, orderId, 1L));

            given(orderMapper.findById(orderId)).willReturn(order);
            given(orderMapper.findOrderItemsByOrderId(orderId)).willReturn(orderItems);

            // when
            OrderResponse response = orderService.findById(memberId, orderId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.orderId()).isEqualTo(orderId);
        }

        @Test
        @DisplayName("실패: 주문이 존재하지 않음")
        void 주문_존재하지_않음() {
            // given
            Long memberId = 1L;
            Long orderId = 999L;

            given(orderMapper.findById(orderId)).willReturn(null);

            // when & then
            assertThatThrownBy(() -> orderService.findById(memberId, orderId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderError.NOT_FOUND);
        }

        @Test
        @DisplayName("실패: 주문 소유자가 아님")
        void 주문_소유자_아님() {
            // given
            Long memberId = 1L;
            Long otherMemberId = 2L;
            Long orderId = 1L;
            Order order = TestFixture.createOrder(orderId, otherMemberId, OrderStatus.PENDING);

            given(orderMapper.findById(orderId)).willReturn(order);

            // when & then
            assertThatThrownBy(() -> orderService.findById(memberId, orderId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderError.NOT_ORDER_OWNER);
        }
    }
}
