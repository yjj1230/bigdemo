package org.example.shopdemo.service;

import org.example.shopdemo.dto.CreateOrderRequest;
import org.example.shopdemo.dto.CreatePaymentResponse;
import org.example.shopdemo.dto.OrderItemRequest;
import org.example.shopdemo.entity.*;
import org.example.shopdemo.enums.OrderStatus;
import org.example.shopdemo.mapper.*;
import org.example.shopdemo.utils.RedisCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单服务类
 * 处理订单相关的业务逻辑
 */
@Service
public class OrderService {
    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ProductService productService;

    @Autowired
    private AddressMapper addressMapper;

    @Autowired
    private CartMapper cartMapper;
    
    @Autowired
    private RedisCacheService redisCacheService;
    
    @Autowired
    private StockService stockService;
    
    @Autowired
    private WebSocketMessageService webSocketMessageService;
    
    @Autowired
    private CouponService couponService;
    
    @Autowired
    private UserCouponMapper userCouponMapper;
    
    @Autowired
    private CouponMapper couponMapper;
    
    @Autowired
    private WeChatPayService weChatPayService;
    
    private static final String ORDER_CACHE_PREFIX = "order";
    private static final String ORDER_LIST_CACHE_PREFIX = "order_list";
    private static final String ORDER_ITEM_CACHE_PREFIX = "order_item";
    private static final int ORDER_TIMEOUT_MINUTES = 15;

