package com.fluxmall.cart.repository;



import com.fluxmall.cart.domain.Cart;
import com.fluxmall.cart.domain.CartItem;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CartMapper {

    Cart findByMemberId(Long memberId);

    void createCart(Long memberId);

    void addCartItem(CartItem cartItem);

    void updateCartItemQuantity(CartItem cartItem);

    void deleteCartItem(Long cartItemId);

    void deleteSelectedCartItems(List<Long> cartItemIds);
}