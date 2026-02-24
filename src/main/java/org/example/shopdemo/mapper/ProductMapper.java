package org.example.shopdemo.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.shopdemo.entity.Product;

import java.util.List;

@Mapper
public interface ProductMapper {
    int insert(Product product);
    Product findById(Long id);
    List<Product> findByIds(@Param("ids") List<Long> ids);
    List<Product> findAll();
    List<Product> findAllIncludingOffShelf();
    List<Product> findByCategoryId(Long categoryId);
    List<Product> searchProducts(@Param("keyword") String keyword);
    int update(Product product);
    int updateImage(@Param("id") Long id, @Param("mainImage") String mainImage, @Param("images") String images);
    int deleteById(Long id);
    int updateStock(@Param("id") Long id, @Param("quantity") Integer quantity);
    int setStock(@Param("id") Long id, @Param("stock") Integer stock);
    int increaseStock(@Param("id") Long id, @Param("quantity") Integer quantity);
    Integer getStockById(@Param("id") Long id);
    List<Product> findAllByPage(@Param("offset") Integer offset, @Param("pageSize") Integer pageSize);
    List<Product> findByCategoryIdByPage(@Param("categoryId") Long categoryId, @Param("offset") Integer offset, @Param("pageSize") Integer pageSize);
    List<Product> searchProductsByPage(@Param("keyword") String keyword, @Param("offset") Integer offset, @Param("pageSize") Integer pageSize);
    Long countAll();
    Long countByCategoryId(Long categoryId);
    Long countByKeyword(@Param("keyword") String keyword);
    List<Product> findRecommendations(@Param("limit") Integer limit);
}
