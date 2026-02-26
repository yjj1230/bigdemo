package org.example.shopdemo.service;

import org.example.shopdemo.entity.Logistics;
import org.example.shopdemo.mapper.LogisticsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

//物流追踪
@Service
public class LogisticsService {

    @Autowired
    private LogisticsMapper logisticsMapper;

    public Logistics createLogistics(Logistics logistics) {
        logisticsMapper.insert(logistics);
        return logistics;
    }

    public List<Logistics> getLogisticsByOrderId(Long orderId) {
        return logisticsMapper.getByOrderId(orderId);
    }

    public List<Logistics> getLogisticsByLogisticsNo(String logisticsNo) {
        return logisticsMapper.getByLogisticsNo(logisticsNo);
    }

    public Logistics updateLogistics(Logistics logistics) {
        logisticsMapper.update(logistics);
        return logistics;
    }

    public void deleteLogistics(Long id) {
        logisticsMapper.deleteById(id);
    }
}