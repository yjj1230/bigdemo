package org.example.shopdemo.agent;

import org.springframework.ai.zhipuai.api.ZhiPuAiApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Rage {
    @Autowired
    private ZhiPuAiApi.EmbeddingModel embeddingModel;
    String message="电影太好看了啊啊啊啊，下次我还来看。";
    String message1="苹果太好吃了啊啊啊啊，下次我还来吃。";
    String message2="床太好睡了啊啊啊啊，下次我还来睡。";
    public String getEmbedding(String message) {

        return "";
    }

}
