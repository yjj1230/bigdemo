package org.example.shopdemo.agent.tool.impl;

import org.example.shopdemo.agent.tool.Tool;
import org.example.shopdemo.common.Result;
import org.example.shopdemo.entity.Coupon;
import org.example.shopdemo.entity.UserCoupon;
import org.example.shopdemo.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 优惠券工具
 * 负责处理优惠券相关的请求
 * 可以领取优惠券和查看优惠券列表
 */
@Component
public class CouponTool implements Tool {
    
    /**
     * 优惠券服务
     * 用于处理优惠券相关的业务逻辑
     */
    @Autowired
    private CouponService couponService;
    
    /**
     * 执行优惠券功能
     * 根据消息内容决定是领取优惠券还是查看优惠券列表
     *
     * @param message 用户输入的消息
     * @param userId  用户ID
     * @param params  从消息中提取的参数
     * @return 执行结果
     */
    @Override
    public Result<Map<String, Object>> execute(String message, Long userId, Map<String, Object> params) {
        // 判断用户是想领取优惠券还是查看优惠券列表
        if (message.contains("领") || message.contains("领券") || message.contains("领优惠")) {
            return receiveCoupon(userId);
        } else {
            return getCouponList(userId);
        }
    }
    
    /**
     * 领取优惠券
     * @param userId 用户ID
     * @return 执行结果
     */
    private Result<Map<String, Object>> receiveCoupon(Long userId) {
        try {
            // 查询可领取的优惠券
            List<Coupon> availableCoupons = couponService.getAvailableCouponsForUser(userId);
            
            // 检查是否有可领取的优惠券
            if (availableCoupons == null || availableCoupons.isEmpty()) {
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("message", "当前没有可领取的优惠券。");
                return Result.success(responseData);
            }
            
            // 领取第一个可用的优惠券
            Coupon coupon = availableCoupons.get(0);
            Long userCouponId = couponService.receiveCoupon(userId, coupon.getId());
            
            // 检查领取是否成功
            if (userCouponId != null) {
                // 构建领取成功的文本描述
                StringBuilder sb = new StringBuilder();
                sb.append("🎉 领取成功！\n");
                sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                sb.append("优惠券：").append(coupon.getName()).append("\n");
                sb.append("类型：").append(getCouponTypeText(coupon.getType())).append("\n");
                
                // 根据优惠券类型显示不同的优惠信息
                if (coupon.getType() == 1) {
                    sb.append("优惠金额：¥").append(coupon.getDiscountAmount()).append("\n");
                } else if (coupon.getType() == 2) {
                    sb.append("折扣：").append(coupon.getDiscountRate().multiply(new java.math.BigDecimal(10))).append("折\n");
                }
                
                sb.append("最低消费：¥").append(coupon.getMinAmount()).append("\n");
                sb.append("有效期：").append(coupon.getStartTime()).append(" 至 ").append(coupon.getEndTime()).append("\n");
                sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                
                // 创建响应数据
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("message", sb.toString());
                responseData.put("navigationType", "coupons");
                responseData.put("navigationParams", new HashMap<>());
                
                // 返回包含跳转信息的响应
                return Result.success(responseData);
            } else {
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("message", "领取优惠券失败，可能已达到领取上限。");
                return Result.success(responseData);
            }
            
        } catch (Exception e) {
            // 捕获异常并返回友好的错误信息
            return Result.error("领取优惠券失败：" + e.getMessage());
        }
    }
    
    /**
     * 查询优惠券列表
     * @param userId 用户ID
     * @return 执行结果
     */
    private Result<Map<String, Object>> getCouponList(Long userId) {
        try {
            // 查询用户的优惠券列表
            List<UserCoupon> userCoupons = couponService.getUserCoupons(userId);
            
            // 检查优惠券列表是否为空
            if (userCoupons == null || userCoupons.isEmpty()) {
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("message", "您还没有任何优惠券。快去领取吧！");
                return Result.success(responseData);
            }
            
            // 构建优惠券列表的文本描述
            StringBuilder sb = new StringBuilder();
            sb.append("🎫 我的优惠券\n");
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            
            // 遍历优惠券列表，显示每个优惠券的信息
            for (UserCoupon userCoupon : userCoupons) {
                sb.append("优惠券：").append(userCoupon.getCouponName()).append("\n");
                sb.append("状态：").append(getCouponStatusText(userCoupon.getStatus())).append("\n");
                
                // 根据优惠券类型显示不同的优惠信息
                if (userCoupon.getType() == 1) {
                    sb.append("优惠金额：¥").append(userCoupon.getDiscountAmount()).append("\n");
                } else if (userCoupon.getType() == 2) {
                    sb.append("折扣：").append(userCoupon.getDiscountRate().multiply(new java.math.BigDecimal(10))).append("折\n");
                }
                
                sb.append("有效期：").append(userCoupon.getStartTime()).append(" 至 ").append(userCoupon.getEndTime()).append("\n");
                sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            }
            
            // 创建响应数据
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("message", sb.toString());
            responseData.put("navigationType", "coupons");
            responseData.put("navigationParams", new HashMap<>());
            
            // 返回包含跳转信息的响应
            return Result.success(responseData);
            
        } catch (Exception e) {
            // 捕获异常并返回友好的错误信息
            return Result.error("查询优惠券列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取优惠券类型的文本描述
     * @param type 优惠券类型码
     * @return 优惠券类型的文本描述
     */
    private String getCouponTypeText(Integer type) {
        switch (type) {
            case 1: return "满减券";
            case 2: return "折扣券";
            case 3: return "免运费券";
            default: return "未知";
        }
    }
    
    /**
     * 获取优惠券状态的文本描述
     * @param status 优惠券状态码
     * @return 优惠券状态的文本描述
     */
    private String getCouponStatusText(Integer status) {
        switch (status) {
            case 0: return "已使用";
            case 1: return "可使用";
            case 2: return "已过期";
            default: return "未知";
        }
    }
    
    /**
     * 获取工具名称
     * @return 工具名称
     */
    @Override
    public String getToolName() {
        return "优惠券工具";
    }
    
    /**
     * 获取工具描述
     * @return 工具功能描述
     */
    @Override
    public String getDescription() {
        return "领取优惠券、查看优惠券列表";
    }
    
    /**
     * 获取工具关键词
     * @return 关键词数组
     */
    @Override
    public String[] getKeywords() {
        return new String[]{"领券", "领优惠", "优惠券", "优惠卷", "我的券", "可用券"};
    }
    
    /**
     * 判断工具是否能处理指定意图
     * @param message 意图类型
     * @return 是否能处理
     */
    @Override
    public boolean canHandle(String message) {
        return message.equals("COUPON_RECEIVE") || message.equals("COUPON_LIST");
    }
}
