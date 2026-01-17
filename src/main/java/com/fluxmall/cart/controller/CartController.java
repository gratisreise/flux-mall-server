package com.fluxmall.cart.controller;

import com.fluxmall.cart.service.CartService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "장바구니 API")
public class CartController {

    private final CartService cartService;
}
