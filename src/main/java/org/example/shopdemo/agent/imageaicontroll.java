package org.example.shopdemo.agent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.zhipuai.ZhiPuAiImageModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/imageai")
public class imageaicontroll {
    @Autowired
    private ZhiPuAiImageModel zhiPuAiImageModel;
    @RequestMapping("/image")
  public String imageaicontroll(String prompt) {
        ImagePrompt imagePrompt = new ImagePrompt(prompt);
        ImageResponse imageResponse = zhiPuAiImageModel.call(imagePrompt);
        String imageUrl = imageResponse.getResult().getOutput().getUrl();
        System.out.println(imageUrl);
      return "";
  }
}
