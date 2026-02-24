<template>
  <div class="home-container">
    <!-- 导航栏 -->
    <nav class="navbar">
      <div class="navbar-content">
        <div class="logo">
          <span class="logo-icon">🛒️</span>
          <span class="logo-text">智能商城</span>
        </div>
        <div class="nav-links">
          <router-link to="/" class="nav-link active">
            <span class="nav-icon">🏠</span>
            <span class="nav-text">首页</span>
          </router-link>
          <router-link to="/products" class="nav-link">
            <span class="nav-icon">📦</span>
            <span class="nav-text">商品</span>
          </router-link>
          <router-link to="/cart" class="nav-link">
            <span class="nav-icon">🛒</span>
            <span class="nav-text">购物车</span>
          </router-link>
          <router-link to="/orders" class="nav-link">
            <span class="nav-icon">📋</span>
            <span class="nav-text">订单</span>
          </router-link>
          <router-link to="/ai" class="nav-link">
                    <span class="nav-icon">🤖</span>
                    <span class="nav-text">AI助手</span>
                  </router-link>
                  <router-link to="/favorites" class="nav-link">
                    <span class="nav-icon">❤️</span>
                    <span class="nav-text">收藏</span>
                  </router-link>
                  <router-link to="/coupons" class="nav-link">
                    <span class="nav-icon">🎫</span>
                    <span class="nav-text">优惠券</span>
                  </router-link>
                  <router-link to="/profile" class="nav-link">
                    <span class="nav-icon">👤</span>
                    <span class="nav-text">个人中心</span>
                  </router-link>
        </div>
        <div class="user-actions">
          <div v-if="userStore.token" class="user-menu">
            <el-dropdown @command="handleCommand" class="user-dropdown">
              <div class="user-avatar">
                <span class="avatar-icon">👤</span>
                <span class="username">{{ userStore.userInfo.username }}</span>
                <el-icon class="dropdown-icon"><arrow-down /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu class="dropdown-menu">
                  <el-dropdown-item command="profile">
                    <span class="dropdown-icon">👤</span>
                    <span class="dropdown-text">个人中心</span>
                  </el-dropdown-item>
                  <el-dropdown-item command="orders">
                    <span class="dropdown-icon">📋</span>
                    <span class="dropdown-text">我的订单</span>
                  </el-dropdown-item>
                  <el-dropdown-item command="cart">
                    <span class="dropdown-icon">🛒</span>
                    <span class="dropdown-text">购物车</span>
                  </el-dropdown-item>
                  <el-dropdown-item divided command="logout">
                    <span class="dropdown-icon">🚪</span>
                    <span class="dropdown-text">退出登录</span>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
          <router-link v-else to="/login" class="login-btn">
            <span class="login-icon">🔐</span>
            <span class="login-text">登录</span>
          </router-link>
        </div>
      </div>
    </nav>

    <!-- 主要内容 -->
    <main class="main-content">
      <!-- 轮播图区域 -->
      <section class="hero-section">
        <div class="carousel-container">
          <el-carousel height="450px" :interval="5000" indicator-position="outside" arrow="always">
            <el-carousel-item v-for="item in banners" :key="item.id" class="carousel-item">
              <div class="carousel-content">
                <img :src="item.image" :alt="item.title" class="carousel-image" />
                <div class="carousel-overlay">
                  <h2 class="carousel-title">{{ item.title }}</h2>
                  <p class="carousel-desc">{{ item.desc }}</p>
                </div>
              </div>
            </el-carousel-item>
          </el-carousel>
        </div>
      </section>

      <!-- 热门商品区域 -->
      <section class="products-section">
        <div class="section-header">
          <div class="header-content">
            <span class="section-icon">🔥</span>
            <h2 class="section-title">热门商品</h2>
            <router-link to="/products" class="view-all-btn">
              <span>查看全部</span>
              <el-icon><arrow-right /></el-icon>
            </router-link>
          </div>
        </div>
        <div class="products-grid">
          <div 
            v-for="product in hotProducts" 
            :key="product.id" 
            class="product-card"
            @click="goToProduct(product.id)"
          >
            <div class="product-image-wrapper">
              <img :src="product.mainImage" :alt="product.name" class="product-image" />
              <div class="product-overlay">
                <el-button type="primary" size="small" class="quick-add-btn" @click.stop="addToCart(product)">
                  <span class="btn-icon">+</span>
                </el-button>
              </div>
            </div>
            <div class="product-info">
              <h3 class="product-name">{{ product.name }}</h3>
              <div class="product-meta">
                <span class="price">¥{{ product.price }}</span>
                <span class="sales">已售 {{ product.sales || 0 }}</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 推荐商品区域 -->
      <section class="products-section">
        <div class="section-header">
          <div class="header-content">
            <span class="section-icon">⭐</span>
            <h2 class="section-title">推荐商品</h2>
            <router-link to="/products" class="view-all-btn">
              <span>查看全部</span>
              <el-icon><arrow-right /></el-icon>
            </router-link>
          </div>
        </div>
        <div class="products-grid">
          <div 
            v-for="product in recommendProducts" 
            :key="product.id" 
            class="product-card"
            @click="goToProduct(product.id)"
          >
            <div class="product-image-wrapper">
              <img :src="product.mainImage" :alt="product.name" class="product-image" />
              <div class="product-overlay">
                <el-button type="primary" size="small" class="quick-add-btn" @click.stop="addToCart(product)">
                  <span class="btn-icon">+</span>
                </el-button>
              </div>
            </div>
            <div class="product-info">
              <h3 class="product-name">{{ product.name }}</h3>
              <div class="product-meta">
                <span class="price">¥{{ product.price }}</span>
                <span class="sales">已售 {{ product.sales || 0 }}</span>
              </div>
            </div>
          </div>
        </div>
      </section>
    </main>

    <!-- 页脚 -->
    <footer class="footer">
      <div class="footer-content">
        <div class="footer-section">
          <h4 class="footer-title">关于我们</h4>
          <p class="footer-text">智能商城，为您提供优质的购物体验</p>
        </div>
        <div class="footer-section">
          <h4 class="footer-title">快速链接</h4>
          <div class="footer-links">
            <router-link to="/" class="footer-link">首页</router-link>
            <router-link to="/products" class="footer-link">商品</router-link>
            <router-link to="/cart" class="footer-link">购物车</router-link>
            <router-link to="/orders" class="footer-link">订单</router-link>
          </div>
        </div>
        <div class="footer-section">
          <h4 class="footer-title">联系我们</h4>
          <p class="footer-text">客服热线：400-123-4567</p>
          <p class="footer-text">邮箱：support@smartshop.com</p>
        </div>
      </div>
      <div class="footer-bottom">
        <p>&copy; 2024 智能商城 版权所有</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown, ArrowRight } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getRecommendations } from '@/api/product'
