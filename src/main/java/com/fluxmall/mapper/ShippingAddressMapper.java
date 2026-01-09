package com.fluxmall.mapper;

import com.fluxmall.domain.member.entity.ShippingAddress;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ShippingAddressMapper {

    ShippingAddress findById(Long id);

    ShippingAddress findDefaultByMemberId(Long memberId);

    List<ShippingAddress> findAllByMemberId(Long memberId);

    void save(ShippingAddress shippingAddress);

    void update(ShippingAddress shippingAddress);

    void setDefault(Long memberId, Long addressId);

    void deleteById(Long id);
}