package org.example.shopdemo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.shopdemo.annotation.RequireAdmin;
import org.example.shopdemo.common.Result;
import org.example.shopdemo.entity.Category;
import org.example.shopdemo.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品分类控制器
 * 处理商品分类相关的HTTP请求
 */
@RestController
@RequestMapping("/api/category")
@Tag(name = "分类管理", description = "商品分类的查询、添加、更新、删除接口")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 获取所有分类
     * @return 分类列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取所有分类", description = "获取系统中所有商品分类")
    public Result<List<Category>> getAllCategories() {
        return Result.success(categoryService.getAllCategories());
    }

    /**
     * 根据ID获取分类
     * @param id 分类ID
     * @return 分类对象
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取分类", description = "根据分类ID获取分类详细信息")
    public Result<Category> getCategoryById(@PathVariable Long id) {
        return Result.success(categoryService.getCategoryById(id));
    }

    /**
     * 根据父分类ID获取子分类
     * @param parentId 父分类ID
     * @return 子分类列表
     */
    @GetMapping("/parent/{parentId}")
    @Operation(summary = "获取子分类", description = "根据父分类ID获取所有子分类")
    public Result<List<Category>> getCategoriesByParentId(@PathVariable Long parentId) {
        return Result.success(categoryService.getCategoriesByParentId(parentId));
    }

    /**
     * 添加分类
     * @param category 分类对象
     * @return 操作结果
     */
    @PostMapping("/add")
    @RequireAdmin
    @Operation(summary = "添加分类", description = "添加新的商品分类")
    public Result<Void> addCategory(@RequestBody Category category) {
        categoryService.addCategory(category);
        return Result.success("添加成功", null);
    }

    /**
     * 更新分类
     * @param category 分类对象
     * @return 操作结果
     */
    @PutMapping("/update")
    @RequireAdmin
    @Operation(summary = "更新分类", description = "更新商品分类信息")
    public Result<Void> updateCategory(@RequestBody Category category) {
        categoryService.updateCategory(category);
        return Result.success("更新成功", null);
    }

    /**
     * 删除分类
     * @param id 分类ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @RequireAdmin
    @Operation(summary = "删除分类", description = "根据ID删除商品分类")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return Result.success("删除成功", null);
    }
}
