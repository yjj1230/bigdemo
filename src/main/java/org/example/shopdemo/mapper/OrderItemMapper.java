package org.example.shopdemo.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.shopdemo.entity.OrderItem;

import java.util.List;

/**
 * 订单项映射器接口，用于操作订单项相关的数据库操作
 */
@Mapper
public interface OrderItemMapper {
    /**
     * 插入一个新的订单项
     * @param orderItem 要插入的订单项对象
     * @return 插入成功的记录数
     */
    int insert(OrderItem orderItem);

    /**
     * 根据订单ID查询订单项列表
     * @param orderId 订单ID
     * @return 订单项列表
     */
    List<OrderItem> findByOrderId(Long orderId);

    /**
     * 根据产品ID查询订单项列表
     * @param productId 产品ID
     * @return 订单项列表
     */
    List<OrderItem> findByProductId(Long productId);

    /**
     * 根据订单ID删除订单项
     * @param orderId 订单ID
     * @return 删除的记录数
     */
    int deleteByOrderId(Long orderId);

    /**
     * 检查用户是否购买过指定商品
     * @param userId 用户ID
     * @param productId 商品ID
     * @return 如果购买过返回true，否则返回false
     */
    boolean hasUserPurchasedProduct(@Param("userId") Long userId, @Param("productId") Long productId);
}
