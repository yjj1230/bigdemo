package org.example.shopdemo.service;

import org.example.shopdemo.entity.SearchHistory;
import org.example.shopdemo.mapper.SearchHistoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 搜索历史服务类
 * 处理搜索历史相关的业务逻辑
 */
@Service
public class SearchHistoryService {

    @Autowired
    private SearchHistoryMapper searchHistoryMapper;

    /**
     * 添加或更新搜索历史
     * @param userId 用户ID
     * @param keyword 搜索关键词
     */
    @Transactional
    public void addOrUpdateSearchHistory(Long userId, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return;
        }

        keyword = keyword.trim();
        int updateResult = searchHistoryMapper.updateSearchHistory(userId, keyword);
        
        if (updateResult == 0) {
            SearchHistory searchHistory = new SearchHistory();
            searchHistory.setUserId(userId);
            searchHistory.setKeyword(keyword);
            searchHistory.setSearchCount(1);
            searchHistoryMapper.insert(searchHistory);
        }
    }

    /**
     * 获取用户搜索历史
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 搜索历史列表
     */
    public List<SearchHistory> getUserSearchHistory(Long userId, Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }
        return searchHistoryMapper.findByUserId(userId, limit);
    }

    /**
     * 删除搜索历史
     * @param id 搜索历史ID
     */
    @Transactional
    public void deleteSearchHistory(Long id) {
        searchHistoryMapper.deleteById(id);
    }

    /**
     * 清空用户搜索历史
     * @param userId 用户ID
     */
    @Transactional
    public void clearUserSearchHistory(Long userId) {
        searchHistoryMapper.clearByUserId(userId);
    }

    /**
     * 获取热门搜索关键词
     * @param limit 限制数量
     * @return 热门搜索列表
     */
    public List<SearchHistory> getHotSearchKeywords(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }
        return searchHistoryMapper.getHotSearchKeywords(limit);
    }
}
