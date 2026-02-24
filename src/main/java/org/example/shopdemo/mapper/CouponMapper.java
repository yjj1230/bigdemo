package org.example.shopdemo.mapper;

import org.apache.ibatis.annotations.*;
import org.example.shopdemo.entity.Coupon;
import java.util.List;

/**
 * 优惠券Mapper接口
 */
@Mapper
public interface CouponMapper {

    /**
     * 插入优惠券
     * @param coupon 优惠券对象
     * @return 插入的行数
     */
    @Insert("INSERT INTO coupon (name, description, type, discount_amount, discount_rate, min_amount, max_discount, " +
            "total_count, received_count, used_count, limit_per_user, start_time, end_time, status, create_time, update_time) " +
            "VALUES (#{name}, #{description}, #{type}, #{discountAmount}, #{discountRate}, #{minAmount}, #{maxDiscount}, " +
            "#{totalCount}, #{receivedCount}, #{usedCount}, #{limitPerUser}, #{startTime}, #{endTime}, #{status}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Coupon coupon);

    /**
     * 根据ID查询优惠券
     * @param id 优惠券ID
     * @return 优惠券对象
     */
    @Select("SELECT * FROM coupon WHERE id = #{id}")
    Coupon selectById(Long id);

    /**
     * 查询所有可用优惠券
     * @return 优惠券列表
     */
    @Select("SELECT * FROM coupon WHERE status = 2 AND start_time <= NOW() AND end_time >= NOW() ORDER BY create_time DESC")
    List<Coupon> selectAvailableCoupons();

    /**
     * 查询用户可领取的优惠券
     * @param userId 用户ID
     * @return 优惠券列表
     */
    @Select("SELECT c.* FROM coupon c " +
            "WHERE c.status = 2 AND c.start_time <= NOW() AND c.end_time >= NOW() " +
            "AND c.received_count < c.total_count " +
            "AND (SELECT COUNT(*) FROM user_coupon WHERE user_id = #{userId} AND coupon_id = c.id) < c.limit_per_user " +
            "ORDER BY c.create_time DESC")
    List<Coupon> selectAvailableCouponsForUser(Long userId);

    /**
     * 更新优惠券
     * @param coupon 优惠券对象
     * @return 更新的行数
     */
    @Update("UPDATE coupon SET name = #{name}, description = #{description}, type = #{type}, " +
            "discount_amount = #{discountAmount}, discount_rate = #{discountRate}, min_amount = #{minAmount}, " +
            "max_discount = #{maxDiscount}, total_count = #{totalCount}, limit_per_user = #{limitPerUser}, " +
            "start_time = #{startTime}, end_time = #{endTime}, status = #{status}, update_time = NOW() " +
            "WHERE id = #{id}")
    int update(Coupon coupon);

    /**
     * 增加已领取数量
     * @param id 优惠券ID
     * @return 更新的行数
     */
    @Update("UPDATE coupon SET received_count = received_count + 1 WHERE id = #{id}")
    int incrementReceivedCount(Long id);

    /**
     * 增加已使用数量
     * @param id 优惠券ID
     * @return 更新的行数
     */
    @Update("UPDATE coupon SET used_count = used_count + 1 WHERE id = #{id}")
    int incrementUsedCount(Long id);

    /**
     * 删除优惠券
     * @param id 优惠券ID
     * @return 删除的行数
     */
    @Delete("DELETE FROM coupon WHERE id = #{id}")
    int deleteById(Long id);

    /**
     * 更新优惠券状态
     */
    @Update("UPDATE coupon SET status = CASE " +
            "WHEN start_time > NOW() THEN 1 " +
            "WHEN end_time < NOW() THEN 3 " +
            "ELSE 2 END, update_time = NOW()")
    int updateCouponStatus();
}
