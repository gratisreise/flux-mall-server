package com.fluxmall.review.service;

import com.fluxmall.global.exception.BusinessException;
import com.fluxmall.order.domain.Order;
import com.fluxmall.order.domain.Order.OrderStatus;
import com.fluxmall.order.domain.OrderItem;
import com.fluxmall.order.repository.OrderMapper;
import com.fluxmall.product.domain.Product;
import com.fluxmall.product.exception.ProductError;
import com.fluxmall.product.repository.ProductMapper;
import com.fluxmall.review.domain.Review;
import com.fluxmall.review.dto.request.ReviewCreateRequest;
import com.fluxmall.review.dto.request.ReviewUpdateRequest;
import com.fluxmall.review.dto.response.ReviewResponse;
import com.fluxmall.review.exception.ReviewError;
import com.fluxmall.review.repository.ReviewMapper;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewService 테스트")
class ReviewServiceTest {

    @Mock
    private ReviewMapper reviewMapper;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ReviewService reviewService;

    @Nested
    @DisplayName("getReviewsByProductId: 상품별 리뷰 조회")
    class GetReviewsByProductId {

        @Test
        @DisplayName("성공: 리뷰 목록 조회")
        void 리뷰_목록_조회_성공() {
            // given
            Long productId = 1L;
            Product product = TestFixture.createProduct();
            List<Review> reviews = List.of(TestFixture.createReview());

            given(productMapper.findById(productId)).willReturn(product);
            given(reviewMapper.findByProductIdWithPaging(eq(productId), isNull(), anyString(), anyInt(), anyInt()))
                    .willReturn(reviews);

            // when
            List<ReviewResponse> response = reviewService.getReviewsByProductId(productId, null, "latest", 0, 10);

            // then
            assertThat(response).hasSize(1);
        }

