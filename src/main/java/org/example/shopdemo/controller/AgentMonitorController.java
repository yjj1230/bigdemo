package org.example.shopdemo.controller;

import org.example.shopdemo.agent.monitor.AgentPerformanceMonitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Agent监控控制器
 * 提供Agent性能监控和统计信息的API
 */
@RestController
@RequestMapping("/api/agent/monitor")
public class AgentMonitorController {
    
    @Autowired
    private AgentPerformanceMonitor performanceMonitor;
    
    /**
     * 获取性能统计信息
     * @return 性能统计信息
     */
    @GetMapping("/stats")
    public String getPerformanceStats() {
        return performanceMonitor.getPerformanceStats();
    }
    
    /**
     * 重置性能统计信息
     * @return 操作结果
     */
    @PostMapping("/stats/reset")
    public String resetStats() {
        performanceMonitor.resetStats();
        return "性能统计信息已重置";
    }
}
