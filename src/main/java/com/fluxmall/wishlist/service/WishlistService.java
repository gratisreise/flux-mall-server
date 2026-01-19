package com.fluxmall.wishlist.service;

import com.fluxmall.global.exception.BusinessException;
import com.fluxmall.product.domain.Product;
import com.fluxmall.product.exception.ProductError;
import com.fluxmall.product.repository.ProductMapper;
import com.fluxmall.wishlist.domain.Wishlist;
import com.fluxmall.wishlist.dto.response.WishlistResponse;
import com.fluxmall.wishlist.exception.WishlistError;
import com.fluxmall.wishlist.repository.WishlistMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistMapper wishlistMapper;
    private final ProductMapper productMapper;

    @Transactional(readOnly = true)
    public List<WishlistResponse> getWishlists(Long memberId, int page, int size) {
        int offset = page * size;
        List<Wishlist> wishlists = wishlistMapper.findByMemberIdWithPaging(memberId, offset, size);

        return wishlists.stream()
                .map(WishlistResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean isWishlisted(Long memberId, Long productId) {
        return wishlistMapper.existsByMemberIdAndProductId(memberId, productId);
    }

    @Transactional
    public boolean toggleWishlist(Long memberId, Long productId) {
        // 상품 존재 여부 확인
        Product product = productMapper.findById(productId);
        if (product == null) {
            throw new BusinessException(ProductError.NOT_FOUND);
        }
        if (product.getProductStatus() == Product.ProductStatus.DISCONTINUED) {
            throw new BusinessException(WishlistError.PRODUCT_NOT_AVAILABLE);
        }

        // 이미 위시리스트에 있으면 삭제, 없으면 추가
        boolean exists = wishlistMapper.existsByMemberIdAndProductId(memberId, productId);

        if (exists) {
            wishlistMapper.deleteByMemberIdAndProductId(memberId, productId);
            return false; // 삭제됨
        } else {
            Wishlist wishlist = Wishlist.builder()
                    .memberId(memberId)
                    .productId(productId)
                    .build();
            wishlistMapper.insert(wishlist);
            return true; // 추가됨
        }
    }

    @Transactional
    public void removeWishlist(Long memberId, Long productId) {
        boolean exists = wishlistMapper.existsByMemberIdAndProductId(memberId, productId);
        if (!exists) {
            throw new BusinessException(WishlistError.NOT_FOUND);
        }

        wishlistMapper.deleteByMemberIdAndProductId(memberId, productId);
    }
}
