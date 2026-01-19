package com.fluxmall.address.dto.response;

import com.fluxmall.address.domain.ShippingAddress;

import java.time.LocalDateTime;

public record AddressResponse(
        Long id,
        String recipientName,
        String phone,
        String postcode,
        String address1,
        String address2,
        Boolean isDefault,
        LocalDateTime createdAt
) {
    public static AddressResponse from(ShippingAddress address) {
        return new AddressResponse(
                address.getId(),
                address.getRecipientName(),
                address.getPhone(),
                address.getPostcode(),
                address.getAddress1(),
                address.getAddress2(),
                address.getIsDefault(),
                address.getCreatedAt()
        );
    }
}
