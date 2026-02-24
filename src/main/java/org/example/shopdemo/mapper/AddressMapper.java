package org.example.shopdemo.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.shopdemo.entity.Address;

import java.util.List;

@Mapper
public interface AddressMapper {
    int insert(Address address);
    Address findById(Long id);
    List<Address> findByUserId(Long userId);
    int update(Address address);
    int deleteById(Long id);
    int clearDefaultAddress(@Param("userId") Long userId);
    int setDefaultAddress(@Param("addressId") Long addressId);
}
