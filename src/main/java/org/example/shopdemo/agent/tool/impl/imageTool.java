package org.example.shopdemo.agent.tool.impl;

import org.example.shopdemo.agent.tool.Tool;
import org.example.shopdemo.common.Result;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.zhipuai.ZhiPuAiImageModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 图片工具
 * 负责处理图片相关的请求
 * 目前功能待实现
 */
@Component
public class imageTool implements Tool {
    @Autowired
    private ZhiPuAiImageModel zhiPuAiImageModel;
    
    /**
     * 执行图片处理功能
     *
     * @param message 用户输入的消息
     * @param userId  用户ID
     * @param params  从消息中提取的参数
     * @return 执行结果
     */
    @Override
    public Result<Map<String, Object>> execute(String message, Long userId, Map<String, Object> params) {
        ImagePrompt imagePrompt = new ImagePrompt(message);
        ImageResponse imageResponse = zhiPuAiImageModel.call(imagePrompt);
        String url=imageResponse.getResult().getOutput().getUrl();
        return Result.success(url);
    }

    /**
     * 获取工具名称
     * @return 工具名称
     */
    @Override
    public String getToolName() {
        return "图片工具";
    }

    /**
     * 获取工具描述
     * @return 工具功能描述
     */
    @Override
    public String getDescription() {
        return "图片处理工具";
    }

    /**
     * 获取工具关键词
     * @return 关键词数组
     */
    @Override
    public String[] getKeywords() {
        return new String[]{"图片", "照片","美化图片"};
    }

    /**
     * 判断工具是否能处理指定意图
     * @param message 意图类型
     * @return 是否能处理
     */
    @Override
    public boolean canHandle(String message) {
        return message.equals("Image_Query");
    }
}
