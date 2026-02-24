package org.example.shopdemo.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.shopdemo.entity.SearchHistory;

import java.util.List;

@Mapper
public interface SearchHistoryMapper {
    int insert(SearchHistory searchHistory);
    List<SearchHistory> findByUserId(@Param("userId") Long userId, @Param("limit") Integer limit);
    int updateSearchHistory(@Param("userId") Long userId, @Param("keyword") String keyword);
    int deleteById(Long id);
    int clearByUserId(Long userId);
    List<SearchHistory> getHotSearchKeywords(@Param("limit") Integer limit);
}