import { addToCart as addToCartApi } from '@/api/cart'

const router = useRouter()
const userStore = useUserStore()

const banners = ref([
  { 
    id: 1, 
    title: '春季大促', 
    desc: '全场商品低至5折起',
    image: 'https://images.unsplash.com/photo-1607082349520-0523f9f2997?w=1200&h=450&fit=crop' 
  },
  { 
    id: 2, 
    title: '新品上市', 
    desc: '最新科技产品，抢先体验',
    image: 'https://images.unsplash.com/photo-1517336714731-489629fd58ba?w=1200&h=450&fit=crop' 
  },
  { 
    id: 3, 
    title: '限时优惠', 
    desc: '精选商品，限时特惠',
    image: 'https://images.unsplash.com/photo-1556742049-0cfed4f6a45?w=1200&h=450&fit=crop' 
  }
])

const hotProducts = ref([])
const recommendProducts = ref([])

onMounted(async () => {
  try {
    const res = await getRecommendations()
    recommendProducts.value = res
    hotProducts.value = res.slice(0, 4)
  } catch (error) {
    ElMessage.error('获取商品信息失败')
  }
})

const addToCart = async (product) => {
  if (!userStore.token) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  try {
    await addToCartApi({ productId: product.id, quantity: 1 })
    ElMessage.success('已加入购物车')
  } catch (error) {
    ElMessage.error('加入购物车失败')
  }
}

