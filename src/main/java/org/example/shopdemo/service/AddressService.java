package org.example.shopdemo.service;

import org.example.shopdemo.entity.Address;
import org.example.shopdemo.mapper.AddressMapper;
import org.example.shopdemo.utils.RedisCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户地址服务类
 * 处理用户地址相关的业务逻辑
 */
@Service
public class AddressService {
    @Autowired
    private AddressMapper addressMapper;
    
    @Autowired
    private RedisCacheService redisCacheService;
    
    private static final String ADDRESS_CACHE_PREFIX = "address";
    private static final String ADDRESS_LIST_CACHE_PREFIX = "address_list";

    /**
     * 获取用户的所有地址（带缓存）
     * @param userId 用户ID
     * @return 地址列表
     */
    public List<Address> getUserAddresses(Long userId) {
        String cacheKey = RedisCacheService.generateKey(ADDRESS_LIST_CACHE_PREFIX, userId.toString());
        Object cachedAddresses = redisCacheService.get(cacheKey);
        
        if (cachedAddresses != null) {
            return (List<Address>) cachedAddresses;
        }
        
        List<Address> addresses = addressMapper.findByUserId(userId);
        redisCacheService.set(cacheKey, addresses, 30, java.util.concurrent.TimeUnit.MINUTES);
        return addresses;
    }

    /**
     * 根据ID获取地址（带缓存）
     * @param id 地址ID
     * @return 地址对象
     */
    public Address getAddressById(Long id) {
        String cacheKey = RedisCacheService.generateKey(ADDRESS_CACHE_PREFIX, id.toString());
        Object cachedAddress = redisCacheService.get(cacheKey);
        
        if (cachedAddress != null) {
            return (Address) cachedAddress;
        }
        
        Address address = addressMapper.findById(id);
        if (address != null) {
            redisCacheService.set(cacheKey, address, 30, java.util.concurrent.TimeUnit.MINUTES);
        }
        return address;
    }

    /**
     * 添加地址
     * @param address 地址对象
     */
    public void addAddress(Address address) {
        if (address.getIsDefault() == 1) {
            addressMapper.clearDefaultAddress(address.getUserId());
        }
        addressMapper.insert(address);
        clearAddressCache(address.getUserId());
    }

    /**
     * 更新地址
     * @param address 地址对象
     */
    public void updateAddress(Address address) {
        // 获取原地址信息，确保userId不为空
        Address existingAddress = addressMapper.findById(address.getId());
        if (existingAddress != null) {
            address.setUserId(existingAddress.getUserId());
        }
        
        if (address.getIsDefault() == 1) {
            addressMapper.clearDefaultAddress(address.getUserId());
            addressMapper.setDefaultAddress(address.getId());
        }
        addressMapper.update(address);
        clearAddressCache(address.getUserId());
        String cacheKey = RedisCacheService.generateKey(ADDRESS_CACHE_PREFIX, address.getId().toString());
        redisCacheService.delete(cacheKey);
    }

    /**
     * 删除地址
     * @param id 地址ID
     */
    public void deleteAddress(Long id) {
        Address address = addressMapper.findById(id);
        addressMapper.deleteById(id);
        
        if (address != null) {
            clearAddressCache(address.getUserId());
            String cacheKey = RedisCacheService.generateKey(ADDRESS_CACHE_PREFIX, id.toString());
            redisCacheService.delete(cacheKey);
        }
    }

    /**
     * 设置默认地址
     * @param userId 用户ID
     * @param addressId 地址ID
     */
    public void setDefaultAddress(Long userId, Long addressId) {
        addressMapper.clearDefaultAddress(userId);
        addressMapper.setDefaultAddress(addressId);
        clearAddressCache(userId);
    }
    
    /**
     * 清除地址缓存
     * @param userId 用户ID
     */
    private void clearAddressCache(Long userId) {
        redisCacheService.delete(RedisCacheService.generateKey(ADDRESS_LIST_CACHE_PREFIX, userId.toString()));
    }
}
