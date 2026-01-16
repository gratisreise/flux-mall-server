package com.fluxmall.address.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingAddress {
    private Long id;
    private Long memberId;
    private String recipientName;
    private String phone;
    private String postcode;
    private String address1;
    private String address2;
    private Boolean isDefault;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void setAsDefault() {
        this.isDefault = true;
    }

    public void unsetDefault() {
        this.isDefault = false;
    }
}