const goToProduct = (productId) => {
  router.push(`/product/${productId}`)
}

const handleCommand = async (command) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'orders':
      router.push('/orders')
      break
    case 'cart':
      router.push('/cart')
      break
    case 'logout':
      try {
        await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        userStore.logout()
        ElMessage.success({ message: '退出成功', duration: 800 })
        router.push('/login')
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error({ message: '退出失败', duration: 800 })
        }
      }
      break
  }
}
</script>

<style scoped>
/* 引入字体 */
@import url('https://fonts.googleapis.com/css2?family=Orbitron:wght@400;500;600;700&family=Noto+Sans+SC:wght@300;400;500;600&display=swap');

/* CSS变量定义 */
:root {
  --primary-color: #00ff88;
  --secondary-color: #00d4ff;
  --accent-color: #ff6b9d;
  --bg-dark: #0a0e27;
  --bg-card: #1a1f3a;
  --bg-lighter: #252b4d;
  --text-primary: #ffffff;
  --text-secondary: #a0a8c0;
  --border-color: #2d3561;
  --shadow-color: rgba(0, 255, 136, 0.1);
  --gradient-1: linear-gradient(135deg, #00ff88 0%, #00d4ff 100%);
  --gradient-2: linear-gradient(135deg, #ff6b9d 0%, #00d4ff 100%);
}

.home-container {
  min-height: 100vh;
  background: linear-gradient(180deg, var(--bg-dark) 0%, #0f142e 100%);
  font-family: 'Noto Sans SC', sans-serif;
}

/* 导航栏 */
.navbar {
  background: rgba(26, 31, 58, 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid var(--border-color);
  position: sticky;
  top: 0;
  z-index: 1000;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
}

.navbar-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 16px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  font-size: 28px;
}

.logo-text {
  font-family: 'Orbitron', sans-serif;
  font-size: 20px;
  font-weight: 700;
  background: var(--gradient-1);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.nav-links {
  display: flex;
  gap: 8px;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 16px;
  background: var(--bg-lighter);
  border-radius: 8px;
  text-decoration: none;
  color: var(--text-secondary);
  font-weight: 500;
  transition: all 0.3s ease;
  border: 1px solid transparent;
}

.nav-link:hover,
.nav-link.active {
  background: linear-gradient(135deg, rgba(0, 255, 136, 0.15) 0%, rgba(0, 212, 255, 0.15) 100%);
  border-color: var(--primary-color);
  color: var(--text-primary);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px var(--shadow-color);
}

.nav-link.active {
  background: var(--gradient-1);
  border-color: var(--primary-color);
}

.nav-icon {
  font-size: 18px;
  transition: transform 0.3s ease;
}

.nav-link:hover .nav-icon {
  transform: scale(1.2) rotate(10deg);
}

.nav-text {
  font-size: 14px;
  font-weight: 500;
  letter-spacing: 0.5px;
}

.user-actions {
  display: flex;
  align-items: center;
}

.user-menu {
  margin-right: 16px;
}

.user-dropdown {
  cursor: pointer;
}

.user-avatar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: var(--bg-lighter);
  border-radius: 8px;
  border: 1px solid var(--border-color);
  transition: all 0.3s ease;
}

.user-avatar:hover {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px var(--shadow-color);
}

.avatar-icon {
  font-size: 20px;
}

.username {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.dropdown-icon {
  color: var(--text-secondary);
  font-size: 14px;
}

.dropdown-menu {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3);
}

.dropdown-icon {
  margin-right: 8px;
  font-size: 16px;
}

.dropdown-text {
  font-size: 14px;
  color: var(--text-primary);
}

.login-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: var(--gradient-1);
  border-radius: 8px;
  text-decoration: none;
  color: var(--text-primary);
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px var(--shadow-color);
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px var(--shadow-color);
}

.login-icon {
  font-size: 18px;
}

.login-text {
  font-size: 14px;
  font-weight: 600;
}

/* 主要内容 */
.main-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 40px 24px;
}

/* 轮播图区域 */
.hero-section {
  margin-bottom: 50px;
}

.carousel-container {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
}

.carousel-item {
  position: relative;
}

.carousel-content {
  position: relative;
  width: 100%;
  height: 100%;
}

.carousel-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.carousel-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 40px;
  background: linear-gradient(to top, rgba(10, 14, 39, 0.95) 0%, rgba(10, 14, 39, 0.7) 100%);
}

