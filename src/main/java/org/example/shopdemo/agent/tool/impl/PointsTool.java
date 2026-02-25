package org.example.shopdemo.agent.tool.impl;

import org.example.shopdemo.agent.tool.Tool;
import org.example.shopdemo.common.Result;
import org.example.shopdemo.entity.Points;
import org.example.shopdemo.service.PointsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 积分工具
 * 负责处理积分相关的查询请求
 * 可以查询积分余额和积分记录
 */
@Component
public class PointsTool implements Tool {
    
    /**
     * 积分服务
     * 用于处理积分相关的业务逻辑
     */
    @Autowired
    private PointsService pointsService;
    
    /**
     * 执行积分功能
     * 目前只支持查询积分，使用积分功能请在订单结算页面使用
     *
     * @param message 用户输入的消息
     * @param userId  用户ID
     * @param params  从消息中提取的参数
     * @return 执行结果
     */
    @Override
    public Result<Map<String, Object>> execute(String message, Long userId, Map<String, Object> params) {
        // 判断用户是想查询积分还是使用积分
        if (message.contains("用") || message.contains("抵扣") || message.contains("兑换")) {
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("message", "积分使用功能请在订单结算页面使用，目前暂不支持直接使用。");
            return Result.success(responseData);
        } else {
            // 查询积分信息
            return getPointsInfo(userId);
        }
    }
    
    /**
     * 查询积分信息
     * @param userId 用户ID
     * @return 执行结果
     */
    private Result<Map<String, Object>> getPointsInfo(Long userId) {
        try {
            // 查询用户的积分记录
            List<Points> pointsList = pointsService.getByUserId(userId);
            
            // 检查积分记录是否存在
            if (pointsList == null || pointsList.isEmpty()) {
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("message", "您还没有积分信息。");
                return Result.success(responseData);
            }
            
            // 计算总积分
            Integer totalPoints = pointsService.getTotalPointsByUserId(userId);
            
            // 构建积分信息的文本描述
            StringBuilder sb = new StringBuilder();
            sb.append("💰 我的积分\n");
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            sb.append("当前积分：").append(totalPoints).append("\n");
            sb.append("总记录数：").append(pointsList.size()).append("\n");
            
            // 查询最近的积分记录
            List<Points> recentPoints = pointsService.getRecentByUserId(userId, 5);
            
            // 如果有积分记录，显示最近的5条记录
            if (recentPoints != null && !recentPoints.isEmpty()) {
                sb.append("\n最近积分记录：\n");
                for (Points record : recentPoints) {
                    sb.append("  ").append(record.getCreateTime()).append("\n");
                    sb.append("  ").append(record.getPoints() >= 0 ? "+" : "")
                      .append(record.getPoints()).append(" ")
                      .append(record.getType()).append(" - ")
                      .append(record.getDescription()).append("\n");
                    sb.append("  ───────────────────────────\n");
                }
            }
            
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            
            // 创建响应数据
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("message", sb.toString());
            
            // 返回响应
            return Result.success(responseData);
            
        } catch (Exception e) {
            // 捕获异常并返回友好的错误信息
            return Result.error("查询积分信息失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取工具名称
     * @return 工具名称
     */
    @Override
    public String getToolName() {
        return "积分工具";
    }
    
    /**
     * 获取工具描述
     * @return 工具功能描述
     */
    @Override
    public String getDescription() {
        return "查询积分余额、积分记录";
    }
    
    /**
     * 获取工具关键词
     * @return 关键词数组
     */
    @Override
    public String[] getKeywords() {
        return new String[]{"积分", "我的积分", "积分查询", "积分余额"};
    }
    
    /**
     * 判断工具是否能处理指定意图
     * @param message 意图类型
     * @return 是否能处理
     */
    @Override
    public boolean canHandle(String message) {
        return message.contains("积分") || message.contains("用") || message.contains("抵扣") || message.contains("兑换");
    }
}
