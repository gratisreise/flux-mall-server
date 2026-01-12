package com.fluxmall.mapper;


import com.fluxmall.domain.entity.ShippingAddress;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

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