package com.fluxmall.wishlist.service;

import com.fluxmall.global.exception.BusinessException;
import com.fluxmall.product.domain.Product;
import com.fluxmall.product.exception.ProductError;
import com.fluxmall.product.repository.ProductMapper;
import com.fluxmall.support.TestFixture;
import com.fluxmall.wishlist.domain.Wishlist;
import com.fluxmall.wishlist.dto.response.WishlistResponse;
import com.fluxmall.wishlist.exception.WishlistError;
import com.fluxmall.wishlist.repository.WishlistMapper;
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
@DisplayName("WishlistService 테스트")
class WishlistServiceTest {

    @Mock
    private WishlistMapper wishlistMapper;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private WishlistService wishlistService;

    @Nested
    @DisplayName("getWishlists: 위시리스트 조회")
    class GetWishlists {

        @Test
        @DisplayName("성공: 위시리스트 목록 조회")
        void 위시리스트_목록_조회_성공() {
            // given
            Long memberId = 1L;
            List<Wishlist> wishlists = List.of(
                    TestFixture.createWishlist(1L, memberId, 1L),
                    TestFixture.createWishlist(2L, memberId, 2L)
            );

            given(wishlistMapper.findByMemberIdWithPaging(eq(memberId), anyInt(), anyInt()))
                    .willReturn(wishlists);

            // when
            List<WishlistResponse> response = wishlistService.getWishlists(memberId, 0, 10);

            // then
            assertThat(response).hasSize(2);
        }

        @Test
        @DisplayName("성공: 위시리스트 없으면 빈 목록 반환")
        void 위시리스트_없으면_빈_목록() {
            // given
            Long memberId = 1L;

            given(wishlistMapper.findByMemberIdWithPaging(eq(memberId), anyInt(), anyInt()))
                    .willReturn(List.of());

            // when
            List<WishlistResponse> response = wishlistService.getWishlists(memberId, 0, 10);

            // then
            assertThat(response).isEmpty();
        }
    }

    @Nested
    @DisplayName("isWishlisted: 위시리스트 여부 확인")
    class IsWishlisted {

        @Test
        @DisplayName("성공: 위시리스트에 있음")
        void 위시리스트에_있음() {
            // given
            Long memberId = 1L;
            Long productId = 1L;

            given(wishlistMapper.existsByMemberIdAndProductId(memberId, productId)).willReturn(true);

            // when
            boolean result = wishlistService.isWishlisted(memberId, productId);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("성공: 위시리스트에 없음")
        void 위시리스트에_없음() {
            // given
            Long memberId = 1L;
            Long productId = 1L;

            given(wishlistMapper.existsByMemberIdAndProductId(memberId, productId)).willReturn(false);

            // when
            boolean result = wishlistService.isWishlisted(memberId, productId);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("toggleWishlist: 위시리스트 토글")
    class ToggleWishlist {

        @Test
        @DisplayName("성공: 위시리스트 추가")
        void 위시리스트_추가() {
            // given
            Long memberId = 1L;
            Long productId = 1L;
            Product product = TestFixture.createProduct(productId, "테스트 상품", 10000, 100);

            given(productMapper.findById(productId)).willReturn(product);
            given(wishlistMapper.existsByMemberIdAndProductId(memberId, productId)).willReturn(false);

            // when
            boolean result = wishlistService.toggleWishlist(memberId, productId);

            // then
            assertThat(result).isTrue();  // 추가됨
            verify(wishlistMapper).insert(any(Wishlist.class));
            verify(wishlistMapper, never()).deleteByMemberIdAndProductId(memberId, productId);
        }

        @Test
        @DisplayName("성공: 위시리스트 삭제")
        void 위시리스트_삭제() {
            // given
            Long memberId = 1L;
            Long productId = 1L;
            Product product = TestFixture.createProduct(productId, "테스트 상품", 10000, 100);

            given(productMapper.findById(productId)).willReturn(product);
            given(wishlistMapper.existsByMemberIdAndProductId(memberId, productId)).willReturn(true);

            // when
            boolean result = wishlistService.toggleWishlist(memberId, productId);

            // then
            assertThat(result).isFalse();  // 삭제됨
            verify(wishlistMapper).deleteByMemberIdAndProductId(memberId, productId);
            verify(wishlistMapper, never()).insert(any(Wishlist.class));
        }

        @Test
        @DisplayName("실패: 상품 없음")
        void 상품_없음() {
            // given
            Long memberId = 1L;
            Long productId = 999L;

            given(productMapper.findById(productId)).willReturn(null);

            // when & then
            assertThatThrownBy(() -> wishlistService.toggleWishlist(memberId, productId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ProductError.NOT_FOUND);
        }

        @Test
        @DisplayName("실패: 판매 중단 상품")
        void 판매_중단_상품() {
            // given
            Long memberId = 1L;
            Long productId = 1L;
            Product discontinuedProduct = TestFixture.createDiscontinuedProduct();

            given(productMapper.findById(productId)).willReturn(discontinuedProduct);

            // when & then
            assertThatThrownBy(() -> wishlistService.toggleWishlist(memberId, productId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(WishlistError.PRODUCT_NOT_AVAILABLE);
        }
    }

    @Nested
    @DisplayName("removeWishlist: 위시리스트 삭제")
    class RemoveWishlist {

        @Test
        @DisplayName("성공: 위시리스트 삭제")
        void 위시리스트_삭제_성공() {
            // given
            Long memberId = 1L;
            Long productId = 1L;

            given(wishlistMapper.existsByMemberIdAndProductId(memberId, productId)).willReturn(true);

            // when
            wishlistService.removeWishlist(memberId, productId);

            // then
            verify(wishlistMapper).deleteByMemberIdAndProductId(memberId, productId);
        }

        @Test
        @DisplayName("실패: 위시리스트에 없음")
        void 위시리스트에_없음() {
            // given
            Long memberId = 1L;
            Long productId = 1L;

            given(wishlistMapper.existsByMemberIdAndProductId(memberId, productId)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> wishlistService.removeWishlist(memberId, productId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(WishlistError.NOT_FOUND);
        }
    }
}
