package org.example.shopdemo.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.shopdemo.entity.Order;

import java.util.List;

@Mapper
public interface OrderMapper {
    int insert(Order order);
    Order findById(Long id);
    Order findByOrderNo(String orderNo);
    List<Order> findByUserId(Long userId);
    List<Order> findAll();
    int update(Order order);
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
    int deleteById(Long id);
    List<Order> findTimeoutOrders(@Param("timeoutTime") java.time.LocalDateTime timeoutTime);
    void executeSql(@Param("sql") String sql);
}
