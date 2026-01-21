package com.fluxmall.cart.service;

import com.fluxmall.cart.domain.Cart;
import com.fluxmall.cart.domain.CartItem;
import com.fluxmall.cart.dto.request.CartItemAddRequest;
import com.fluxmall.cart.dto.request.CartItemUpdateRequest;
import com.fluxmall.cart.dto.response.CartResponse;
import com.fluxmall.cart.exception.CartError;
import com.fluxmall.cart.repository.CartMapper;
import com.fluxmall.global.exception.BusinessException;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartService 테스트")
class CartServiceTest {

    @Mock
    private CartMapper cartMapper;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private CartService cartService;

    @Nested
    @DisplayName("getCart: 장바구니 조회")
    class GetCart {

        @Test
        @DisplayName("성공: 기존 장바구니 조회")
        void 기존_장바구니_조회() {
            // given
            Long memberId = 1L;
            Cart cart = TestFixture.createCart(1L, memberId);
            List<CartItem> cartItems = TestFixture.createCartItems(1L);

            given(cartMapper.findByMemberId(memberId)).willReturn(cart);
            given(cartMapper.findCartItemsByCartId(cart.getId())).willReturn(cartItems);

            // when
            CartResponse response = cartService.getCart(memberId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.items()).hasSize(2);
            verify(cartMapper, never()).insertCart(any(Cart.class));
        }

        @Test
        @DisplayName("성공: 장바구니 없으면 새로 생성")
        void 새_장바구니_생성() {
            // given
            Long memberId = 1L;

            given(cartMapper.findByMemberId(memberId)).willReturn(null);

            // insertCart 호출 시 id를 설정하도록 stubbing
            willAnswer(invocation -> {
                Cart cart = invocation.getArgument(0);
                setFieldValue(cart, "id", 1L);
                return null;
            }).given(cartMapper).insertCart(any(Cart.class));

            given(cartMapper.findCartItemsByCartId(1L)).willReturn(List.of());

            // when
            CartResponse response = cartService.getCart(memberId);

            // then
            assertThat(response).isNotNull();
            verify(cartMapper).insertCart(any(Cart.class));
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
    }

    @Nested
    @DisplayName("addItem: 장바구니 상품 추가")
    class AddItem {

        @Test
        @DisplayName("성공: 새 상품 추가")
        void 새_상품_추가() {
            // given
            Long memberId = 1L;
            Product product = TestFixture.createProduct(1L, "테스트 상품", 10000, 100);
            Cart cart = TestFixture.createCart(1L, memberId);
            CartItemAddRequest request = new CartItemAddRequest(1L, 2);

            given(productMapper.findById(1L)).willReturn(product);
            given(cartMapper.findByMemberId(memberId)).willReturn(cart);
            given(cartMapper.findCartItemByCartIdAndProductId(cart.getId(), 1L)).willReturn(null);

            // when
            cartService.addItem(memberId, request);

            // then
            verify(cartMapper).insertCartItem(any(CartItem.class));
        }

        @Test
        @DisplayName("성공: 기존 상품 수량 증가")
        void 기존_상품_수량_증가() {
            // given
            Long memberId = 1L;
            Product product = TestFixture.createProduct(1L, "테스트 상품", 10000, 100);
            Cart cart = TestFixture.createCart(1L, memberId);
            CartItem existingItem = TestFixture.createCartItem(1L, cart.getId(), 1L, 2);
            CartItemAddRequest request = new CartItemAddRequest(1L, 3);

            given(productMapper.findById(1L)).willReturn(product);
            given(cartMapper.findByMemberId(memberId)).willReturn(cart);
            given(cartMapper.findCartItemByCartIdAndProductId(cart.getId(), 1L)).willReturn(existingItem);

            // when
            cartService.addItem(memberId, request);

            // then
            verify(cartMapper).updateCartItemQuantity(existingItem.getId(), 5); // 2 + 3 = 5
            verify(cartMapper, never()).insertCartItem(any(CartItem.class));
        }

