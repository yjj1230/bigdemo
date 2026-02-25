package org.example.shopdemo.service;

import org.example.shopdemo.dto.PageRequest;
import org.example.shopdemo.dto.PageResponse;
import org.example.shopdemo.entity.Category;
import org.example.shopdemo.entity.Product;
import org.example.shopdemo.enums.ProductStatus;
import org.example.shopdemo.mapper.CategoryMapper;
import org.example.shopdemo.mapper.ProductMapper;
import org.example.shopdemo.utils.AdvancedRedisCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商品服务类
 * 处理商品相关的业务逻辑
 */
@Service
public class ProductService {
    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CategoryMapper categoryMapper;
    
    @Autowired
    private AdvancedRedisCacheService advancedRedisCacheService;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final String PRODUCT_CACHE_PREFIX = "product";
    private static final String PRODUCT_LIST_CACHE_PREFIX = "product_list";
    private static final String PRODUCT_CATEGORY_CACHE_PREFIX = "product_category";

    /**
     * 获取所有商品（带缓存击穿和雪崩防护）
     * @return 商品列表
     */
    public List<Product> getAllProducts() {
        String cacheKey = AdvancedRedisCacheService.generateKey(PRODUCT_LIST_CACHE_PREFIX, "all");
        return advancedRedisCacheService.getWithFullProtection(cacheKey, 
                () -> productMapper.findAll(), 
                30, java.util.concurrent.TimeUnit.MINUTES);
    }

    /**
     * 获取所有商品（包括下架的，用于管理界面）
     * @return 商品列表
     */
    public List<Product> getAllProductsIncludingOffShelf() {
        String cacheKey = AdvancedRedisCacheService.generateKey(PRODUCT_LIST_CACHE_PREFIX, "all_including_off_shelf");
        return advancedRedisCacheService.getWithFullProtection(cacheKey, 
                () -> productMapper.findAllIncludingOffShelf(), 
                30, java.util.concurrent.TimeUnit.MINUTES);
    }

    /**
     * 根据ID获取商品（带缓存击穿和雪崩防护）
     * @param id 商品ID
     * @return 商品对象
     */
    public Product getProductById(Long id) {
        String cacheKey = AdvancedRedisCacheService.generateKey(PRODUCT_CACHE_PREFIX, id.toString());
        return advancedRedisCacheService.getWithFullProtection(cacheKey, 
                () -> productMapper.findById(id), 
                30, java.util.concurrent.TimeUnit.MINUTES);
    }

    /**
     * 批量获取商品（带缓存优化）
     * @param ids 商品ID列表
     * @return 商品列表
     */
    public List<Product> getProductsByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        List<Product> result = new java.util.ArrayList<>();
        List<Long> uncachedIds = new java.util.ArrayList<>();

