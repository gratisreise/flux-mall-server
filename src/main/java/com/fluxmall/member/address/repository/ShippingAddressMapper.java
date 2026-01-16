package com.fluxmall.member.address.repository;

import com.fluxmall.member.address.domain.ShippingAddress;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ShippingAddressMapper {

    ShippingAddress findById(Long id);

    ShippingAddress findDefaultByMemberId(Long memberId);

    List<ShippingAddress> findByMemberId(Long memberId);

    void insert(ShippingAddress shippingAddress);

    void update(ShippingAddress shippingAddress);

    void clearDefaultByMemberId(Long memberId);

    void setDefault(@Param("id") Long id);

    void delete(Long id);

    void deleteByMemberId(Long memberId);
}