.carousel-title {
  font-family: 'Orbitron', sans-serif;
  font-size: 32px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 12px;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.5);
}

.carousel-desc {
  font-size: 16px;
  color: var(--text-secondary);
  margin: 0;
}

/* 商品区域 */
.products-section {
  margin-bottom: 60px;
}

.section-header {
  margin-bottom: 30px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-icon {
  font-size: 32px;
  margin-right: 12px;
}

.section-title {
  font-family: 'Orbitron', sans-serif;
  font-size: 28px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
  display: flex;
  align-items: center;
}

.view-all-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: var(--bg-lighter);
  border-radius: 6px;
  text-decoration: none;
  color: var(--text-secondary);
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  border: 1px solid transparent;
}

.view-all-btn:hover {
  background: var(--gradient-1);
  border-color: var(--primary-color);
  color: var(--text-primary);
  transform: translateX(4px);
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
}

/* 商品卡片 */
.product-card {
  background: var(--bg-card);
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid var(--border-color);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  position: relative;
}

.product-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 16px 48px rgba(0, 255, 136, 0.2);
  border-color: var(--primary-color);
}

.product-image-wrapper {
  position: relative;
  width: 100%;
  padding-top: 75%;
  overflow: hidden;
}

.product-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.product-card:hover .product-image {
  transform: scale(1.05);
}

.product-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.product-card:hover .product-overlay {
  opacity: 1;
}

.quick-add-btn {
  background: var(--gradient-1);
  border: none;
  border-radius: 50%;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.quick-add-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 8px 24px var(--shadow-color);
}

.btn-icon {
  font-size: 24px;
  font-weight: bold;
}

.product-info {
  padding: 20px;
}

.product-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 12px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.price {
  font-family: 'Orbitron', sans-serif;
  font-size: 20px;
  font-weight: 700;
  color: var(--primary-color);
  text-shadow: 0 0 10px var(--shadow-color);
}

.sales {
  font-size: 13px;
  color: var(--text-secondary);
}

/* 页脚 */
.footer {
  background: var(--bg-card);
  border-top: 1px solid var(--border-color);
  padding: 40px 24px;
  margin-top: 60px;
}

.footer-content {
  max-width: 1400px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 40px;
}

.footer-section {
  margin-bottom: 20px;
}

.footer-title {
  font-family: 'Orbitron', sans-serif;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 16px;
}

.footer-text {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.8;
  margin: 8px 0;
}

.footer-links {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.footer-link {
  color: var(--text-secondary);
  text-decoration: none;
  font-size: 14px;
  transition: all 0.3s ease;
}

.footer-link:hover {
  color: var(--primary-color);
  transform: translateX(4px);
}

.footer-bottom {
  text-align: center;
  padding-top: 40px;
  border-top: 1px solid var(--border-color);
  color: var(--text-secondary);
  font-size: 13px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .navbar-content,
  .main-content,
  .footer-content {
    max-width: 100%;
  }
  
  .products-grid {
    grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  }
}

@media (max-width: 768px) {
  .navbar-content {
    padding: 12px 16px;
  }
  
  .nav-links {
    display: none;
  }
  
  .logo-text {
    font-size: 18px;
  }
  
  .main-content {
    padding: 24px 16px;
  }
  
  .carousel-title {
    font-size: 24px;
  }
  
  .section-title {
    font-size: 22px;
  }
  
  .products-grid {
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 16px;
  }
  
  .footer-content {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 480px) {
  .navbar-content {
    padding: 10px 12px;
  }
  
  .logo-icon {
    font-size: 24px;
  }
  
  .logo-text {
    font-size: 16px;
  }
  
  .carousel-title {
    font-size: 20px;
  }
  
  .carousel-desc {
    font-size: 14px;
  }
  
  .section-icon {
    font-size: 24px;
  }
  
  .section-title {
    font-size: 18px;
  }
  
  .products-grid {
    grid-template-columns: 1fr;
  }
}
</style>
