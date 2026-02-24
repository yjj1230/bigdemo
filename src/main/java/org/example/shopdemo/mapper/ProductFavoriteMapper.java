package org.example.shopdemo.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.shopdemo.entity.ProductFavorite;

import java.util.List;

@Mapper
public interface ProductFavoriteMapper {
    int insert(ProductFavorite favorite);
    ProductFavorite findByUserAndProduct(@Param("userId") Long userId, @Param("productId") Long productId);
    List<ProductFavorite> findByUserId(@Param("userId") Long userId, @Param("offset") Integer offset, @Param("pageSize") Integer pageSize);
    int deleteById(Long id);
    int deleteByUserAndProduct(@Param("userId") Long userId, @Param("productId") Long productId);
    Long countByUserId(Long userId);
}
