<template>
  <div class="favorites-container">
    <!-- 导航栏 -->
    <nav class="navbar">
      <div class="navbar-content">
        <div class="logo">
          <span class="logo-icon">🛒️</span>
          <span class="logo-text">智能商城</span>
        </div>
        <div class="nav-links">
          <router-link to="/" class="nav-link">
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
      <!-- 页面标题 -->
      <section class="page-header">
        <div class="header-content">
          <span class="page-icon">❤️</span>
          <h1 class="page-title">我的收藏</h1>
          <span class="item-count">{{ favorites.length }} 件商品</span>
        </div>
      </section>

      <!-- 空收藏状态 -->
      <div v-if="loading" class="loading-state">
        <el-icon class="is-loading"><loading /></el-icon>
        <span>加载中...</span>
      </div>

      <div v-else-if="favorites.length === 0" class="empty-state">
        <div class="empty-content">
          <div class="empty-icon">❤️</div>
          <h2 class="empty-title">收藏夹是空的</h2>
          <p class="empty-desc">快去收藏喜欢的商品吧</p>
          <router-link to="/products" class="shop-btn">
            <span class="btn-icon">🛍️</span>
            <span class="btn-text">去逛逛</span>
          </router-link>
        </div>
      </div>

      <!-- 收藏商品列表 -->
      <div v-else class="favorites-grid">
        <div 
          v-for="item in favorites" 
          :key="item.id" 
          class="favorite-card"
        >
          <div class="product-image-wrapper">
            <img 
              :src="item.product.mainImage || item.product.image" 
              :alt="item.product.name"
              class="product-image"
              @error="handleImageError"
            />
            <div class="product-overlay">
              <div class="overlay-actions">
                <el-button 
                  type="primary" 
                  size="small" 
                  class="quick-add-btn" 
                  @click="addToCart(item.product)"
                >
                  <span class="btn-icon">🛒</span>
                  <span class="btn-text">加入购物车</span>
                </el-button>
                <el-button 
                  type="danger" 
                  size="small" 
                  class="remove-btn" 
                  @click="removeFavorite(item.product.id)"
                >
                  <span class="btn-icon">🗑️</span>
                  <span class="btn-text">取消收藏</span>
                </el-button>
              </div>
            </div>
          </div>
          <div class="product-info">
            <h3 class="product-name">{{ item.product.name }}</h3>
            <div class="product-meta">
              <span class="price">¥{{ item.product.price }}</span>
              <span class="stock">库存: {{ item.product.stock || 0 }}</span>
            </div>
            <div class="product-actions">
              <el-button 
                type="primary" 
                size="small" 
                @click="goToProduct(item.product.id)"
                class="view-btn"
              >
                查看详情
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Loading, ArrowDown } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getFavorites, removeFavorite as apiRemoveFavorite } from '@/api/product'
import { addToCart as apiAddToCart } from '@/api/cart'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const favorites = ref([])

const loadFavorites = async () => {
  loading.value = true
  try {
    const response = await getFavorites()
    if (response.code === 200) {
      favorites.value = response.data || []
    }
  } catch (error) {
    console.error('加载收藏失败:', error)
    ElMessage.error('加载收藏失败')
  } finally {
    loading.value = false
  }
}

const removeFavorite = async (productId) => {
  try {
    const response = await apiRemoveFavorite(productId)
    if (response.code === 200) {
      ElMessage.success('取消收藏成功')
      favorites.value = favorites.value.filter(item => item.product.id !== productId)
    } else {
      ElMessage.error(response.message || '取消收藏失败')
    }
  } catch (error) {
    console.error('取消收藏失败:', error)
    ElMessage.error('取消收藏失败')
  }
}

const addToCart = async (product) => {
  try {
    const response = await apiAddToCart({
      productId: product.id,
      quantity: 1
    })
    if (response.code === 200) {
      ElMessage.success('已加入购物车')
    } else {
      ElMessage.error(response.message || '加入购物车失败')
    }
  } catch (error) {
    console.error('加入购物车失败:', error)
    ElMessage.error('加入购物车失败')
  }
}

const goToProduct = (productId) => {
  router.push(`/product/${productId}`)
}

const handleImageError = (event) => {
  event.target.src = 'https://via.placeholder.com/300x300?text=No+Image'
}

const handleCommand = (command) => {
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
      userStore.logout()
      router.push('/login')
      break
  }
}

onMounted(() => {
  loadFavorites()
})
</script>

<style scoped>
.favorites-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  color: #e0e0e0;
}

