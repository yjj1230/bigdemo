package org.example.shopdemo.agent.tool;

import org.example.shopdemo.common.Result;
import java.util.Map;

/**
 * 工具接口
 * 所有具体工具类都需要实现此接口
 * 定义了工具的基本行为和属性
 */
public interface Tool {
    
    /**
     * 执行工具功能
     *
     * @param message 用户输入的消息
     * @param userId  用户ID
     * @param params  从消息中提取的参数
     * @return 执行结果
     */
    Result<Map<String, Object>> execute(String message, Long userId, Map<String, Object> params);
    
    /**
     * 获取工具名称
     * @return 工具名称
     */
    String getToolName();
    
    /**
     * 获取工具描述
     * @return 工具功能描述
     */
    String getDescription();
    
    /**
     * 获取工具关键词
     * 用于意图识别时匹配用户输入
     * @return 关键词数组
     */
    String[] getKeywords();
    
    /**
     * 判断工具是否能处理指定意图
     * @param message 意图类型
     * @return 是否能处理
     */
    boolean canHandle(String message);
}
