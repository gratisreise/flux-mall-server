package com.fluxmall.address.controller;

import com.fluxmall.address.service.ShippingAddressService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shipping-addresses")
@RequiredArgsConstructor
@Tag(name = "ShippingAddress", description = "배송지 API")
public class ShippingAddressController {

    private final ShippingAddressService shippingAddressService;
}
