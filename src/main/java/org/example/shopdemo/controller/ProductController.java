package org.example.shopdemo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.shopdemo.annotation.RateLimiter;
import org.example.shopdemo.annotation.RequireAdmin;
import org.example.shopdemo.common.Result;
import org.example.shopdemo.dto.PageRequest;
import org.example.shopdemo.dto.PageResponse;
import org.example.shopdemo.entity.Product;
import org.example.shopdemo.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品控制器
 * 处理商品相关的HTTP请求
 */
@RestController
@RequestMapping("/api/product")
@Tag(name = "商品管理", description = "商品查询、添加、更新、删除接口")
public class ProductController {
    @Autowired
    private ProductService productService;

    /**
     * 获取所有商品
     * @return 商品列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取所有商品", description = "获取系统中所有商品的列表")
    public Result<List<Product>> getAllProducts() {
        return Result.success(productService.getAllProducts());
    }

    /**
     * 获取所有商品（包括下架的，用于管理界面）
     * @return 商品列表
     */
    @GetMapping("/list/all")
    @RequireAdmin
    @Operation(summary = "获取所有商品（包括下架的）", description = "获取系统中所有商品（包括下架的），用于管理界面")
    public Result<List<Product>> getAllProductsIncludingOffShelf() {
        return Result.success(productService.getAllProductsIncludingOffShelf());
    }

    /**
     * 获取推荐商品
     * @return 推荐商品列表
     */
    @GetMapping("/recommendations")
    @Operation(summary = "获取推荐商品", description = "获取销量最高的推荐商品")
    public Result<List<Product>> getRecommendations() {
        return Result.success(productService.getRecommendations());
    }

    /**
     * 根据ID获取商品
     * @param id 商品ID
     * @return 商品对象
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取商品", description = "根据商品ID获取商品详细信息")
    public Result<Product> getProductById(@PathVariable Long id) {
        return Result.success(productService.getProductById(id));
    }

    /**
     * 根据分类ID获取商品列表
     * @param categoryId 分类ID
     * @return 商品列表
     */
    @GetMapping("/category/{categoryId}")
    @Operation(summary = "根据分类获取商品", description = "根据分类ID获取该分类下的所有商品")
    public Result<List<Product>> getProductsByCategory(@PathVariable Long categoryId) {
        return Result.success(productService.getProductsByCategory(categoryId));
    }

    /**
     * 搜索商品
     * @param keyword 关键词
     * @return 商品列表
     */
    @GetMapping("/search")
    @RateLimiter(time = 60, count = 30, limitType = RateLimiter.LimitType.IP, message = "搜索请求过于频繁，请稍后再试")
    @Operation(summary = "搜索商品", description = "根据关键词搜索商品")
    public Result<List<Product>> searchProducts(@RequestParam String keyword) {
        return Result.success(productService.searchProducts(keyword));
    }

    /**
     * 分页获取所有商品
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    @GetMapping("/list/page")
    @Operation(summary = "分页获取所有商品", description = "分页查询所有商品")
    public Result<PageResponse<Product>> getAllProductsByPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageRequest pageRequest = new PageRequest();
        pageRequest.setPageNum(pageNum);
        pageRequest.setPageSize(pageSize);
        return Result.success(productService.getAllProductsByPage(pageRequest));
    }

    /**
     * 分页根据分类获取商品
     * @param categoryId 分类ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    @GetMapping("/category/{categoryId}/page")
    @Operation(summary = "分页根据分类获取商品", description = "分页查询指定分类下的商品")
    public Result<PageResponse<Product>> getProductsByCategoryByPage(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageRequest pageRequest = new PageRequest();
        pageRequest.setPageNum(pageNum);
        pageRequest.setPageSize(pageSize);
        return Result.success(productService.getProductsByCategoryByPage(categoryId, pageRequest));
    }

    /**
     * 分页搜索商品
     * @param keyword 关键词
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    @GetMapping("/search/page")
    @Operation(summary = "分页搜索商品", description = "分页搜索商品")
    public Result<PageResponse<Product>> searchProductsByPage(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageRequest pageRequest = new PageRequest();
        pageRequest.setPageNum(pageNum);
        pageRequest.setPageSize(pageSize);
        return Result.success(productService.searchProductsByPage(keyword, pageRequest));
    }

    /**
     * 添加商品
     * @param product 商品对象
     * @return 操作结果
     */
    @PostMapping("/add")
    @RequireAdmin
    @Operation(summary = "添加商品", description = "添加新商品到系统")
    public Result<Void> addProduct(@RequestBody Product product) {
        productService.addProduct(product);
        return Result.success("添加成功", null);
    }

    /**
     * 更新商品
     * @param product 商品对象
     * @return 操作结果
     */
    @PutMapping("/update")
    @RequireAdmin
    @Operation(summary = "更新商品", description = "更新商品信息")
    public Result<Void> updateProduct(@RequestBody Product product) {
        productService.updateProduct(product);
        return Result.success("更新成功", null);
    }

    @PutMapping("/updateImage")
    @RequireAdmin
    @Operation(summary = "更新商品图片", description = "更新商品的图片URL")
    public Result<Void> updateProductImage(@RequestParam Long id, @RequestParam String mainImage, @RequestParam String images) {
        productService.updateProductImage(id, mainImage, images);
        return Result.success("更新成功", null);
    }

    /**
     * 删除商品
     * @param id 商品ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @RequireAdmin
    @Operation(summary = "删除商品", description = "根据ID删除商品")
    public Result<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return Result.success("删除成功", null);
    }

    /**
     * 上架商品
     * @param id 商品ID
     * @return 操作结果
     */
    @PutMapping("/onShelf/{id}")
    @RequireAdmin
    @Operation(summary = "上架商品", description = "将商品上架")
    public Result<Void> onShelfProduct(@PathVariable Long id) {
        productService.onShelfProduct(id);
        return Result.success("上架成功", null);
    }

    /**
     * 下架商品
     * @param id 商品ID
     * @return 操作结果
     */
    @PutMapping("/offShelf/{id}")
    @RequireAdmin
    @Operation(summary = "下架商品", description = "将商品下架")
    public Result<Void> offShelfProduct(@PathVariable Long id) {
        productService.offShelfProduct(id);
        return Result.success("下架成功", null);
    }
    @PutMapping("/updateStock")
    @RequireAdmin
    @Operation(summary = "更新商品库存", description = "更新商品库存数量")
    public Result<Void> updateStock(@RequestParam Long id, @RequestParam Integer stock){
        productService.setStock(id, stock);
        return Result.success("更新成功", null);
    }

    /**
     * 高级筛选商品
     * @param keyword 关键词
     * @param categoryId 分类ID
     * @param minPrice 最低价格
     * @param maxPrice 最高价格
     * @param sortBy 排序方式：price_asc, price_desc, sales_desc, time_desc
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    @GetMapping("/filter")
    @Operation(summary = "高级筛选商品", description = "根据多个条件筛选商品")
    public Result<PageResponse<Product>> filterProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false, defaultValue = "time_desc") String sortBy,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageRequest pageRequest = new PageRequest();
        pageRequest.setPageNum(pageNum);
        pageRequest.setPageSize(pageSize);
        return Result.success(productService.filterProducts(keyword, categoryId, minPrice, maxPrice, sortBy, pageRequest));
    }
}
