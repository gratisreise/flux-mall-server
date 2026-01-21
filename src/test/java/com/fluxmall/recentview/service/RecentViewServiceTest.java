package com.fluxmall.recentview.service;

import com.fluxmall.global.exception.BusinessException;
import com.fluxmall.product.domain.Product;
import com.fluxmall.product.exception.ProductError;
import com.fluxmall.product.repository.ProductMapper;
import com.fluxmall.recentview.domain.RecentView;
import com.fluxmall.recentview.dto.response.RecentViewResponse;
import com.fluxmall.recentview.repository.RecentViewMapper;
import com.fluxmall.support.TestFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecentViewService 테스트")
class RecentViewServiceTest {

    @Mock
    private RecentViewMapper recentViewMapper;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private RecentViewService recentViewService;

    @Nested
    @DisplayName("getRecentViews: 최근 본 상품 조회")
    class GetRecentViews {

        @Test
        @DisplayName("성공: 최근 본 상품 목록 조회")
        void 최근_본_상품_목록_조회_성공() {
            // given
            Long memberId = 1L;
            List<RecentView> recentViews = TestFixture.createRecentViews(memberId);

            given(recentViewMapper.findByMemberId(eq(memberId), anyInt())).willReturn(recentViews);

            // when
            List<RecentViewResponse> response = recentViewService.getRecentViews(memberId, 20);

            // then
            assertThat(response).hasSize(3);
        }

        @Test
        @DisplayName("성공: 최근 본 상품 없으면 빈 목록 반환")
        void 최근_본_상품_없으면_빈_목록() {
            // given
            Long memberId = 1L;

            given(recentViewMapper.findByMemberId(eq(memberId), anyInt())).willReturn(List.of());

            // when
            List<RecentViewResponse> response = recentViewService.getRecentViews(memberId, 20);

            // then
            assertThat(response).isEmpty();
        }

        @Test
        @DisplayName("성공: limit이 MAX_RECENT_VIEWS를 초과하면 제한")
        void limit_제한() {
            // given
            Long memberId = 1L;
            List<RecentView> recentViews = TestFixture.createRecentViews(memberId);

            given(recentViewMapper.findByMemberId(memberId, 50)).willReturn(recentViews);

            // when
            recentViewService.getRecentViews(memberId, 100);  // 100 요청하지만 50으로 제한됨

            // then
            verify(recentViewMapper).findByMemberId(memberId, 50);
        }
    }

    @Nested
    @DisplayName("recordView: 최근 본 상품 기록")
    class RecordView {

        @Test
        @DisplayName("성공: 신규 기록 추가")
        void 신규_기록_추가() {
            // given
            Long memberId = 1L;
            Long productId = 1L;
            Product product = TestFixture.createProduct(productId, "테스트 상품", 10000, 100);

            given(productMapper.findById(productId)).willReturn(product);
            given(recentViewMapper.findByMemberIdAndProductId(memberId, productId)).willReturn(null);
            given(recentViewMapper.countByMemberId(memberId)).willReturn(10);

            // when
            recentViewService.recordView(memberId, productId);

            // then
            verify(recentViewMapper).insert(any(RecentView.class));
            verify(recentViewMapper, never()).updateViewedAt(memberId, productId);
        }

        @Test
        @DisplayName("성공: 기존 기록 업데이트")
        void 기존_기록_업데이트() {
            // given
            Long memberId = 1L;
            Long productId = 1L;
            Product product = TestFixture.createProduct(productId, "테스트 상품", 10000, 100);
            RecentView existingView = TestFixture.createRecentView(1L, memberId, productId);

            given(productMapper.findById(productId)).willReturn(product);
            given(recentViewMapper.findByMemberIdAndProductId(memberId, productId)).willReturn(existingView);

            // when
            recentViewService.recordView(memberId, productId);

            // then
            verify(recentViewMapper).updateViewedAt(memberId, productId);
            verify(recentViewMapper, never()).insert(any(RecentView.class));
        }

        @Test
        @DisplayName("성공: 최대 개수 초과 시 오래된 기록 삭제")
        void 최대_개수_초과_시_삭제() {
            // given
            Long memberId = 1L;
            Long productId = 1L;
            Product product = TestFixture.createProduct(productId, "테스트 상품", 10000, 100);

            given(productMapper.findById(productId)).willReturn(product);
            given(recentViewMapper.findByMemberIdAndProductId(memberId, productId)).willReturn(null);
            given(recentViewMapper.countByMemberId(memberId)).willReturn(51);  // 50개 초과

            // when
            recentViewService.recordView(memberId, productId);

            // then
            verify(recentViewMapper).insert(any(RecentView.class));
            verify(recentViewMapper).deleteOldestByMemberId(memberId, 50);
        }

        @Test
        @DisplayName("실패: 상품 없음")
        void 상품_없음() {
            // given
            Long memberId = 1L;
            Long productId = 999L;

            given(productMapper.findById(productId)).willReturn(null);

            // when & then
            assertThatThrownBy(() -> recentViewService.recordView(memberId, productId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ProductError.NOT_FOUND);
        }
    }
}