    /**
     * 验证并计算优惠券折扣
     * @param userId 用户ID
     * @param couponId 优惠券ID
     * @param orderAmount 订单金额
     * @return 折扣金额
     */
    private BigDecimal validateAndCalculateCouponDiscount(Long userId, Long couponId, BigDecimal orderAmount) {
        if (couponId == null) {
            return BigDecimal.ZERO;
        }
        
        UserCoupon userCoupon = userCouponMapper.selectById(couponId);
        if (userCoupon == null) {
            throw new RuntimeException("优惠券不存在");
        }
        
        if (!userCoupon.getUserId().equals(userId)) {
            throw new RuntimeException("优惠券不属于当前用户");
        }
        
        if (userCoupon.getStatus() != 1) {
            throw new RuntimeException("优惠券不可用");
        }
        
        Coupon coupon = couponMapper.selectById(userCoupon.getCouponId());
        if (coupon == null) {
            throw new RuntimeException("优惠券信息不存在");
        }
        
        if (coupon.getStatus() != 2) {
            throw new RuntimeException("优惠券已失效");
        }
        
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getStartTime()) || now.isAfter(coupon.getEndTime())) {
            throw new RuntimeException("优惠券不在有效期内");
        }
        
        BigDecimal minAmount = coupon.getMinAmount();
        if (minAmount != null && orderAmount.compareTo(minAmount) < 0) {
            throw new RuntimeException("未达到最低消费金额：" + minAmount);
        }
        
        BigDecimal discount = couponService.calculateDiscount(userCoupon, orderAmount);
        
        if (discount.compareTo(orderAmount) >= 0) {
            throw new RuntimeException("优惠券金额不能大于订单金额");
        }
        
        return discount;
    }

    /**
     * 创建订单
     * @param userId 用户ID
     * @param request 订单请求对象
     * @return 订单对象
     */
    @Transactional
    public Order createOrder(Long userId, CreateOrderRequest request) {
        BigDecimal originalAmount = request.getTotalAmount();
        BigDecimal couponDiscount = validateAndCalculateCouponDiscount(userId, request.getCouponId(), originalAmount);
        BigDecimal payAmount = originalAmount.subtract(couponDiscount);
        
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setCouponId(request.getCouponId());
        order.setOriginalAmount(originalAmount);
        order.setCouponDiscount(couponDiscount);
        order.setTotalAmount(originalAmount);
        order.setPayAmount(payAmount);
        order.setStatus(OrderStatus.PENDING_PAYMENT.getCode());
        order.setReceiverName(request.getReceiverName());
        order.setReceiverPhone(request.getReceiverPhone());
        order.setReceiverAddress(request.getReceiverAddress());
        order.setRemark(request.getRemark());
        orderMapper.insert(order);
        
        for (OrderItemRequest itemRequest : request.getItems()) {
            Product product = productMapper.findById(itemRequest.getProductId());
            if (product == null) {
                throw new RuntimeException("商品不存在");
            }
            
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getMainImage());
            orderItem.setPrice(product.getPrice());
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setTotalPrice(product.getPrice().multiply(new BigDecimal(itemRequest.getQuantity())));
            orderItemMapper.insert(orderItem);
            
            productMapper.increaseSales(product.getId(), itemRequest.getQuantity());
        }
        
        if (request.getCouponId() != null) {
            couponService.useCoupon(request.getCouponId(), order.getId());
        }
        
        clearOrderCache(order.getId(), userId);
        productService.clearRecommendationsCache();
        return order;
    }

    /**
     * 从购物车创建订单
     * @param userId 用户ID
     * @param addressId 地址ID
     * @param couponId 优惠券ID
     * @param remark 订单备注
     * @return 订单对象
     */
    @Transactional
    public Order createOrderFromCart(Long userId, Long addressId, Long couponId, String remark) {
        System.out.println("=== 开始创建订单 ===");
        System.out.println("用户ID: " + userId + ", 地址ID: " + addressId + ", 优惠券ID: " + couponId);
        
        List<Cart> cartItems = cartMapper.findByUserId(userId);
        System.out.println("购物车商品数量: " + cartItems.size());
        
        if (cartItems.isEmpty()) {
            throw new RuntimeException("购物车为空");
        }

        Address address = addressMapper.findById(addressId);
        if (address == null) {
            throw new RuntimeException("收货地址不存在");
        }
        if (address.getReceiverName() == null || address.getReceiverPhone() == null || 
            address.getProvince() == null || address.getCity() == null || 
            address.getDistrict() == null || address.getDetailAddress() == null) {
            throw new RuntimeException("收货地址信息不完整");
        }

        BigDecimal originalAmount = BigDecimal.ZERO;
        java.util.List<StockService.StockItem> stockItems = new ArrayList<>();
        
        for (Cart cart : cartItems) {
            Product product = productMapper.findById(cart.getProductId());
            if (product == null) {
                throw new RuntimeException("商品不存在");
            }
            if (product.getPrice() == null) {
                throw new RuntimeException("商品价格未设置：" + product.getName());
            }
            Integer currentStock = stockService.getStock(cart.getProductId());
            if (currentStock < cart.getQuantity()) {
                throw new RuntimeException("商品库存不足：" + product.getName());
            }
            originalAmount = originalAmount.add(product.getPrice().multiply(new BigDecimal(cart.getQuantity())));
            stockItems.add(new StockService.StockItem(cart.getProductId(), cart.getQuantity()));
        }

        System.out.println("订单原始金额: " + originalAmount);
        
        BigDecimal couponDiscount = validateAndCalculateCouponDiscount(userId, couponId, originalAmount);
        System.out.println("优惠券折扣: " + couponDiscount);
        
        BigDecimal payAmount = originalAmount.subtract(couponDiscount);
        System.out.println("实际支付金额: " + payAmount);

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setCouponId(couponId);
        order.setOriginalAmount(originalAmount);
        order.setCouponDiscount(couponDiscount);
        order.setTotalAmount(originalAmount);
        order.setPayAmount(payAmount);
        order.setStatus(OrderStatus.PENDING_PAYMENT.getCode());
        order.setReceiverName(address.getReceiverName());
        order.setReceiverPhone(address.getReceiverPhone());
        order.setReceiverAddress(address.getProvince() + address.getCity() + address.getDistrict() + address.getDetailAddress());
        order.setRemark(remark);
        orderMapper.insert(order);
        System.out.println("订单插入成功，订单ID: " + order.getId());
        
        boolean deductSuccess = stockService.batchDeductStock(stockItems);
        if (!deductSuccess) {
            throw new RuntimeException("库存扣减失败，请重试");
        }
        System.out.println("库存扣减成功");

        for (Cart cart : cartItems) {
            Product product = productMapper.findById(cart.getProductId());
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getMainImage());
            orderItem.setPrice(product.getPrice());
            orderItem.setQuantity(cart.getQuantity());
            orderItem.setTotalPrice(product.getPrice().multiply(new BigDecimal(cart.getQuantity())));
            orderItemMapper.insert(orderItem);
            
            productMapper.increaseSales(product.getId(), cart.getQuantity());
        }
        System.out.println("订单项插入成功，销量更新成功");
        
        if (couponId != null) {
            couponService.useCoupon(couponId, order.getId());
            System.out.println("优惠券使用成功");
        }
        
        cartMapper.deleteByUserId(userId);
        System.out.println("购物车清空成功");
        
        clearOrderCache(order.getId(), userId);
        productService.clearRecommendationsCache();
        System.out.println("=== 订单创建完成 ===");
        return order;
    }

    /**
     * 根据ID获取订单
     * @param id 订单ID
     * @return 订单对象
     */
    public Order getOrderById(Long id) {
        String cacheKey = RedisCacheService.generateKey(ORDER_CACHE_PREFIX, id.toString());
        Object cachedOrder = redisCacheService.get(cacheKey);
        
        if (cachedOrder != null) {
            return (Order) cachedOrder;
        }
        
        Order order = orderMapper.findById(id);
        if (order != null) {
            redisCacheService.set(cacheKey, order, ORDER_TIMEOUT_MINUTES, java.util.concurrent.TimeUnit.MINUTES);
        }
        return order;
    }

    /**
     * 根据订单号获取订单
     * @param orderNo 订单号
     * @return 订单对象
     */
    public Order getOrderByOrderNo(String orderNo) {
        String cacheKey = RedisCacheService.generateKey(ORDER_CACHE_PREFIX, orderNo);
        Object cachedOrder = redisCacheService.get(cacheKey);
        
        if (cachedOrder != null) {
            return (Order) cachedOrder;
        }
        
        Order order = orderMapper.findByOrderNo(orderNo);
        if (order != null) {
            redisCacheService.set(cacheKey, order, ORDER_TIMEOUT_MINUTES, java.util.concurrent.TimeUnit.MINUTES);
        }
        return order;
    }

    /**
     * 获取用户的订单列表
     * @param userId 用户ID
     * @return 订单列表
     */
    public List<Order> getUserOrders(Long userId) {
        String cacheKey = RedisCacheService.generateKey(ORDER_LIST_CACHE_PREFIX, userId.toString());
        Object cachedOrders = redisCacheService.get(cacheKey);
        
        if (cachedOrders != null) {
            return (List<Order>) cachedOrders;
        }
        
        List<Order> orders = orderMapper.findByUserId(userId);
        redisCacheService.set(cacheKey, orders, ORDER_TIMEOUT_MINUTES, java.util.concurrent.TimeUnit.MINUTES);
        return orders;
    }

    /**
     * 获取所有订单（管理员）
     * @return 订单列表
     */
    public List<Order> getAllOrders() {
        String cacheKey = RedisCacheService.generateKey(ORDER_LIST_CACHE_PREFIX, "all");
        Object cachedOrders = redisCacheService.get(cacheKey);
        
        if (cachedOrders != null) {
            return (List<Order>) cachedOrders;
        }
        
        List<Order> orders = orderMapper.findAll();
        redisCacheService.set(cacheKey, orders, ORDER_TIMEOUT_MINUTES, java.util.concurrent.TimeUnit.MINUTES);
        return orders;
    }

    /**
     * 获取订单项
     * @param orderId 订单ID
     * @return 订单项列表
     */
    public List<OrderItem> getOrderItems(Long orderId) {
        String cacheKey = RedisCacheService.generateKey(ORDER_ITEM_CACHE_PREFIX, orderId.toString());
        Object cachedItems = redisCacheService.get(cacheKey);
        
        if (cachedItems != null) {
            return (List<OrderItem>) cachedItems;
        }
        
        List<OrderItem> items = orderItemMapper.findByOrderId(orderId);
        redisCacheService.set(cacheKey, items, ORDER_TIMEOUT_MINUTES, java.util.concurrent.TimeUnit.MINUTES);
        return items;
    }

    /**
     * 支付订单
     * @param orderId 订单ID
     */
    @Transactional
    public void payOrder(Long orderId) {
        Order order = orderMapper.findById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT.getCode()) {
            throw new RuntimeException("订单状态不正确");
        }
        
        order.setStatus(OrderStatus.PAID.getCode());
        orderMapper.update(order);
        
        clearOrderCache(orderId, order.getUserId());
        // 直接更新订单状态，不使用消息队列
        webSocketMessageService.sendOrderStatusUpdate(order.getUserId(), order);
    }

    /**
     * 发货
     * @param orderId 订单ID
     */
    @Transactional
    public void shipOrder(Long orderId) {
        Order order = orderMapper.findById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getStatus() != OrderStatus.PAID.getCode()) {
            throw new RuntimeException("订单状态不正确");
        }
        
        order.setStatus(OrderStatus.SHIPPED.getCode());
        orderMapper.update(order);
        
        clearOrderCache(orderId, order.getUserId());
        // 直接更新订单状态，不使用消息队列
        webSocketMessageService.sendOrderStatusUpdate(order.getUserId(), order);
    }

    /**
     * 确认收货
     * @param orderId 订单ID
     */
    @Transactional
    public void confirmOrder(Long orderId) {
        Order order = orderMapper.findById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getStatus() != OrderStatus.SHIPPED.getCode()) {
            throw new RuntimeException("订单状态不正确");
        }
        
        order.setStatus(OrderStatus.COMPLETED.getCode());
        orderMapper.update(order);
        
        clearOrderCache(orderId, order.getUserId());
        // 直接更新订单状态，不使用消息队列
        webSocketMessageService.sendOrderStatusUpdate(order.getUserId(), order);
    }

    /**
     * 取消订单
     * @param orderId 订单ID
     */
    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = orderMapper.findById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT.getCode()) {
            throw new RuntimeException("订单状态不正确");
        }
        
        order.setStatus(OrderStatus.CANCELLED.getCode());
        orderMapper.update(order);
        
        // 恢复库存
        List<OrderItem> items = orderItemMapper.findByOrderId(orderId);
        for (OrderItem item : items) {
            stockService.increaseStock(item.getProductId(), item.getQuantity());
        }
        
        clearOrderCache(orderId, order.getUserId());
        // 直接更新订单状态，不使用消息队列
        webSocketMessageService.sendOrderStatusUpdate(order.getUserId(), order);
    }

    /**
     * 删除订单
     * @param orderId 订单ID
     */
    @Transactional
    public void deleteOrder(Long orderId) {
        Order order = orderMapper.findById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        orderItemMapper.deleteByOrderId(orderId);
        orderMapper.deleteById(orderId);
        
        clearOrderCache(orderId, order.getUserId());
    }

    /**
     * 更新订单状态
     * @param orderId 订单ID
     * @param status 新状态
     */
    @Transactional
    public void updateOrderStatus(Long orderId, Integer status) {
        Order order = orderMapper.findById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        order.setStatus(status);
        orderMapper.update(order);
        
        clearOrderCache(orderId, order.getUserId());
        // 直接更新订单状态，不使用消息队列
        webSocketMessageService.sendOrderStatusUpdate(order.getUserId(), order);
    }

    /**
     * 超时取消订单
     * @param orderId 订单ID
     */
    @Transactional
    public void timeoutCancelOrder(Long orderId) {
        Order order = orderMapper.findById(orderId);
        if (order == null) {
            return;
        }
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT.getCode()) {
            return;
        }
        
        order.setStatus(OrderStatus.CANCELLED.getCode());
        orderMapper.update(order);
        
        // 恢复库存
        List<OrderItem> items = orderItemMapper.findByOrderId(orderId);
        for (OrderItem item : items) {
            stockService.increaseStock(item.getProductId(), item.getQuantity());
        }
        
        clearOrderCache(orderId, order.getUserId());
    }

    /**
     * 清除订单相关缓存
     * @param orderId 订单ID
     * @param userId 用户ID
     */
    private void clearOrderCache(Long orderId, Long userId) {
        redisCacheService.delete(RedisCacheService.generateKey(ORDER_CACHE_PREFIX, orderId.toString()));
        redisCacheService.delete(RedisCacheService.generateKey(ORDER_LIST_CACHE_PREFIX, userId.toString()));
        redisCacheService.delete(RedisCacheService.generateKey(ORDER_LIST_CACHE_PREFIX, "all"));
        redisCacheService.delete(RedisCacheService.generateKey(ORDER_ITEM_CACHE_PREFIX, orderId.toString()));
    }

    /**
     * 创建微信支付订单
     * @param orderId 订单ID
     * @param openid 用户openid
     * @return 支付订单信息
     */
    public CreatePaymentResponse createWeChatPayment(Long orderId, String openid) {
        Order order = orderMapper.findById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT.getCode()) {
            throw new RuntimeException("订单状态不正确");
        }
        
        String prepayId = weChatPayService.createPayment(orderId, order.getOrderNo(), order.getPayAmount(), openid);
        CreatePaymentResponse response = new CreatePaymentResponse();
        response.setPrepayId(prepayId);
        return response;
    }

    /**
     * 查询微信支付状态
     * @param orderId 订单ID
     * @return 支付状态
     */
    public String queryWeChatPaymentStatus(Long orderId) {
        Order order = orderMapper.findById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        return weChatPayService.queryPaymentStatus(order.getOrderNo());
    }

    /**
     * 完成订单
     * @param orderId 订单ID
     */
    public void finishOrder(Long orderId) {
        confirmOrder(orderId);
    }

    /**
     * 获取订单支付剩余时间
     * @param orderId 订单ID
     * @return 剩余时间（秒），-1表示已超时，-2表示订单状态异常
     */
    public long getPayRemainingTime(Long orderId) {
        Order order = orderMapper.findById(orderId);
        if (order == null) {
            return -2;
        }
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT.getCode()) {
            return -2;
        }
        
        LocalDateTime createTime = order.getCreateTime();
        LocalDateTime timeoutTime = createTime.plusMinutes(15);
        LocalDateTime now = LocalDateTime.now();
        
        if (now.isAfter(timeoutTime)) {
            return -1;
        }
        
        return java.time.Duration.between(now, timeoutTime).getSeconds();
    }

    /**
     * 生成订单号
     * @return 订单号
     */
    private String generateOrderNo() {
        return "ORD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
    }
}
