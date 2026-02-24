package org.example.shopdemo.mapper;

import org.apache.ibatis.annotations.*;
import org.example.shopdemo.entity.UserCoupon;
import java.util.List;

/**
 * 用户优惠券Mapper接口
 */
@Mapper
public interface UserCouponMapper {

    /**
     * 插入用户优惠券
     * @param userCoupon 用户优惠券对象
     * @return 插入的行数
     */
    @Insert("INSERT INTO user_coupon (user_id, coupon_id, coupon_name, type, discount_amount, discount_rate, " +
            "min_amount, max_discount, start_time, end_time, status, receive_time) " +
            "VALUES (#{userId}, #{couponId}, #{couponName}, #{type}, #{discountAmount}, #{discountRate}, " +
            "#{minAmount}, #{maxDiscount}, #{startTime}, #{endTime}, 1, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserCoupon userCoupon);

    /**
     * 根据ID查询用户优惠券
     * @param id 用户优惠券ID
     * @return 用户优惠券对象
     */
    @Select("SELECT * FROM user_coupon WHERE id = #{id}")
    UserCoupon selectById(Long id);

    /**
     * 查询用户的所有优惠券
     * @param userId 用户ID
     * @return 用户优惠券列表
     */
    @Select("SELECT * FROM user_coupon WHERE user_id = #{userId} ORDER BY receive_time DESC")
    List<UserCoupon> selectByUserId(Long userId);

    /**
     * 查询用户可用的优惠券
     * @param userId 用户ID
     * @return 用户优惠券列表
     */
    @Select("SELECT * FROM user_coupon WHERE user_id = #{userId} AND status = 1 AND end_time >= NOW() ORDER BY receive_time DESC")
    List<UserCoupon> selectAvailableByUserId(Long userId);

    /**
     * 查询用户已使用的优惠券
     * @param userId 用户ID
     * @return 用户优惠券列表
     */
    @Select("SELECT * FROM user_coupon WHERE user_id = #{userId} AND status = 2 ORDER BY used_time DESC")
    List<UserCoupon> selectUsedByUserId(Long userId);

    /**
     * 查询用户已过期的优惠券
     * @param userId 用户ID
     * @return 用户优惠券列表
     */
    @Select("SELECT * FROM user_coupon WHERE user_id = #{userId} AND status = 3 ORDER BY receive_time DESC")
    List<UserCoupon> selectExpiredByUserId(Long userId);

    /**
     * 检查用户是否已领取优惠券
     * @param userId 用户ID
     * @param couponId 优惠券ID
     * @return 用户优惠券数量
     */
    @Select("SELECT COUNT(*) FROM user_coupon WHERE user_id = #{userId} AND coupon_id = #{couponId}")
    int countByUserAndCoupon(@Param("userId") Long userId, @Param("couponId") Long couponId);

    /**
     * 使用优惠券
     * @param id 用户优惠券ID
     * @param orderId 订单ID
     * @return 更新的行数
     */
    @Update("UPDATE user_coupon SET status = 2, used_time = NOW(), order_id = #{orderId} WHERE id = #{id}")
    int useCoupon(@Param("id") Long id, @Param("orderId") Long orderId);

    /**
     * 更新过期优惠券状态
     */
    @Update("UPDATE user_coupon SET status = 3 WHERE status = 1 AND end_time < NOW()")
    int updateExpiredCoupons();

    /**
     * 删除用户优惠券
     * @param id 用户优惠券ID
     * @return 删除的行数
     */
    @Delete("DELETE FROM user_coupon WHERE id = #{id}")
    int deleteById(Long id);
}
