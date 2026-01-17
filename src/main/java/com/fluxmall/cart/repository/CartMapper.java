package com.fluxmall.cart.repository;

import com.fluxmall.cart.domain.Cart;
import com.fluxmall.cart.domain.CartItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CartMapper {

    Cart findById(Long id);

    Cart findByMemberId(Long memberId);

    List<CartItem> findCartItemsByCartId(Long cartId);

    CartItem findCartItemById(Long id);

    CartItem findCartItemByCartIdAndProductId(@Param("cartId") Long cartId, @Param("productId") Long productId);

    void insertCart(Cart cart);

    void insertCartItem(CartItem cartItem);

    void updateCartItemQuantity(@Param("id") Long id, @Param("quantity") int quantity);

    void deleteCart(Long id);

    void deleteCartItem(Long id);

    void deleteCartItemsByCartId(Long cartId);

    void deleteCartItemsByIds(@Param("ids") List<Long> ids);
}
