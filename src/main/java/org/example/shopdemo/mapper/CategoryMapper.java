package org.example.shopdemo.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.shopdemo.entity.Category;

import java.util.List;

@Mapper
public interface CategoryMapper {
    int insert(Category category);
    Category findById(Long id);
    List<Category> findAll();
    List<Category> findByParentId(Long parentId);
    int update(Category category);
    int deleteById(Long id);
}
