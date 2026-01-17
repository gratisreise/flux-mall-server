package com.fluxmall.wishlist.controller;

import com.fluxmall.wishlist.service.WishlistService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
@Tag(name = "Wishlist", description = "위시리스트 API")
public class WishlistController {

    private final WishlistService wishlistService;
}
