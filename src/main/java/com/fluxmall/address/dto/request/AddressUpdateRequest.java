package com.fluxmall.address.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AddressUpdateRequest(
        @Size(max = 50, message = "수령인 이름은 50자 이하여야 합니다.")
        String recipientName,

        @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "연락처 형식이 올바르지 않습니다. (예: 010-1234-5678)")
        String phone,

        @Pattern(regexp = "^\\d{5}$", message = "우편번호는 5자리 숫자입니다.")
        String postcode,

        @Size(max = 200, message = "주소는 200자 이하여야 합니다.")
        String address1,

        @Size(max = 100, message = "상세주소는 100자 이하여야 합니다.")
        String address2
) {}
