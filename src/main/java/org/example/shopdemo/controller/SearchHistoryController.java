package org.example.shopdemo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.shopdemo.common.Result;
import org.example.shopdemo.entity.SearchHistory;
import org.example.shopdemo.service.SearchHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 搜索历史控制器
 * 处理搜索历史相关的API请求
 */
@Tag(name = "搜索历史", description = "搜索历史管理接口")
@RestController
@RequestMapping("/api/search-history")
public class SearchHistoryController {

    @Autowired
    private SearchHistoryService searchHistoryService;

    /**
     * 添加搜索历史
     * @param keyword 搜索关键词
     * @param token JWT token
     * @return 操作结果
     */
    @PostMapping
    @Operation(summary = "添加搜索历史", description = "记录用户搜索关键词")
    public Result<Void> addSearchHistory(
            @Parameter(description = "搜索关键词") @RequestParam String keyword,
            @Parameter(hidden = true) @RequestHeader("Authorization") String token) {
        Long userId = getUserIdFromToken(token);
        searchHistoryService.addOrUpdateSearchHistory(userId, keyword);
        return Result.success("记录成功");
    }

    /**
     * 获取用户搜索历史
     * @param token JWT token
     * @param limit 限制数量
     * @return 搜索历史列表
     */
    @GetMapping
    @Operation(summary = "获取用户搜索历史", description = "获取当前用户的搜索历史")
    public Result<List<SearchHistory>> getUserSearchHistory(
            @Parameter(hidden = true) @RequestHeader("Authorization") String token,
            @Parameter(description = "限制数量") @RequestParam(defaultValue = "10") Integer limit) {
        Long userId = getUserIdFromToken(token);
        List<SearchHistory> historyList = searchHistoryService.getUserSearchHistory(userId, limit);
        return Result.success(historyList);
    }

    /**
     * 删除搜索历史
     * @param id 搜索历史ID
     * @param token JWT token
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除搜索历史", description = "删除指定的搜索历史记录")
    public Result<Void> deleteSearchHistory(
            @Parameter(description = "搜索历史ID") @PathVariable Long id,
            @Parameter(hidden = true) @RequestHeader("Authorization") String token) {
        searchHistoryService.deleteSearchHistory(id);
        return Result.success("删除成功");
    }

    /**
     * 清空搜索历史
     * @param token JWT token
     * @return 操作结果
     */
    @DeleteMapping("/clear")
    @Operation(summary = "清空搜索历史", description = "清空当前用户的所有搜索历史")
    public Result<Void> clearSearchHistory(
            @Parameter(hidden = true) @RequestHeader("Authorization") String token) {
        Long userId = getUserIdFromToken(token);
        searchHistoryService.clearUserSearchHistory(userId);
        return Result.success("清空成功");
    }

    /**
     * 获取热门搜索关键词
     * @param limit 限制数量
     * @return 热门搜索列表
     */
    @GetMapping("/hot")
    @Operation(summary = "获取热门搜索", description = "获取热门搜索关键词")
    public Result<List<SearchHistory>> getHotSearchKeywords(
            @Parameter(description = "限制数量") @RequestParam(defaultValue = "10") Integer limit) {
        List<SearchHistory> hotKeywords = searchHistoryService.getHotSearchKeywords(limit);
        return Result.success(hotKeywords);
    }

    /**
     * 从token中获取用户ID
     * @param token JWT token
     * @return 用户ID
     */
    private Long getUserIdFromToken(String token) {
        String tokenValue = token.replace("Bearer ", "");
        return org.example.shopdemo.utils.jwtutil.getUserIdFromToken(tokenValue);
    }
}