        @Test
        @DisplayName("실패: 상품 없음")
        void 상품_없음() {
            // given
            Long productId = 999L;

            given(productMapper.findById(productId)).willReturn(null);

            // when & then
            assertThatThrownBy(() -> reviewService.getReviewsByProductId(productId, null, "latest", 0, 10))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ProductError.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("createReview: 리뷰 생성")
    class CreateReview {

        @Test
        @DisplayName("성공: 리뷰 생성")
        void 리뷰_생성_성공() {
            // given
            Long memberId = 1L;
            Long productId = 1L;
            Long orderItemId = 1L;
            Product product = TestFixture.createProduct();
            Order deliveredOrder = TestFixture.createDeliveredOrder(1L, memberId);
            OrderItem orderItem = TestFixture.createOrderItem(orderItemId, deliveredOrder.getId(), productId);
            Review savedReview = TestFixture.createReview(1L, productId, memberId);

            ReviewCreateRequest request = new ReviewCreateRequest(
                    orderItemId,
                    5,
                    "좋은 상품입니다!",
                    List.of()
            );

            given(productMapper.findById(productId)).willReturn(product);
            given(orderMapper.findOrderItemsByOrderId(orderItemId)).willReturn(List.of(orderItem));
            given(orderMapper.findByMemberId(memberId)).willReturn(List.of(deliveredOrder));
            given(orderMapper.findOrderItemsByOrderId(deliveredOrder.getId())).willReturn(List.of(orderItem));
            given(reviewMapper.existsByMemberIdAndOrderItemId(memberId, orderItemId)).willReturn(false);

            // insert 호출 시 id를 설정하도록 stubbing
            willAnswer(invocation -> {
                Review review = invocation.getArgument(0);
                setFieldValue(review, "id", 1L);
                return null;
            }).given(reviewMapper).insert(any(Review.class));

            given(reviewMapper.findById(1L)).willReturn(savedReview);

            // when
            ReviewResponse response = reviewService.createReview(memberId, productId, request);

            // then
            assertThat(response).isNotNull();
            verify(reviewMapper).insert(any(Review.class));
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
        @DisplayName("실패: 상품 없음")
        void 상품_없음() {
            // given
            Long memberId = 1L;
            Long productId = 999L;
            ReviewCreateRequest request = new ReviewCreateRequest(1L, 5, "좋아요", List.of());

            given(productMapper.findById(productId)).willReturn(null);

            // when & then
            assertThatThrownBy(() -> reviewService.createReview(memberId, productId, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ProductError.NOT_FOUND);
        }

        @Test
        @DisplayName("실패: 배송 완료되지 않은 주문")
        void 배송_미완료() {
            // given
            Long memberId = 1L;
            Long productId = 1L;
            Long orderItemId = 1L;
            Product product = TestFixture.createProduct();
            Order paidOrder = TestFixture.createPaidOrder(1L, memberId);  // PAID 상태
            OrderItem orderItem = TestFixture.createOrderItem(orderItemId, paidOrder.getId(), productId);

            ReviewCreateRequest request = new ReviewCreateRequest(orderItemId, 5, "좋아요", List.of());

            given(productMapper.findById(productId)).willReturn(product);
            given(orderMapper.findOrderItemsByOrderId(orderItemId)).willReturn(List.of(orderItem));
            given(orderMapper.findByMemberId(memberId)).willReturn(List.of(paidOrder));
            given(orderMapper.findOrderItemsByOrderId(paidOrder.getId())).willReturn(List.of(orderItem));

            // when & then
            assertThatThrownBy(() -> reviewService.createReview(memberId, productId, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ReviewError.ORDER_NOT_DELIVERED);
        }

        @Test
        @DisplayName("실패: 이미 리뷰 작성")
        void 이미_리뷰_작성() {
            // given
            Long memberId = 1L;
            Long productId = 1L;
            Long orderItemId = 1L;
            Product product = TestFixture.createProduct();
            Order deliveredOrder = TestFixture.createDeliveredOrder(1L, memberId);
            OrderItem orderItem = TestFixture.createOrderItem(orderItemId, deliveredOrder.getId(), productId);

            ReviewCreateRequest request = new ReviewCreateRequest(orderItemId, 5, "좋아요", List.of());

            given(productMapper.findById(productId)).willReturn(product);
            given(orderMapper.findOrderItemsByOrderId(orderItemId)).willReturn(List.of(orderItem));
            given(orderMapper.findByMemberId(memberId)).willReturn(List.of(deliveredOrder));
            given(orderMapper.findOrderItemsByOrderId(deliveredOrder.getId())).willReturn(List.of(orderItem));
            given(reviewMapper.existsByMemberIdAndOrderItemId(memberId, orderItemId)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> reviewService.createReview(memberId, productId, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ReviewError.ALREADY_REVIEWED);
        }
    }

    @Nested
    @DisplayName("updateReview: 리뷰 수정")
    class UpdateReview {

        @Test
        @DisplayName("성공: 리뷰 수정")
        void 리뷰_수정_성공() {
            // given
            Long memberId = 1L;
            Long reviewId = 1L;
            Review review = TestFixture.createReview(reviewId, 1L, memberId);
            ReviewUpdateRequest request = new ReviewUpdateRequest(4, "수정된 내용", List.of());

            given(reviewMapper.findById(reviewId)).willReturn(review);

            // when
            ReviewResponse response = reviewService.updateReview(memberId, reviewId, request);

            // then
            assertThat(response).isNotNull();
            verify(reviewMapper).update(any(Review.class));
        }

        @Test
        @DisplayName("실패: 리뷰 없음")
        void 리뷰_없음() {
            // given
            Long memberId = 1L;
            Long reviewId = 999L;
            ReviewUpdateRequest request = new ReviewUpdateRequest(4, "수정", List.of());

            given(reviewMapper.findById(reviewId)).willReturn(null);

            // when & then
            assertThatThrownBy(() -> reviewService.updateReview(memberId, reviewId, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ReviewError.NOT_FOUND);
        }

        @Test
        @DisplayName("실패: 삭제된 리뷰")
        void 삭제된_리뷰() {
            // given
            Long memberId = 1L;
            Long reviewId = 2L;
            Review deletedReview = TestFixture.createDeletedReview();
            ReviewUpdateRequest request = new ReviewUpdateRequest(4, "수정", List.of());

            given(reviewMapper.findById(reviewId)).willReturn(deletedReview);

            // when & then
            assertThatThrownBy(() -> reviewService.updateReview(memberId, reviewId, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ReviewError.NOT_FOUND);
        }

        @Test
        @DisplayName("실패: 권한 없음")
        void 권한_없음() {
            // given
            Long memberId = 2L;  // 다른 사용자
            Long reviewId = 1L;
            Review review = TestFixture.createReview(reviewId, 1L, 1L);  // memberId=1
            ReviewUpdateRequest request = new ReviewUpdateRequest(4, "수정", List.of());

            given(reviewMapper.findById(reviewId)).willReturn(review);

            // when & then
            assertThatThrownBy(() -> reviewService.updateReview(memberId, reviewId, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ReviewError.NOT_REVIEW_OWNER);
        }
    }

    @Nested
    @DisplayName("deleteReview: 리뷰 삭제 (Soft Delete)")
    class DeleteReview {

        @Test
        @DisplayName("성공: 리뷰 삭제")
        void 리뷰_삭제_성공() {
            // given
            Long memberId = 1L;
            Long reviewId = 1L;
            Review review = TestFixture.createReview(reviewId, 1L, memberId);

            given(reviewMapper.findById(reviewId)).willReturn(review);

            // when
            reviewService.deleteReview(memberId, reviewId);

            // then
            verify(reviewMapper).softDelete(reviewId);
        }

        @Test
        @DisplayName("실패: 리뷰 없음")
        void 리뷰_없음() {
            // given
            Long memberId = 1L;
            Long reviewId = 999L;

            given(reviewMapper.findById(reviewId)).willReturn(null);

            // when & then
            assertThatThrownBy(() -> reviewService.deleteReview(memberId, reviewId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ReviewError.NOT_FOUND);
        }

        @Test
        @DisplayName("실패: 권한 없음")
        void 권한_없음() {
            // given
            Long memberId = 2L;  // 다른 사용자
            Long reviewId = 1L;
            Review review = TestFixture.createReview(reviewId, 1L, 1L);  // memberId=1

            given(reviewMapper.findById(reviewId)).willReturn(review);

            // when & then
            assertThatThrownBy(() -> reviewService.deleteReview(memberId, reviewId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ReviewError.NOT_REVIEW_OWNER);
        }
    }

    @Nested
    @DisplayName("forceDeleteReview: 리뷰 강제 삭제 (Hard Delete)")
    class ForceDeleteReview {

        @Test
        @DisplayName("성공: 리뷰 강제 삭제")
        void 강제_삭제_성공() {
            // given
            Long reviewId = 1L;
            Review review = TestFixture.createReview();

            given(reviewMapper.findById(reviewId)).willReturn(review);

            // when
            reviewService.forceDeleteReview(reviewId);

            // then
            verify(reviewMapper).delete(reviewId);
        }

        @Test
        @DisplayName("실패: 리뷰 없음")
        void 리뷰_없음() {
            // given
            Long reviewId = 999L;

            given(reviewMapper.findById(reviewId)).willReturn(null);

            // when & then
            assertThatThrownBy(() -> reviewService.forceDeleteReview(reviewId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ReviewError.NOT_FOUND);
        }
    }
}
