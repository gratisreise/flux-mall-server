package com.fluxmall.address.controller;


import com.fluxmall.address.service.ShippingAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members/shipping-addresses")
@RequiredArgsConstructor
public class ShippingAddressController {

    private final ShippingAddressService shippingAddressService;
}