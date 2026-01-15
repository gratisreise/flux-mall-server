package com.fluxmall.wishlist.service;


import com.fluxmall.wishlist.domain.Wishlist;
import com.fluxmall.wishlist.repository.WishlistMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistMapper wishlistMapper;

    public boolean isWish(Long memberId, Long productId) {
        return false;
    }

    public List<Wishlist> findAllByMemberId(Long memberId) {
        return null;
    }

    public void addWish(Long memberId, Long productId) {
    }

    public void removeWish(Long memberId, Long productId) {
    }

    public void removeSelected(Long memberId, List<Long> productIds) {
    }
}