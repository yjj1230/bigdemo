package org.example.shopdemo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.shopdemo.annotation.RateLimiter;
import org.example.shopdemo.annotation.RequireAdmin;
import org.example.shopdemo.common.Result;
import org.example.shopdemo.dto.*;
import org.example.shopdemo.entity.Order;
import org.example.shopdemo.entity.OrderItem;
import org.example.shopdemo.entity.Product;
import org.example.shopdemo.mapper.ProductMapper;
import org.example.shopdemo.service.OrderService;
import org.example.shopdemo.utils.jwtutil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 订单控制器
 * 处理订单相关的HTTP请求
 */
@RestController
@RequestMapping("/api/order")
@Tag(name = "订单管理", description = "订单创建、查询、支付、发货等接口")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductMapper productMapper;

    /**
     * 创建订单
     * @param token JWT token
     * @param request 创建订单请求对象
     * @return 订单对象
     *  @RateLimiter自定义注解，对接口限流，60秒内允许10次请求
     *  @Parameter 是 Swagger/OpenAPI 框架提供的注解，用于描述 API 接口的参数信息，帮助生成更详细的 API 文档。
     */
    @PostMapping("/create")
    @RateLimiter(time = 60, count = 10, limitType = RateLimiter.LimitType.USER, message = "下单请求过于频繁，请稍后再试")
    @Operation(summary = "创建订单", description = "创建新订单")
    public Result<Order> createOrder(@Parameter(hidden = true) @RequestHeader("Authorization") String token, @RequestBody CreateOrderRequest request) {
        String actualToken = org.example.shopdemo.utils.jwtutil.extractToken(token);
        Long userId = org.example.shopdemo.utils.jwtutil.getUserIdFromToken(actualToken);
        Order order = orderService.createOrder(userId, request);
        return Result.success("创建订单成功", order);
    }

    /**
     * 从购物车创建订单
     * @param token JWT token
     * @param request 创建订单请求
     * @return 订单对象
     */
    @PostMapping("/createFromCart")
    @Operation(summary = "从购物车创建订单", description = "将购物车中的商品创建为订单")
    public Result<Order> createOrderFromCart(
            @Parameter(hidden = true) @RequestHeader("Authorization") String token,
            @RequestBody CreateOrderFromCartRequest request) {
        String actualToken = org.example.shopdemo.utils.jwtutil.extractToken(token);
        Long userId = org.example.shopdemo.utils.jwtutil.getUserIdFromToken(actualToken);
        Order order = orderService.createOrderFromCart(userId, request.getAddressId(), request.getRemark());
        return Result.success("创建订单成功", order);
    }

    /**
     * 根据ID获取订单
     * @param id 订单ID
     * @return 订单对象
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取订单", description = "根据订单ID获取订单详细信息")
    public Result<Order> getOrderById(@PathVariable Long id) {
        return Result.success(orderService.getOrderById(id));
    }

    /**
     * 根据订单号获取订单
     * @param orderNo 订单号
     * @return 订单对象
     */
    @GetMapping("/orderNo/{orderNo}")
    @Operation(summary = "根据订单号获取订单", description = "根据订单号查询订单")
    public Result<Order> getOrderByOrderNo(@PathVariable String orderNo) {
        return Result.success(orderService.getOrderByOrderNo(orderNo));
    }

    /**
     * 获取用户的订单列表
     * @param token JWT token
     * @return 订单列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取用户订单列表", description = "获取当前登录用户的所有订单")
    public Result<List<OrderDetailDTO>> getUserOrders(@Parameter(hidden = true) @RequestHeader("Authorization") String token) {
        String actualToken = org.example.shopdemo.utils.jwtutil.extractToken(token);
        String username = org.example.shopdemo.utils.jwtutil.getUsernameFromToken(actualToken);
        Long userId = org.example.shopdemo.utils.jwtutil.getUserIdFromToken(actualToken);
        
        List<Order> orders = orderService.getUserOrders(userId);
        List<OrderDetailDTO> orderDetails = new ArrayList<>();
        
        for (Order order : orders) {
            List<OrderItem> items = orderService.getOrderItems(order.getId());
            List<OrderItemDTO> itemDTOs = new ArrayList<>();
            
            for (OrderItem item : items) {
                Product product = productMapper.findById(item.getProductId());
                itemDTOs.add(OrderItemDTO.fromOrderItem(item, product));
            }
            
            orderDetails.add(OrderDetailDTO.fromOrder(order, itemDTOs));
        }
        
        return Result.success(orderDetails);
    }

    /**
     * 获取所有订单（管理员）
     * @return 订单列表
     */
    @GetMapping("/all")
    @RequireAdmin
    @Operation(summary = "获取所有订单", description = "获取系统中所有订单（管理员功能）")
    public Result<List<Order>> getAllOrders() {
        return Result.success(orderService.getAllOrders());
    }

    /**
     * 获取订单详情
     * @param orderId 订单ID
     * @return 订单详情列表
     */
    @GetMapping("/items/{orderId}")
    @Operation(summary = "获取订单详情", description = "获取订单的商品明细")
    public Result<List<OrderItem>> getOrderItems(@PathVariable Long orderId) {
        return Result.success(orderService.getOrderItems(orderId));
    }

    /**
     * 支付订单
     * @param orderId 订单ID
     * @return 操作结果
     */
    @PostMapping("/pay/{orderId}")
    @Operation(summary = "支付订单", description = "支付指定订单")
    public Result<Void> payOrder(@PathVariable Long orderId) {
        orderService.payOrder(orderId);
        return Result.success("支付成功", null);
    }

    /**
     * 创建微信支付订单
     * @param orderId 订单ID
     * @param openid 用户openid
     * @return 预支付ID
     */
    @PostMapping("/wechat-pay/{orderId}")
    @Operation(summary = "创建微信支付订单", description = "创建微信支付订单并返回预支付ID")
    public Result<CreatePaymentResponse> createWeChatPayment(@PathVariable Long orderId, 
                                           @RequestParam String openid) {
        CreatePaymentResponse response = orderService.createWeChatPayment(orderId, openid);
        return Result.success("创建支付订单成功", response);
    }

    /**
     * 查询微信支付状态
     * @param orderId 订单ID
     * @return 支付状态
     */
    @GetMapping("/wechat-pay/status/{orderId}")
    @Operation(summary = "查询微信支付状态", description = "查询微信支付订单的状态")
    public Result<String> queryWeChatPaymentStatus(@PathVariable Long orderId) {
        String status = orderService.queryWeChatPaymentStatus(orderId);
        return Result.success("查询成功", status);
    }

    /**
     * 发货（管理员）
     * @param orderId 订单ID
     * @return 操作结果
     */
    @PostMapping("/ship/{orderId}")
    @RequireAdmin
    @Operation(summary = "订单发货", description = "管理员对订单进行发货")
    public Result<Void> shipOrder(@PathVariable Long orderId) {
        orderService.shipOrder(orderId);
        return Result.success("发货成功", null);
    }

    /**
     * 完成订单
     * @param orderId 订单ID
     * @return 操作结果
     */
    @PostMapping("/finish/{orderId}")
    @Operation(summary = "完成订单", description = "确认收货，完成订单")
    public Result<Void> finishOrder(@PathVariable Long orderId) {
        orderService.finishOrder(orderId);
        return Result.success("订单已完成", null);
    }

    /**
     * 取消订单
     * @param orderId 订单ID
     * @return 操作结果
     */
    @PostMapping("/cancel/{orderId}")
    @Operation(summary = "取消订单", description = "取消指定订单")
    public Result<Void> cancelOrder(@PathVariable Long orderId) {
        orderService.cancelOrder(orderId);
        return Result.success("订单已取消", null);
    }

    /**
     * 获取订单支付剩余时间
     * @param orderId 订单ID
     * @return 剩余时间（秒），-1表示已超时，-2表示订单状态异常
     */
    @GetMapping("/payTime/{orderId}")
    @Operation(summary = "获取订单支付剩余时间", description = "获取订单支付剩余时间（秒）")
    public Result<Long> getPayRemainingTime(@PathVariable Long orderId) {
        long remainingTime = orderService.getPayRemainingTime(orderId);
        if (remainingTime == -1) {
            return Result.error("订单已超时，请重新下单");
        } else if (remainingTime == -2) {
            return Result.error("订单状态异常");
        }
        return Result.success(remainingTime);
    }
}
