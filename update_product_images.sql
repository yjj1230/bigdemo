-- 更新商品图片URL
-- 使用Unsplash等免费图片库的真实商品图片

-- 更新 iPhone 15 Pro Max 的图片
UPDATE product 
SET main_image = 'https://images.unsplash.com/photo-1592750475168-0f3a7660833f?w=800&h=800&fit=crop',
    images = '["https://images.unsplash.com/photo-1592750475168-0f3a7660833f?w=800&h=800&fit=crop","https://images.unsplash.com/photo-1510557880182-3d4d3cba35a5?w=800&h=800&fit=crop","https://images.unsplash.com/photo-1565849904461-04a58ad377e0?w=800&h=800&fit=crop"]'
WHERE name = 'iPhone 15 Pro Max';

-- 更新 MacBook Pro 14 的图片
UPDATE product 
SET main_image = 'https://images.unsplash.com/photo-1517336714731-489679fd1ca8?w=800&h=800&fit=crop',
    images = '["https://images.unsplash.com/photo-1517336714731-489679fd1ca8?w=800&h=800&fit=crop","https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=800&h=800&fit=crop","https://images.unsplash.com/photo-1525547719571-a2d4ac8945e2?w=800&h=800&fit=crop"]'
WHERE name = 'MacBook Pro 14';

-- 更新 男士休闲T恤 的图片
UPDATE product 
SET main_image = 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=800&h=800&fit=crop',
    images = '["https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=800&h=800&fit=crop","https://images.unsplash.com/photo-1583743814966-8936f5b7be1a?w=800&h=800&fit=crop","https://images.unsplash.com/photo-1618354691373-d851c5c3a990?w=800&h=800&fit=crop"]'
WHERE name = '男士休闲T恤';

-- 更新 女士连衣裙 的图片
UPDATE product 
SET main_image = 'https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?w=800&h=800&fit=crop',
    images = '["https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?w=800&h=800&fit=crop","https://images.unsplash.com/photo-1539008835657-9e8e9680c956?w=800&h=800&fit=crop","https://images.unsplash.com/photo-1572804013309-59a88b7e92f1?w=800&h=800&fit=crop"]'
WHERE name = '女士连衣裙';
