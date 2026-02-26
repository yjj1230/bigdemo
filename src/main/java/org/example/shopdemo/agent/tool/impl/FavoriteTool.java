package org.example.shopdemo.agent.tool.impl;

import org.example.shopdemo.agent.tool.Tool;
import org.example.shopdemo.common.Result;
import org.example.shopdemo.dto.PageRequest;
import org.example.shopdemo.dto.PageResponse;
import org.example.shopdemo.entity.Product;
import org.example.shopdemo.entity.ProductFavorite;
import org.example.shopdemo.service.ProductFavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 收藏工具
 * 负责处理收藏相关的查询请求
 * 可以查询用户的收藏列表
 */
@Component
public class FavoriteTool implements Tool {
    
    /**
     * 收藏服务
     * 用于查询收藏信息
     */
    @Autowired
    private ProductFavoriteService favoriteService;
    
    /**
     * 执行收藏查询功能
     * 查询用户的收藏列表
     *
     * @param message 用户输入的消息
     * @param userId  用户ID
     * @param params  从消息中提取的参数
     * @return 执行结果
     */
    @Override
    public Result execute(String message, Long userId, Map<String, Object> params) {

        return getFavoriteList(userId);
    }
    
    /**
     * 查询收藏列表
     * @param userId 用户ID
     * @return 执行结果
     */
    private Result getFavoriteList(Long userId) {
        try {
            // 查询用户的所有收藏记录（最多20条）
            PageRequest pageRequest = new PageRequest();
            pageRequest.setPageNum(1);
            pageRequest.setPageSize(20);
            PageResponse<Product> pageResponse = favoriteService.getUserFavorites(userId, pageRequest);
            java.util.List<Product> products = pageResponse.getRecords();
            
            // 检查是否有收藏记录
            if (products == null || products.isEmpty()) {
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("message", "您还没有收藏任何商品。");
                return Result.success(responseData);
            }
            
            // 构建收藏列表的文本描述
            StringBuilder sb = new StringBuilder();
            sb.append("⭐️ 我的收藏\n");
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            
            for (Product product : products) {
                sb.append("收藏商品：").append(product.getName()).append("\n");
                sb.append("价格：¥").append(product.getPrice()).append("\n");
                sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            }
            
            // 创建响应数据
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("message", sb.toString());
            responseData.put("count", products.size());
            
            return Result.success(responseData);
            
        } catch (Exception e) {
            return Result.error("查询收藏列表时出现错误：" + e.getMessage());
        }
    }
    
    @Override
    public String getToolName() {
        return "收藏工具";
    }
    
    @Override
    public String getDescription() {
        return "查询用户的收藏列表、收藏夹";
    }
    
    @Override
    public String[] getKeywords() {
        return new String[]{"收藏", "我的收藏", "收藏夹", "查询收藏"};
    }
    
    @Override
    public boolean canHandle(String message) {
        return message.equals("FAVORITE_QUERY");
    }
}
