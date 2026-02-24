package org.example.shopdemo.service;

import org.example.shopdemo.entity.Category;
import org.example.shopdemo.mapper.CategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商品分类服务类
 * 处理商品分类相关的业务逻辑
 */
@Service
public class CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 获取所有分类
     * @return 分类列表
     */
    public List<Category> getAllCategories() {
        return categoryMapper.findAll();
    }

    /**
     * 根据父分类ID获取子分类
     * @param parentId 父分类ID
     * @return 子分类列表
     */
    public List<Category> getCategoriesByParentId(Long parentId) {
        return categoryMapper.findByParentId(parentId);
    }

    /**
     * 根据ID获取分类
     * @param id 分类ID
     * @return 分类对象
     */
    public Category getCategoryById(Long id) {
        return categoryMapper.findById(id);
    }

    /**
     * 添加分类
     * @param category 分类对象
     */
    public void addCategory(Category category) {
        categoryMapper.insert(category);
    }

    /**
     * 更新分类
     * @param category 分类对象
     */
    public void updateCategory(Category category) {
        categoryMapper.update(category);
    }

    /**
     * 删除分类
     * @param id 分类ID
     */
    public void deleteCategory(Long id) {
        categoryMapper.deleteById(id);
    }
}
