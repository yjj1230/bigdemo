package org.example.shopdemo.utils;

import org.example.shopdemo.entity.Product;
import org.example.shopdemo.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 商品图片更新工具 - 扩展版
 * 为所有商品更新真实图片
 * ommandLineRunner是 Spring Boot 的一个函数式接口，具有以下特点：
 * 核心功能：
 * 当 Spring Boot 应用启动完成后，会自动执行实现了该接口的类中的 run() 方法
 * 常用于执行应用启动后的初始化任务
 * 使用场景：
 * 数据库初始化
 * 缓存预加载
 * 配置文件处理
 * 批量数据处理
 *
 * @PostConstruct：在依赖注入完成后，执行初始化操作
 *
 * 实际应用场景选择：
 * 使用 @PostConstruct 适合：
 * 简单的初始化任务
 * 不依赖其他复杂 Bean 的场景
 * 需要尽早执行的任务
 * 使用 CommandLineRunner 适合：
 * 需要等待整个应用完全启动后执行
 * 需要访问命令行参数
 * 需要控制执行顺序的多个初始化任务
 * 更复杂的启动后处理逻辑
 * 在你的商品图片更新场景中，CommandLineRunner 是更好的选择，因为它确保了：
 * 数据库连接已经建立
 * 所有相关 Bean 都已初始化完成
 * 应用已经准备好处理业务逻辑
 * 这就是为什么你的代码选择了 CommandLineRunner 而不是 @PostConstruct。
 */
@Component
public class ProductImageUpdaterExtended implements CommandLineRunner {

    @Autowired
    private ProductMapper productMapper;

    @Override
    public void run(String... args) throws Exception {
        updateAllProductImages();
    }

    /**
     * 更新所有商品的图片
     */
    private void updateAllProductImages() {
        System.out.println("开始更新所有商品图片...");

        List<Product> products = productMapper.findAll();
        int updatedCount = 0;
        
        for (Product product : products) {
            String name = product.getName();
            String mainImage = product.getMainImage();
            
            // 检查是否需要更新
            boolean needsUpdate = (mainImage == null || 
                               mainImage.trim().isEmpty() ||
                               mainImage.contains("example.com") ||
                               mainImage.contains("placeholder.com") ||
                               mainImage.contains("via.placeholder"));
            
            if (needsUpdate) {
                String newMainImage = getImageForProduct(name);
                String newImages = getImagesForProduct(name);
                
                if (newMainImage != null) {
                    productMapper.updateImage(product.getId(), newMainImage, newImages);
                    System.out.println("✓ 已更新 " + name + " 的图片");
                    updatedCount++;
                } else {
                    System.out.println("✗ 未找到 " + name + " 的合适图片");
                }
            } else {
                System.out.println("○ " + name + " 已有真实图片，跳过");
            }
        }
        
        System.out.println("\n========== 更新完成 ==========");
        System.out.println("总商品数: " + products.size());
        System.out.println("已更新商品数: " + updatedCount);
        System.out.println("==================================\n");
    }

    /**
     * 根据商品名称获取主图URL
     */
    private String getImageForProduct(String productName) {
        if (productName.contains("iPhone") || productName.contains("苹果")) {
            return "https://images.unsplash.com/photo-1592750475168-0f3a7660833f?w=800&h=800&fit=crop";
        } else if (productName.contains("MacBook") || productName.contains("笔记本") || productName.contains("电脑")) {
            return "https://images.unsplash.com/photo-1517336714731-489679fd1ca8?w=800&h=800&fit=crop";
        } else if (productName.contains("T恤") || productName.contains("T-shirt") || productName.contains("男装")) {
            return "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=800&h=800&fit=crop";
        } else if (productName.contains("连衣裙") || productName.contains("女装")) {
            return "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?w=800&h=800&fit=crop";
        } else if (productName.contains("小米") || productName.contains("手机")) {
            return "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=800&h=800&fit=crop";
        } else if (productName.contains("耳机") || productName.contains("耳机")) {
            return "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800&h=800&fit=crop";
        } else if (productName.contains("手表") || productName.contains("Watch")) {
            return "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=800&h=800&fit=crop";
        } else if (productName.contains("相机") || productName.contains("Camera")) {
            return "https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=800&h=800&fit=crop";
        } else if (productName.contains("包") || productName.contains("Bag")) {
            return "https://images.unsplash.com/photo-1548036328-c9fa89d128fa?w=800&h=800&fit=crop";
        } else if (productName.contains("鞋") || productName.contains("Shoe")) {
            return "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800&h=800&fit=crop";
        } else if (productName.contains("眼镜") || productName.contains("Glasses")) {
            return "https://images.unsplash.com/photo-1572635196237-14b3f281503f?w=800&h=800&fit=crop";
        } else if (productName.contains("键盘") || productName.contains("Keyboard")) {
            return "https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=800&h=800&fit=crop";
        } else if (productName.contains("鼠标") || productName.contains("Mouse")) {
            return "https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?w=800&h=800&fit=crop";
        } else if (productName.contains("音响") || productName.contains("Speaker")) {
            return "https://images.unsplash.com/photo-1543512214-318c77a7f5da?w=800&h=800&fit=crop";
        } else if (productName.contains("平板") || productName.contains("Tablet")) {
            return "https://images.unsplash.com/photo-1544244015-0df4b3ffc6b?w=800&h=800&fit=crop";
        } else if (productName.contains("充电器") || productName.contains("Charger")) {
            return "https://images.unsplash.com/photo-1586816879360-004f5b0c51e?w=800&h=800&fit=crop";
        } else if (productName.contains("数据线") || productName.contains("Cable")) {
            return "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=800&h=800&fit=crop";
        } else {
            return "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=800&h=800&fit=crop";
        }
    }

    /**
     * 根据商品名称获取图片数组
     */
    private String getImagesForProduct(String productName) {
        String mainImage = getImageForProduct(productName);
        return "[\"" + mainImage + "\"]";
    }
}
