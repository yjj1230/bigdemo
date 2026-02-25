package org.example.shopdemo.agent.model;

import lombok.Data;

import java.util.Map;

/**
 * 意图模型类
 * 用于表示从用户消息中识别出的意图信息
 */
@Data
public class Intent {
    
    /**
     * 意图类型
     * 例如：ORDER_QUERY（查询订单）、LOGISTICS_QUERY（查询物流）等
     */
    private String type;
    
    /**
     * 意图描述
     * 对意图的详细说明
     */
    private String description;
    
    /**
     * 意图参数
     * 从用户消息中提取的参数，如订单ID、关键词等
     */
    private Map<String, Object> params;
    
    /**
     * 置信度
     * 表示意图识别的可信程度，范围0-1
     */
    private double confidence;
    
    /**
     * 无参构造函数
     */
    public Intent() {
    }
    
    /**
     * 全参构造函数
     * @param type 意图类型
     * @param description 意图描述
     * @param params 意图参数
     * @param confidence 置信度
     */
    public Intent(String type, String description, Map<String, Object> params, double confidence) {
        this.type = type;
        this.description = description;
        this.params = params;
        this.confidence = confidence;
    }
}
