package com.fluxmall.address.service;

import com.fluxmall.address.domain.ShippingAddress;
import com.fluxmall.address.dto.request.AddressCreateRequest;
import com.fluxmall.address.dto.request.AddressUpdateRequest;
import com.fluxmall.address.dto.response.AddressResponse;
import com.fluxmall.address.exception.AddressError;
import com.fluxmall.address.repository.ShippingAddressMapper;
import com.fluxmall.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShippingAddressService {

    private static final int MAX_ADDRESS_COUNT = 10;

    private final ShippingAddressMapper shippingAddressMapper;

    @Transactional(readOnly = true)
    public List<AddressResponse> getAddresses(Long memberId) {
        List<ShippingAddress> addresses = shippingAddressMapper.findByMemberId(memberId);
        return addresses.stream()
                .map(AddressResponse::from)
                .toList();
    }

    @Transactional
    public AddressResponse createAddress(Long memberId, AddressCreateRequest request) {
        // 최대 개수 확인
        List<ShippingAddress> existingAddresses = shippingAddressMapper.findByMemberId(memberId);
        if (existingAddresses.size() >= MAX_ADDRESS_COUNT) {
            throw new BusinessException(AddressError.MAX_ADDRESS_EXCEEDED);
        }

        ShippingAddress address = request.toEntity(memberId);

        // 첫 배송지이거나 기본 배송지로 설정 요청 시
        if (existingAddresses.isEmpty() || Boolean.TRUE.equals(request.isDefault())) {
            // 기존 기본 배송지 해제
            shippingAddressMapper.clearDefaultByMemberId(memberId);
            address = ShippingAddress.builder()
                    .memberId(memberId)
                    .recipientName(request.recipientName())
                    .phone(request.phone())
                    .postcode(request.postcode())
                    .address1(request.address1())
                    .address2(request.address2())
                    .isDefault(true)
                    .build();
        }

        shippingAddressMapper.insert(address);
        return AddressResponse.from(shippingAddressMapper.findById(address.getId()));
    }

    @Transactional
    public AddressResponse updateAddress(Long memberId, Long addressId, AddressUpdateRequest request) {
        ShippingAddress address = shippingAddressMapper.findById(addressId);
        if (address == null) {
            throw new BusinessException(AddressError.NOT_FOUND);
        }
        if (!address.getMemberId().equals(memberId)) {
            throw new BusinessException(AddressError.NOT_OWNER);
        }

        ShippingAddress updatedAddress = ShippingAddress.builder()
                .id(addressId)
                .memberId(memberId)
                .recipientName(request.recipientName() != null ? request.recipientName() : address.getRecipientName())
                .phone(request.phone() != null ? request.phone() : address.getPhone())
                .postcode(request.postcode() != null ? request.postcode() : address.getPostcode())
                .address1(request.address1() != null ? request.address1() : address.getAddress1())
                .address2(request.address2() != null ? request.address2() : address.getAddress2())
                .isDefault(address.getIsDefault())
                .build();

        shippingAddressMapper.update(updatedAddress);
        return AddressResponse.from(shippingAddressMapper.findById(addressId));
    }

    @Transactional
    public void deleteAddress(Long memberId, Long addressId) {
        ShippingAddress address = shippingAddressMapper.findById(addressId);
        if (address == null) {
            throw new BusinessException(AddressError.NOT_FOUND);
        }
        if (!address.getMemberId().equals(memberId)) {
            throw new BusinessException(AddressError.NOT_OWNER);
        }

        // 기본 배송지 삭제 시, 다른 배송지가 있으면 가장 최근 것을 기본으로 설정
        if (Boolean.TRUE.equals(address.getIsDefault())) {
            List<ShippingAddress> otherAddresses = shippingAddressMapper.findByMemberId(memberId);
            otherAddresses.removeIf(a -> a.getId().equals(addressId));

            if (!otherAddresses.isEmpty()) {
                // 가장 먼저 조회되는 것 (is_default DESC, created_at DESC 정렬이므로 최근 것)
                shippingAddressMapper.setDefault(otherAddresses.get(0).getId());
            }
        }

        shippingAddressMapper.delete(addressId);
    }

    @Transactional
    public AddressResponse setDefaultAddress(Long memberId, Long addressId) {
        ShippingAddress address = shippingAddressMapper.findById(addressId);
        if (address == null) {
            throw new BusinessException(AddressError.NOT_FOUND);
        }
        if (!address.getMemberId().equals(memberId)) {
            throw new BusinessException(AddressError.NOT_OWNER);
        }

        // 기존 기본 배송지 해제
        shippingAddressMapper.clearDefaultByMemberId(memberId);
        // 새 기본 배송지 설정
        shippingAddressMapper.setDefault(addressId);

        return AddressResponse.from(shippingAddressMapper.findById(addressId));
    }
}