        for (Long id : ids) {
            String cacheKey = AdvancedRedisCacheService.generateKey(PRODUCT_CACHE_PREFIX, id.toString());
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                result.add((Product) cached);
            } else {
                uncachedIds.add(id);
            }
        }

        if (!uncachedIds.isEmpty()) {
            List<Product> dbProducts = productMapper.findByIds(uncachedIds);
            for (Product product : dbProducts) {
                String cacheKey = AdvancedRedisCacheService.generateKey(PRODUCT_CACHE_PREFIX, product.getId().toString());
                redisTemplate.opsForValue().set(cacheKey, product, 30, java.util.concurrent.TimeUnit.MINUTES);
                result.add(product);
            }
        }

        return result;
    }

    /**
     * 根据分类ID获取商品列表（带缓存击穿和雪崩防护）
     * @param categoryId 分类ID
     * @return 商品列表
     */
    public List<Product> getProductsByCategory(Long categoryId) {
        String cacheKey = AdvancedRedisCacheService.generateKey(PRODUCT_CATEGORY_CACHE_PREFIX, categoryId.toString());
        return advancedRedisCacheService.getWithFullProtection(cacheKey, 
                () -> productMapper.findByCategoryId(categoryId), 
                30, java.util.concurrent.TimeUnit.MINUTES);
    }

    /**
     * 搜索商品
     * @param keyword 关键词
     * @return 商品列表
     */
    public List<Product> searchProducts(String keyword) {
        return productMapper.searchProducts(keyword);
    }

    /**
     * 添加商品
     * @param product 商品对象
     */
    public void addProduct(Product product) {
        productMapper.insert(product);
        clearProductCache();
    }

    /**
     * 更新商品
     * @param product 商品对象
     */
    public void updateProduct(Product product) {
        productMapper.update(product);
        clearProductCache();
        String cacheKey = AdvancedRedisCacheService.generateKey(PRODUCT_CACHE_PREFIX, product.getId().toString());
        advancedRedisCacheService.delete(cacheKey);
    }

    public void updateProductImage(Long id, String mainImage, String images) {
        productMapper.updateImage(id, mainImage, images);
        clearProductCache();
        String cacheKey = AdvancedRedisCacheService.generateKey(PRODUCT_CACHE_PREFIX, id.toString());
        advancedRedisCacheService.delete(cacheKey);
    }



    /**
     * 更新商品库存
     * @param id 商品ID
     * @param quantity 数量
     * @return 是否成功
     */
    public boolean updateStock(Long id, Integer quantity) {
        int result = productMapper.updateStock(id, quantity);
        if (result > 0) {
            String cacheKey = AdvancedRedisCacheService.generateKey(PRODUCT_CACHE_PREFIX, id.toString());
            advancedRedisCacheService.delete(cacheKey);
            clearProductCache();
        }
        return result > 0;
    }

    /**
     * 设置商品库存
     * @param id 商品ID
     * @param stock 库存数量
     * @return 是否成功
     */
    public boolean setStock(Long id, Integer stock) {
        int result = productMapper.setStock(id, stock);
        if (result > 0) {
            String cacheKey = AdvancedRedisCacheService.generateKey(PRODUCT_CACHE_PREFIX, id.toString());
            advancedRedisCacheService.delete(cacheKey);
            clearProductCache();
        }
        return result > 0;
    }
    
    /**
     * 清除商品列表缓存
     */
    public void clearProductCache() {
        String cacheKey1 = AdvancedRedisCacheService.generateKey(PRODUCT_LIST_CACHE_PREFIX, "all");
        String cacheKey2 = AdvancedRedisCacheService.generateKey(PRODUCT_LIST_CACHE_PREFIX, "all_including_off_shelf");
        String cacheKey3 = AdvancedRedisCacheService.generateKey(PRODUCT_LIST_CACHE_PREFIX, "recommendations");
        
        redisTemplate.delete(cacheKey1);
        redisTemplate.delete(cacheKey2);
        redisTemplate.delete(cacheKey3);
        
        advancedRedisCacheService.delete(cacheKey1);
        advancedRedisCacheService.delete(cacheKey2);
        advancedRedisCacheService.delete(cacheKey3);
    }
    
    /**
     * 清除推荐商品缓存
     */
    public void clearRecommendationsCache() {
        String cacheKey = AdvancedRedisCacheService.generateKey(PRODUCT_LIST_CACHE_PREFIX, "recommendations");
        advancedRedisCacheService.delete(cacheKey);
    }
    
    /**
     * 分页查询所有商品
     * @param pageRequest 分页请求
     * @return 分页响应
     */
    public PageResponse<Product> getAllProductsByPage(PageRequest pageRequest) {
        List<Product> products = productMapper.findAllByPage(pageRequest.getOffset(), pageRequest.getPageSize());
        Long total = productMapper.countAll();
        return PageResponse.of(products, pageRequest.getPageNum(), pageRequest.getPageSize(), total);
    }
    
    /**
     * 分页查询分类商品
     * @param categoryId 分类ID
     * @param pageRequest 分页请求
     * @return 分页响应
     */
    public PageResponse<Product> getProductsByCategoryByPage(Long categoryId, PageRequest pageRequest) {
        List<Product> products = productMapper.findByCategoryIdByPage(categoryId, pageRequest.getOffset(), pageRequest.getPageSize());
        Long total = productMapper.countByCategoryId(categoryId);
        return PageResponse.of(products, pageRequest.getPageNum(), pageRequest.getPageSize(), total);
    }
    
    /**
     * 分页搜索商品
     * @param keyword 关键词
     * @param pageRequest 分页请求
     * @return 分页响应
     */
    public PageResponse<Product> searchProductsByPage(String keyword, PageRequest pageRequest) {
        List<Product> products = productMapper.searchProductsByPage(keyword, pageRequest.getOffset(), pageRequest.getPageSize());
        Long total = productMapper.countByKeyword(keyword);
        return PageResponse.of(products, pageRequest.getPageNum(), pageRequest.getPageSize(), total);
    }

    /**
     * 根据分类ID获取分类名称
     * @param categoryId 分类ID
     * @return 分类名称
     */
    public String getCategoryName(Long categoryId) {
        if (categoryId == null) {
            return "未分类";
        }
        Category category = categoryMapper.findById(categoryId);
        return category != null ? category.getName() : "未分类";
    }

    /**
     * 上架商品
     * @param id 商品ID
     */
    public void onShelfProduct(Long id) {
        Product product = productMapper.findById(id);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        product.setStatus(ProductStatus.ON_SALE.getCode());
        productMapper.update(product);
        clearProductCache();
        String cacheKey = AdvancedRedisCacheService.generateKey(PRODUCT_CACHE_PREFIX, id.toString());
        advancedRedisCacheService.delete(cacheKey);
    }

    /**
     * 下架商品
     * @param id 商品ID
     */
    public void offShelfProduct(Long id) {
        Product product = productMapper.findById(id);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        product.setStatus(ProductStatus.OFF_SALE.getCode());
        productMapper.update(product);
        clearProductCache();
        String cacheKey = AdvancedRedisCacheService.generateKey(PRODUCT_CACHE_PREFIX, id.toString());
        advancedRedisCacheService.delete(cacheKey);
    }

    /**
     * 删除商品（硬删除）
     * @param id 商品ID
     */
    public void deleteProduct(Long id) {
        Product product = productMapper.findById(id);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        productMapper.deleteById(id);
        clearProductCache();
        String cacheKey = AdvancedRedisCacheService.generateKey(PRODUCT_CACHE_PREFIX, id.toString());
        advancedRedisCacheService.delete(cacheKey);
    }

    /**
     * 获取推荐商品（按销量排序）
     * @return 推荐商品列表
     */
    public List<Product> getRecommendations() {
        String cacheKey = AdvancedRedisCacheService.generateKey(PRODUCT_LIST_CACHE_PREFIX, "recommendations");
        return advancedRedisCacheService.getWithFullProtection(cacheKey, 
                () -> productMapper.findRecommendations(10), 
                30, java.util.concurrent.TimeUnit.MINUTES);
    }

    /**
     * 高级筛选商品
     * @param keyword 关键词
     * @param categoryId 分类ID
     * @param minPrice 最低价格
     * @param maxPrice 最高价格
     * @param sortBy 排序方式
     * @param pageRequest 分页请求
     * @return 分页响应
     */
    public PageResponse<Product> filterProducts(String keyword, Long categoryId, Double minPrice, Double maxPrice, String sortBy, PageRequest pageRequest) {
        List<Product> products = productMapper.filterProducts(keyword, categoryId, minPrice, maxPrice, sortBy, pageRequest.getOffset(), pageRequest.getPageSize());
        Long total = productMapper.countFilterProducts(keyword, categoryId, minPrice, maxPrice);
        return PageResponse.of(products, pageRequest.getPageNum(), pageRequest.getPageSize(), total);
    }
}
