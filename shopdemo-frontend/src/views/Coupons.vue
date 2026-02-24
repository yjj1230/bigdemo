<template>
  <div class="coupons-container">
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
          <span class="page-icon">🎫</span>
          <h1 class="page-title">优惠券中心</h1>
        </div>
      </section>

      <!-- 优惠券标签页 -->
      <el-tabs v-model="activeTab" class="coupon-tabs">
        <!-- 可领取优惠券 -->
        <el-tab-pane label="可领取" name="available">
          <div v-if="loading" class="loading-state">
            <el-icon class="is-loading"><loading /></el-icon>
            <span>加载中...</span>
          </div>

          <div v-else-if="availableCoupons.length === 0" class="empty-state">
            <span class="empty-icon">🎫</span>
            <p>暂无可用优惠券</p>
          </div>

          <div v-else class="coupon-list">
            <div 
              v-for="coupon in availableCoupons" 
              :key="coupon.id" 
              class="coupon-card"
            >
              <div class="coupon-left">
                <div class="coupon-amount">
                  <span class="amount-symbol">¥</span>
                  <span class="amount-value">{{ coupon.discountAmount || '免' }}</span>
                </div>
                <div class="coupon-type">{{ getCouponTypeText(coupon.type) }}</div>
              </div>
              <div class="coupon-divider"></div>
              <div class="coupon-right">
                <h3 class="coupon-name">{{ coupon.name }}</h3>
                <p class="coupon-desc">{{ coupon.description }}</p>
                <div class="coupon-info">
                  <span class="info-item">
                    <span class="info-icon">📅</span>
                    <span class="info-text">{{ formatDate(coupon.startTime) }} - {{ formatDate(coupon.endTime) }}</span>
                  </span>
                  <span class="info-item">
                    <span class="info-icon">👥</span>
                    <span class="info-text">限领{{ coupon.limitPerUser }}张</span>
                  </span>
                  <span class="info-item">
                    <span class="info-icon">📊</span>
                    <span class="info-text">已领{{ coupon.receivedCount }}/{{ coupon.totalCount }}</span>
                  </span>
                </div>
                <el-button 
                  type="primary" 
                  @click="receiveCoupon(coupon.id)"
                  :disabled="coupon.receivedCount >= coupon.totalCount"
                  class="receive-btn"
                >
                  <span class="btn-icon">🎁</span>
                  <span class="btn-text">{{ coupon.receivedCount >= coupon.totalCount ? '已领完' : '立即领取' }}</span>
                </el-button>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 我的优惠券 -->
        <el-tab-pane label="我的优惠券" name="my">
          <div v-if="loading" class="loading-state">
            <el-icon class="is-loading"><loading /></el-icon>
            <span>加载中...</span>
          </div>

          <div v-else-if="myCoupons.length === 0" class="empty-state">
            <span class="empty-icon">🎫</span>
            <p>暂无优惠券</p>
          </div>

          <div v-else class="coupon-list">
            <div 
              v-for="coupon in myCoupons" 
              :key="coupon.id" 
              class="coupon-card"
              :class="{ used: coupon.status === 2, expired: coupon.status === 3 }"
            >
              <div class="coupon-left">
                <div class="coupon-amount">
                  <span class="amount-symbol">¥</span>
                  <span class="amount-value">{{ coupon.discountAmount || '免' }}</span>
                </div>
                <div class="coupon-type">{{ getCouponTypeText(coupon.type) }}</div>
              </div>
              <div class="coupon-divider"></div>
              <div class="coupon-right">
                <h3 class="coupon-name">{{ coupon.couponName }}</h3>
                <div class="coupon-status">
                  <el-tag v-if="coupon.status === 1" type="success">未使用</el-tag>
                  <el-tag v-else-if="coupon.status === 2" type="info">已使用</el-tag>
                  <el-tag v-else type="warning">已过期</el-tag>
                </div>
                <div class="coupon-info">
                  <span class="info-item">
                    <span class="info-icon">📅</span>
                    <span class="info-text">{{ formatDate(coupon.startTime) }} - {{ formatDate(coupon.endTime) }}</span>
                  </span>
                  <span v-if="coupon.minAmount" class="info-item">
                    <span class="info-icon">💰</span>
                    <span class="info-text">满{{ coupon.minAmount }}元可用</span>
                  </span>
                </div>
                <div v-if="coupon.status === 1" class="coupon-actions">
                  <el-button 
                    type="primary" 
                    size="small"
                    @click="goToCart"
                    class="use-btn"
                  >
                    <span class="btn-icon">🛒</span>
                    <span class="btn-text">去使用</span>
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, onActivated } from 'vue'
import { useRouter } from 'vue-router'
import { Loading, ArrowDown } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getAvailableCouponsForUser, receiveCoupon as apiReceiveCoupon } from '@/api/coupon'
import { getUserCoupons } from '@/api/coupon'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('available')
const loading = ref(false)
const availableCoupons = ref([])
const myCoupons = ref([])

