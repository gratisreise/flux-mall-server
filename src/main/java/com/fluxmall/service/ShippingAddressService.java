package com.fluxmall.service;


import com.fluxmall.mapper.ShippingAddressMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShippingAddressService {

    private final ShippingAddressMapper shippingAddressMapper;

    public ShippingAddress findById(Long id) {
        return null;
    }

    public ShippingAddress findDefault(Long memberId) {
        return null;
    }

    public List<ShippingAddress> findAllByMemberId(Long memberId) {
        return null;
    }

    public void register(ShippingAddress shippingAddress) {
    }

    public void update(ShippingAddress shippingAddress) {
    }

    public void setDefault(Long memberId, Long addressId) {
    }

    public void delete(Long id) {
    }
}