package org.example.shopdemo.service;

import org.example.shopdemo.dto.PageRequest;
import org.example.shopdemo.dto.PageResponse;
import org.example.shopdemo.entity.Product;
import org.example.shopdemo.entity.ProductFavorite;
import org.example.shopdemo.mapper.ProductFavoriteMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 商品收藏服务类
 * 处理商品收藏相关的业务逻辑
 */
@Service
public class ProductFavoriteService {

    @Autowired
    private ProductFavoriteMapper productFavoriteMapper;

    @Autowired
    private ProductService productService;

    /**
     * 添加收藏
     * @param userId 用户ID
     * @param productId 商品ID
     * @return 是否成功
     */
    @Transactional
    public boolean addFavorite(Long userId, Long productId) {
        ProductFavorite existing = productFavoriteMapper.findByUserAndProduct(userId, productId);
        if (existing != null) {
            return false;
        }

        ProductFavorite favorite = new ProductFavorite();
        favorite.setUserId(userId);
        favorite.setProductId(productId);
        productFavoriteMapper.insert(favorite);
        return true;
    }

    /**
     * 取消收藏
     * @param userId 用户ID
     * @param productId 商品ID
     * @return 是否成功
     */
    @Transactional
    public boolean removeFavorite(Long userId, Long productId) {
        int result = productFavoriteMapper.deleteByUserAndProduct(userId, productId);
        return result > 0;
    }

    /**
     * 检查是否已收藏
     * @param userId 用户ID
     * @param productId 商品ID
     * @return 是否已收藏
     */
    public boolean isFavorited(Long userId, Long productId) {
        ProductFavorite favorite = productFavoriteMapper.findByUserAndProduct(userId, productId);
        return favorite != null;
    }

    /**
     * 获取用户收藏列表（分页）
     * @param userId 用户ID
     * @param pageRequest 分页请求
     * @return 商品列表
     */
    public PageResponse<Product> getUserFavorites(Long userId, PageRequest pageRequest) {
        List<ProductFavorite> favorites = productFavoriteMapper.findByUserId(userId, pageRequest.getOffset(), pageRequest.getPageSize());
        Long total = productFavoriteMapper.countByUserId(userId);

        List<Long> productIds = favorites.stream()
                .map(ProductFavorite::getProductId)
                .toList();

        List<Product> products = new ArrayList<>();
        if (!productIds.isEmpty()) {
            products = productService.getProductsByIds(productIds);
        }

        return PageResponse.of(products, pageRequest.getPageNum(), pageRequest.getPageSize(), total);
    }

    /**
     * 删除收藏
     * @param id 收藏ID
     */
    @Transactional
    public void deleteFavorite(Long id) {
        productFavoriteMapper.deleteById(id);
    }
}
