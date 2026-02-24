package org.example.shopdemo.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.shopdemo.entity.Cart;

import java.util.List;

@Mapper
public interface CartMapper {
    int insert(Cart cart);
    Cart findById(Long id);
    Cart findByUserIdAndProductId(@Param("userId") Long userId, @Param("productId") Long productId);
    List<Cart> findByUserId(Long userId);
    int update(Cart cart);
    int deleteById(Long id);
    int deleteByUserId(Long userId);
}
