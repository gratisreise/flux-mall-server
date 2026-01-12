package com.fluxmall.service;



import com.fluxmall.domain.entity.Cart;
import com.fluxmall.domain.entity.CartItem;
import com.fluxmall.mapper.CartMapper;
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