const loadAvailableCoupons = async () => {
  loading.value = true
  try {
    const response = await getAvailableCouponsForUser()
    availableCoupons.value = response || []
  } catch (error) {
    console.error('加载优惠券失败:', error)
    ElMessage.error('加载优惠券失败')
  } finally {
    loading.value = false
  }
}

const loadMyCoupons = async () => {
  loading.value = true
  try {
    const response = await getUserCoupons()
    myCoupons.value = response || []
  } catch (error) {
    console.error('加载我的优惠券失败:', error)
    ElMessage.error('加载我的优惠券失败')
  } finally {
    loading.value = false
  }
}

const receiveCoupon = async (couponId) => {
  try {
    const response = await apiReceiveCoupon(couponId)
    if (response.code === 200) {
      ElMessage.success('领取成功')
      loadAvailableCoupons()
      loadMyCoupons()
    } else {
      ElMessage.error(response.message || '领取失败')
    }
  } catch (error) {
    console.error('领取优惠券失败:', error)
    ElMessage.error('领取优惠券失败')
  }
}

const goToCart = () => {
  router.push('/cart')
}

const getCouponTypeText = (type) => {
  const types = {
    1: '满减券',
    2: '折扣券',
    3: '免运费券'
  }
  return types[type] || '优惠券'
}

const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getMonth() + 1}.${d.getDate()}`
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
  loadAvailableCoupons()
  loadMyCoupons()
})

onActivated(() => {
  loadMyCoupons()
})
</script>

<style scoped>
.coupons-container {
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

.coupon-tabs {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(0, 255, 255, 0.1);
  border-radius: 12px;
  padding: 24px;
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

.coupon-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.coupon-card {
  display: flex;
  background: linear-gradient(135deg, rgba(0, 255, 136, 0.1), rgba(0, 212, 255, 0.1));
  border: 1px solid rgba(0, 255, 255, 0.2);
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s;
}

.coupon-card:hover {
  border-color: rgba(0, 255, 255, 0.4);
  box-shadow: 0 8px 24px rgba(0, 255, 255, 0.15);
  transform: translateY(-2px);
}

.coupon-card.used {
  background: linear-gradient(135deg, rgba(128, 128, 128, 0.1), rgba(96, 96, 96, 0.1));
  border-color: rgba(128, 128, 128, 0.2);
}

.coupon-card.expired {
  background: linear-gradient(135deg, rgba(255, 136, 0, 0.1), rgba(255, 68, 0, 0.1));
  border-color: rgba(255, 136, 0, 0.2);
}

.coupon-left {
  width: 140px;
  padding: 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #00ff88, #00d4ff);
  color: #1a1a2e;
}

.coupon-amount {
  display: flex;
  align-items: baseline;
  margin-bottom: 8px;
}

.amount-symbol {
  font-size: 20px;
  font-weight: bold;
}

.amount-value {
  font-size: 36px;
  font-weight: bold;
}

.coupon-type {
  font-size: 14px;
  font-weight: 500;
}

.coupon-divider {
  width: 2px;
  background: repeating-linear-gradient(
    to bottom,
    rgba(0, 255, 255, 0.3) 0,
    rgba(0, 255, 255, 0.3) 8px,
    transparent 8px,
    transparent 16px
  );
}

.coupon-right {
  flex: 1;
  padding: 20px 24px;
  display: flex;
  flex-direction: column;
}

.coupon-name {
  font-size: 18px;
  font-weight: bold;
  color: #e0e0e0;
  margin-bottom: 8px;
}

.coupon-desc {
  font-size: 14px;
  color: #c0c0c0;
  margin-bottom: 12px;
}

.coupon-status {
  margin-bottom: 12px;
}

.coupon-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #888;
}

.info-icon {
  font-size: 14px;
}

.info-text {
  color: #c0c0c0;
}

.coupon-actions {
  display: flex;
  gap: 12px;
}

.receive-btn,
.use-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 20px;
  background: linear-gradient(135deg, #00ff88, #00d4ff);
  border: none;
  border-radius: 20px;
  color: #1a1a2e;
  font-weight: 500;
  font-size: 14px;
  transition: all 0.3s;
}

.receive-btn:hover:not(:disabled),
.use-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 255, 136, 0.3);
}

.receive-btn:disabled {
  background: rgba(128, 128, 128, 0.3);
  color: #888;
  cursor: not-allowed;
}

.btn-icon {
  font-size: 16px;
}

.btn-text {
  font-size: 14px;
}

@media (max-width: 768px) {
  .nav-links {
    display: none;
  }
  
  .coupon-card {
    flex-direction: column;
  }
  
  .coupon-left {
    width: 100%;
    padding: 16px;
  }
  
  .coupon-divider {
    width: 100%;
    height: 2px;
    background: repeating-linear-gradient(
      to right,
      rgba(0, 255, 255, 0.3) 0,
      rgba(0, 255, 255, 0.3) 8px,
      transparent 8px,
      transparent 16px
    );
  }
}
</style>
