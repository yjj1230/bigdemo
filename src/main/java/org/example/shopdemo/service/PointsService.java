package org.example.shopdemo.service;

import org.example.shopdemo.entity.Points;
import org.example.shopdemo.mapper.PointsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;

@Service
public class PointsService {

    @Autowired
    private PointsMapper pointsMapper;

    public Points addPoints(Points points) {
        pointsMapper.insert(points);
        return points;
    }

    public Points getById(Long id) {
        return pointsMapper.getById(id);
    }

    public List<Points> getByUserId(Long userId) {
        return pointsMapper.getByUserId(userId);
    }

    public Integer getTotalPointsByUserId(Long userId) {
        Integer total = pointsMapper.getTotalPointsByUserId(userId);
        return total != null ? total : 0;
    }

    public List<Points> getRecentByUserId(Long userId, int limit) {
        return pointsMapper.getRecentByUserId(userId, limit);
    }

    public Points addOrderPoints(Long userId, Long orderId, BigDecimal orderAmount) {
        Points points = new Points();
        points.setUserId(userId);
        points.setPoints(orderAmount.intValue());
        points.setType("购物获得");
        points.setDescription("购物获得积分");
        points.setRelatedOrderId(orderId);
        return addPoints(points);
    }

    public Points addReviewPoints(Long userId, Long productId) {
        Points points = new Points();
        points.setUserId(userId);
        points.setPoints(10);
        points.setType("评价获得");
        points.setDescription("商品评价获得10积分");
        return addPoints(points);
    }

    public Points addSignInPoints(Long userId) {
        Points points = new Points();
        points.setUserId(userId);
        points.setPoints(5);
        points.setType("签到获得");
        points.setDescription("每日签到获得5积分");
        return addPoints(points);
    }

    public Points usePoints(Long userId, Integer pointsAmount, String description, Long relatedOrderId) {
        Points points = new Points();
        points.setUserId(userId);
        points.setPoints(-pointsAmount);
        points.setType("积分消费");
        points.setDescription(description);
        points.setRelatedOrderId(relatedOrderId);
        return addPoints(points);
    }
}