        @Test
        @DisplayName("실패: 상품 미존재")
        void 상품_미존재() {
            // given
            Long memberId = 1L;
            CartItemAddRequest request = new CartItemAddRequest(999L, 2);

            given(productMapper.findById(999L)).willReturn(null);

            // when & then
            assertThatThrownBy(() -> cartService.addItem(memberId, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ProductError.NOT_FOUND);
        }

        @Test
        @DisplayName("실패: 판매 불가 상품")
        void 판매_불가_상품() {
            // given
            Long memberId = 1L;
            Product soldOutProduct = TestFixture.createSoldOutProduct();
            CartItemAddRequest request = new CartItemAddRequest(soldOutProduct.getId(), 2);

            given(productMapper.findById(soldOutProduct.getId())).willReturn(soldOutProduct);

            // when & then
            assertThatThrownBy(() -> cartService.addItem(memberId, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(CartError.PRODUCT_NOT_AVAILABLE);
        }

        @Test
        @DisplayName("실패: 재고 초과")
        void 재고_초과() {
            // given
            Long memberId = 1L;
            Product product = TestFixture.createProduct(1L, "테스트 상품", 10000, 5);
            Cart cart = TestFixture.createCart(1L, memberId);
            CartItemAddRequest request = new CartItemAddRequest(1L, 10);

            given(productMapper.findById(1L)).willReturn(product);
            given(cartMapper.findByMemberId(memberId)).willReturn(cart);
            given(cartMapper.findCartItemByCartIdAndProductId(cart.getId(), 1L)).willReturn(null);

            // when & then
            assertThatThrownBy(() -> cartService.addItem(memberId, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(CartError.EXCEEDS_STOCK);
        }
    }

    @Nested
    @DisplayName("updateItemQuantity: 수량 변경")
    class UpdateItemQuantity {

        @Test
        @DisplayName("성공: 수량 변경")
        void 수량_변경_성공() {
            // given
            Long memberId = 1L;
            Long cartItemId = 1L;
            Cart cart = TestFixture.createCart(1L, memberId);
            CartItem cartItem = TestFixture.createCartItem(cartItemId, cart.getId(), 1L, 2);
            Product product = TestFixture.createProduct(1L, "테스트 상품", 10000, 100);
            CartItemUpdateRequest request = new CartItemUpdateRequest(5);

            given(cartMapper.findCartItemById(cartItemId)).willReturn(cartItem);
            given(cartMapper.findById(cart.getId())).willReturn(cart);
            given(productMapper.findById(1L)).willReturn(product);

            // when
            cartService.updateItemQuantity(memberId, cartItemId, request);

            // then
            verify(cartMapper).updateCartItemQuantity(cartItemId, 5);
        }

        @Test
        @DisplayName("실패: 장바구니 아이템 미존재")
        void 아이템_미존재() {
            // given
            Long memberId = 1L;
            Long cartItemId = 999L;
            CartItemUpdateRequest request = new CartItemUpdateRequest(5);

            given(cartMapper.findCartItemById(cartItemId)).willReturn(null);

            // when & then
            assertThatThrownBy(() -> cartService.updateItemQuantity(memberId, cartItemId, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(CartError.CART_ITEM_NOT_FOUND);
        }

        @Test
        @DisplayName("실패: 소유권 없음")
        void 소유권_없음() {
            // given
            Long memberId = 1L;
            Long otherMemberId = 2L;
            Long cartItemId = 1L;
            Cart otherCart = TestFixture.createCart(1L, otherMemberId);
            CartItem cartItem = TestFixture.createCartItem(cartItemId, otherCart.getId(), 1L, 2);
            CartItemUpdateRequest request = new CartItemUpdateRequest(5);

            given(cartMapper.findCartItemById(cartItemId)).willReturn(cartItem);
            given(cartMapper.findById(otherCart.getId())).willReturn(otherCart);

            // when & then
            assertThatThrownBy(() -> cartService.updateItemQuantity(memberId, cartItemId, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(CartError.NOT_CART_OWNER);
        }

        @Test
        @DisplayName("실패: 재고 초과")
        void 재고_초과() {
            // given
            Long memberId = 1L;
            Long cartItemId = 1L;
            Cart cart = TestFixture.createCart(1L, memberId);
            CartItem cartItem = TestFixture.createCartItem(cartItemId, cart.getId(), 1L, 2);
            Product product = TestFixture.createProduct(1L, "테스트 상품", 10000, 5);
            CartItemUpdateRequest request = new CartItemUpdateRequest(10);

            given(cartMapper.findCartItemById(cartItemId)).willReturn(cartItem);
            given(cartMapper.findById(cart.getId())).willReturn(cart);
            given(productMapper.findById(1L)).willReturn(product);

            // when & then
            assertThatThrownBy(() -> cartService.updateItemQuantity(memberId, cartItemId, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(CartError.EXCEEDS_STOCK);
        }
    }

    @Nested
    @DisplayName("removeItem: 단건 삭제")
    class RemoveItem {

        @Test
        @DisplayName("성공: 상품 삭제")
        void 상품_삭제_성공() {
            // given
            Long memberId = 1L;
            Long cartItemId = 1L;
            Cart cart = TestFixture.createCart(1L, memberId);
            CartItem cartItem = TestFixture.createCartItem(cartItemId, cart.getId(), 1L, 2);

            given(cartMapper.findCartItemById(cartItemId)).willReturn(cartItem);
            given(cartMapper.findById(cart.getId())).willReturn(cart);

            // when
            cartService.removeItem(memberId, cartItemId);

            // then
            verify(cartMapper).deleteCartItem(cartItemId);
        }

        @Test
        @DisplayName("실패: 아이템 미존재")
        void 아이템_미존재() {
            // given
            Long memberId = 1L;
            Long cartItemId = 999L;

            given(cartMapper.findCartItemById(cartItemId)).willReturn(null);

            // when & then
            assertThatThrownBy(() -> cartService.removeItem(memberId, cartItemId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(CartError.CART_ITEM_NOT_FOUND);
        }

        @Test
        @DisplayName("실패: 소유권 없음")
        void 소유권_없음() {
            // given
            Long memberId = 1L;
            Long otherMemberId = 2L;
            Long cartItemId = 1L;
            Cart otherCart = TestFixture.createCart(1L, otherMemberId);
            CartItem cartItem = TestFixture.createCartItem(cartItemId, otherCart.getId(), 1L, 2);

            given(cartMapper.findCartItemById(cartItemId)).willReturn(cartItem);
            given(cartMapper.findById(otherCart.getId())).willReturn(otherCart);

            // when & then
            assertThatThrownBy(() -> cartService.removeItem(memberId, cartItemId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(CartError.NOT_CART_OWNER);
        }
    }

    @Nested
    @DisplayName("removeItems: 다건 삭제")
    class RemoveItems {

        @Test
        @DisplayName("성공: 빈 리스트면 아무 작업 안함")
        void 빈_리스트() {
            // given
            Long memberId = 1L;

            // when
            cartService.removeItems(memberId, List.of());

            // then
            verify(cartMapper, never()).deleteCartItemsByIds(any());
        }

        @Test
        @DisplayName("성공: 다건 삭제")
        void 다건_삭제_성공() {
            // given
            Long memberId = 1L;
            Cart cart = TestFixture.createCart(1L, memberId);
            CartItem item1 = TestFixture.createCartItem(1L, cart.getId(), 1L, 2);
            CartItem item2 = TestFixture.createCartItem(2L, cart.getId(), 2L, 1);
            List<Long> cartItemIds = List.of(1L, 2L);

            given(cartMapper.findCartItemById(1L)).willReturn(item1);
            given(cartMapper.findCartItemById(2L)).willReturn(item2);
            given(cartMapper.findById(cart.getId())).willReturn(cart);

            // when
            cartService.removeItems(memberId, cartItemIds);

            // then
            verify(cartMapper).deleteCartItemsByIds(cartItemIds);
        }

        @Test
        @DisplayName("실패: 일부 아이템 미존재")
        void 일부_아이템_미존재() {
            // given
            Long memberId = 1L;
            Cart cart = TestFixture.createCart(1L, memberId);
            CartItem item1 = TestFixture.createCartItem(1L, cart.getId(), 1L, 2);
            List<Long> cartItemIds = List.of(1L, 999L);

            given(cartMapper.findCartItemById(1L)).willReturn(item1);
            given(cartMapper.findById(cart.getId())).willReturn(cart);
            given(cartMapper.findCartItemById(999L)).willReturn(null);

            // when & then
            assertThatThrownBy(() -> cartService.removeItems(memberId, cartItemIds))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(CartError.CART_ITEM_NOT_FOUND);
        }
    }
}
