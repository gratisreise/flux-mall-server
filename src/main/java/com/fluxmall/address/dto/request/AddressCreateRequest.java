package com.fluxmall.address.dto.request;

import com.fluxmall.address.domain.ShippingAddress;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AddressCreateRequest(
        @NotBlank(message = "수령인 이름은 필수입니다.")
        @Size(max = 50, message = "수령인 이름은 50자 이하여야 합니다.")
        String recipientName,

        @NotBlank(message = "연락처는 필수입니다.")
        @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "연락처 형식이 올바르지 않습니다. (예: 010-1234-5678)")
        String phone,

        @NotBlank(message = "우편번호는 필수입니다.")
        @Pattern(regexp = "^\\d{5}$", message = "우편번호는 5자리 숫자입니다.")
        String postcode,

        @NotBlank(message = "주소는 필수입니다.")
        @Size(max = 200, message = "주소는 200자 이하여야 합니다.")
        String address1,

        @Size(max = 100, message = "상세주소는 100자 이하여야 합니다.")
        String address2,

        Boolean isDefault
) {
    public ShippingAddress toEntity(Long memberId) {
        return ShippingAddress.builder()
                .memberId(memberId)
                .recipientName(recipientName)
                .phone(phone)
                .postcode(postcode)
                .address1(address1)
                .address2(address2)
                .isDefault(isDefault != null ? isDefault : false)
                .build();
    }
}
