package com.fluxmall.mapper;

import com.fluxmall.domain.cart.entity.Cart;
import com.fluxmall.domain.cart.entity.CartItem;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CartMapper {

    Cart findByMemberId(Long memberId);

    void createCart(Long memberId);

    void addCartItem(CartItem cartItem);

    void updateCartItemQuantity(CartItem cartItem);

    void deleteCartItem(Long cartItemId);

    void deleteSelectedCartItems(List<Long> cartItemIds);
}