package org.example.shopdemo.task;

import lombok.extern.slf4j.Slf4j;
import org.example.shopdemo.entity.Order;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class aitask {
    @Autowired
    private DeepSeekChatModel deepSeekChatModel;

    @Scheduled(cron = "0 0/5 * * * ?")
    @Transactional
    public void cancelTimeoutOrders() {
        log.info("开始执行订单超时检查任务");

    }

}
