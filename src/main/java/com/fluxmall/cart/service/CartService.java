package com.fluxmall.cart.service;



import com.fluxmall.cart.domain.Cart;
import com.fluxmall.cart.domain.CartItem;
import com.fluxmall.cart.repository.CartMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartMapper cartMapper;

    public Cart findByMemberId(Long memberId) {
        return null;
    }

    public void addItem(CartItem cartItem) {
    }

    public void updateQuantity(CartItem cartItem) {
    }

    public void removeItem(Long cartItemId) {
    }

    public void removeSelectedItems(List<Long> cartItemIds) {
    }
}