.navbar {
  background: rgba(26, 26, 46, 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(0, 255, 255, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
  padding: 12px 0;
}

.navbar-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
}

.logo-icon {
  font-size: 32px;
}

.logo-text {
  font-size: 24px;
  font-weight: bold;
  background: linear-gradient(135deg, #00ff88, #00d4ff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.nav-links {
  display: flex;
  gap: 24px;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 8px;
  text-decoration: none;
  color: #c0c0c0;
  transition: all 0.3s;
}

.nav-link:hover {
  background: rgba(0, 255, 255, 0.1);
  color: #00ff88;
  transform: translateY(-2px);
}

.nav-link.active {
  background: rgba(0, 255, 255, 0.15);
  color: #00ff88;
}

.nav-icon {
  font-size: 20px;
}

.nav-text {
  font-size: 14px;
}

.user-actions {
  display: flex;
  align-items: center;
}

.user-dropdown {
  cursor: pointer;
}

.user-avatar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: rgba(0, 255, 255, 0.1);
  border: 1px solid rgba(0, 255, 255, 0.2);
  border-radius: 20px;
  transition: all 0.3s;
}

.user-avatar:hover {
  background: rgba(0, 255, 255, 0.2);
  border-color: rgba(0, 255, 255, 0.4);
}

.avatar-icon {
  font-size: 20px;
}

.username {
  font-size: 14px;
  color: #e0e0e0;
}

.dropdown-icon {
  color: #00ff88;
}

.login-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 20px;
  background: linear-gradient(135deg, #00ff88, #00d4ff);
  border: none;
  border-radius: 20px;
  text-decoration: none;
  color: #1a1a2e;
  font-weight: 500;
  transition: all 0.3s;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 255, 136, 0.3);
}

.login-icon {
  font-size: 18px;
}

.login-text {
  font-size: 14px;
}

.main-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px 20px;
}

.page-header {
  margin-bottom: 32px;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.page-icon {
  font-size: 36px;
}

.page-title {
  font-size: 28px;
  font-weight: bold;
  color: #e0e0e0;
}

.item-count {
  padding: 4px 12px;
  background: rgba(0, 255, 255, 0.1);
  border: 1px solid rgba(0, 255, 255, 0.2);
  border-radius: 12px;
  font-size: 14px;
  color: #00ff88;
}

.loading-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  color: #888;
}

.empty-icon {
  font-size: 80px;
  margin-bottom: 20px;
}

.empty-title {
  font-size: 24px;
  color: #e0e0e0;
  margin-bottom: 12px;
}

.empty-desc {
  font-size: 16px;
  margin-bottom: 24px;
}

.shop-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: linear-gradient(135deg, #00ff88, #00d4ff);
  border: none;
  border-radius: 24px;
  text-decoration: none;
  color: #1a1a2e;
  font-weight: 500;
  font-size: 16px;
  transition: all 0.3s;
}

.shop-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 255, 136, 0.3);
}

.favorites-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
}

.favorite-card {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(0, 255, 255, 0.1);
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s;
}

.favorite-card:hover {
  border-color: rgba(0, 255, 255, 0.3);
  box-shadow: 0 8px 24px rgba(0, 255, 255, 0.1);
  transform: translateY(-4px);
}

.product-image-wrapper {
  position: relative;
  width: 100%;
  padding-top: 100%;
  overflow: hidden;
}

.product-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.favorite-card:hover .product-image {
  transform: scale(1.05);
}

.product-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(26, 26, 46, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
}

.favorite-card:hover .product-overlay {
  opacity: 1;
}

.overlay-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.quick-add-btn,
.remove-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border: none;
  border-radius: 20px;
  font-size: 14px;
  transition: all 0.3s;
}

.quick-add-btn {
  background: linear-gradient(135deg, #00ff88, #00d4ff);
  color: #1a1a2e;
}

.quick-add-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 255, 136, 0.3);
}

.remove-btn {
  background: rgba(255, 68, 68, 0.2);
  color: #ff4444;
  border: 1px solid rgba(255, 68, 68, 0.3);
}

.remove-btn:hover {
  background: rgba(255, 68, 68, 0.3);
  border-color: rgba(255, 68, 68, 0.5);
}

.product-info {
  padding: 16px;
}

.product-name {
  font-size: 16px;
  font-weight: 500;
  color: #e0e0e0;
  margin-bottom: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.price {
  font-size: 20px;
  font-weight: bold;
  color: #00ff88;
}

.stock {
  font-size: 14px;
  color: #888;
}

.product-actions {
  display: flex;
  gap: 8px;
}

.view-btn {
  flex: 1;
  padding: 8px 16px;
  background: rgba(0, 255, 255, 0.1);
  border: 1px solid rgba(0, 255, 255, 0.2);
  border-radius: 8px;
  color: #00ff88;
  font-size: 14px;
  transition: all 0.3s;
}

.view-btn:hover {
  background: rgba(0, 255, 255, 0.2);
  border-color: rgba(0, 255, 255, 0.4);
}

@media (max-width: 768px) {
  .nav-links {
    display: none;
  }
  
  .favorites-grid {
    grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
    gap: 16px;
  }
  
  .product-name {
    font-size: 14px;
  }
  
  .price {
    font-size: 18px;
  }
}
</style>
