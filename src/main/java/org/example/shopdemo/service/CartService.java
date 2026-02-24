package org.example.shopdemo.service;

import org.example.shopdemo.dto.CartDTO;
import org.example.shopdemo.dto.CartRequest;
import org.example.shopdemo.entity.Cart;
import org.example.shopdemo.entity.Product;
import org.example.shopdemo.mapper.CartMapper;
import org.example.shopdemo.mapper.ProductMapper;
import org.example.shopdemo.utils.RedisCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 购物车服务类
 * 处理购物车相关的业务逻辑
 */
@Service
public class CartService {
    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;
    
    @Autowired
    private RedisCacheService redisCacheService;
    
    private static final String CART_CACHE_PREFIX = "cart";

    /**
     * 获取用户的购物车（带缓存）
     * @param userId 用户ID
     * @return 购物车列表
     */
    public List<CartDTO> getUserCart(Long userId) {
        String cacheKey = RedisCacheService.generateKey(CART_CACHE_PREFIX, userId.toString());
        Object cachedCart = redisCacheService.get(cacheKey);
        
        if (cachedCart != null) {
            return (List<CartDTO>) cachedCart;
        }
        
        List<Cart> cartList = cartMapper.findByUserId(userId);
        List<CartDTO> result = new ArrayList<>();
        
        for (Cart cart : cartList) {
            Product product = productMapper.findById(cart.getProductId());
            result.add(CartDTO.fromCartAndProduct(cart, product));
        }
        
        redisCacheService.set(cacheKey, result, 30, java.util.concurrent.TimeUnit.MINUTES);
        return result;
    }

    /**
     * 添加商品到购物车
     * @param userId 用户ID
     * @param request 购物车请求对象
     */
    @Transactional
    public void addToCart(Long userId, CartRequest request) {
        Product product = productMapper.findById(request.getProductId());
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        if (product.getStock() < request.getQuantity()) {
            throw new RuntimeException("库存不足");
        }

        Cart existCart = cartMapper.findByUserIdAndProductId(userId, request.getProductId());
        if (existCart != null) {
            existCart.setQuantity(existCart.getQuantity() + request.getQuantity());
            cartMapper.update(existCart);
        } else {
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setProductId(request.getProductId());
            cart.setQuantity(request.getQuantity());
            cartMapper.insert(cart);
        }
        
        clearCartCache(userId);
    }

    /**
     * 更新购物车商品数量
     * @param cartId 购物车ID
     * @param quantity 数量
     */
    public void updateCartItem(Long cartId, Integer quantity) {
        Cart cart = cartMapper.findById(cartId);
        if (cart != null) {
            cart.setQuantity(quantity);
            cartMapper.update(cart);
            clearCartCache(cart.getUserId());
        } else {
            throw new RuntimeException("购物车商品不存在");
        }
    }

    /**
     * 从购物车移除商品
     * @param cartId 购物车ID
     */
    public void removeFromCart(Long cartId) {
        Cart cart = cartMapper.findById(cartId);
        if (cart != null) {
            cartMapper.deleteById(cartId);
            clearCartCache(cart.getUserId());
        } else {
            throw new RuntimeException("购物车商品不存在");
        }
    }

    /**
     * 清空购物车
     * @param userId 用户ID
     */
    public void clearCart(Long userId) {
        cartMapper.deleteByUserId(userId);
        clearCartCache(userId);
    }
    
    /**
     * 清除购物车缓存
     * @param userId 用户ID
     */
    private void clearCartCache(Long userId) {
        String cacheKey = RedisCacheService.generateKey(CART_CACHE_PREFIX, userId.toString());
        redisCacheService.delete(cacheKey);
    }
}
