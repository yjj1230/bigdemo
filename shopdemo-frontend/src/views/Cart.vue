<template>
  <div class="cart-container">
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
          <router-link to="/cart" class="nav-link active">
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
      <!-- 页面标题 -->
      <section class="page-header">
        <div class="header-content">
          <span class="page-icon">🛒</span>
          <h1 class="page-title">购物车</h1>
          <span class="item-count">{{ totalItems }} 件商品</span>
          <div class="header-actions">
            <el-button type="primary" size="small" @click="forceRefreshCart" style="margin-right: 10px;">
              🔄 刷新
            </el-button>
            <el-button type="danger" size="small" @click="clearAllItems" v-if="cartItems.length > 0">
              🗑️ 清空
            </el-button>
          </div>
        </div>
      </section>

      <!-- 空购物车状态 -->
      <div v-if="cartItems.length === 0" class="empty-cart">
        <div class="empty-content">
          <div class="empty-icon">🛒</div>
          <h2 class="empty-title">购物车是空的</h2>
          <p class="empty-desc">快去挑选心仪的商品吧</p>
          <router-link to="/products" class="shop-btn">
            <span class="btn-icon">🛍️</span>
            <span class="btn-text">去购物</span>
          </router-link>
        </div>
      </div>

      <!-- 购物车内容 -->
      <div v-else class="cart-content">
        <!-- 购物车列表 -->
        <div class="cart-items">
          <div 
            v-for="item in cartItems" 
            :key="item.id" 
            class="cart-item"
          >
            <div class="item-image">
              <img 
                :src="item.product.mainImage || item.product.image" 
                :alt="item.product.name"
                @error="handleImageError"
              />
            </div>
            <div class="item-info">
              <h3 class="item-name">{{ item.product.name }}</h3>
              <p class="item-desc">{{ item.product.description }}</p>
              <div class="item-meta">
                <span class="item-price">¥{{ item.product.price }}</span>
              </div>
            </div>
            <div class="item-quantity">
              <el-input-number
                v-model="item.quantity"
                :min="1"
                :max="item.product.stock"
                @change="updateQuantity(item)"
                class="quantity-input"
              />
            </div>
            <div class="item-subtotal">
              <span class="subtotal-label">小计</span>
              <span class="subtotal-value">¥{{ (item.product.price * item.quantity).toFixed(2) }}</span>
            </div>
            <div class="item-actions">
              <el-button
                type="danger"
                size="small"
                :icon="Delete"
                @click="removeItem(item.id)"
                class="delete-btn"
              >
                删除
              </el-button>
            </div>
          </div>
        </div>

        <!-- 结算区域 -->
        <div class="cart-summary">
          <div class="summary-card">
            <div class="summary-header">
              <span class="summary-icon">💰</span>
              <h3 class="summary-title">订单摘要</h3>
            </div>
            <div class="summary-body">
              <div class="summary-row">
                <span class="summary-label">商品总数</span>
                <span class="summary-value">{{ totalItems }} 件</span>
              </div>
              <div class="summary-row total">
                <span class="summary-label">总金额</span>
                <span class="summary-value total-price">¥{{ totalPrice.toFixed(2) }}</span>
              </div>
            </div>
            <el-button
              type="primary"
              size="large"
              @click="checkout"
              class="checkout-btn"
            >
              <span class="btn-icon">💳</span>
              <span class="btn-text">去结算</span>
            </el-button>
          </div>
        </div>
      </div>
    </main>

    <!-- 结算对话框 -->
    <el-dialog
      v-model="checkoutDialogVisible"
      title="确认订单"
      width="600px"
      class="checkout-dialog"
    >
      <div class="checkout-content">
        <!-- 收货地址 -->
        <div class="checkout-section">
          <div class="section-header">
            <span class="section-icon">📍</span>
            <h4 class="section-title">收货地址</h4>
          </div>
          <el-radio-group v-model="selectedAddressId" class="address-list">
            <el-radio
              v-for="address in addresses"
              :key="address.id"
              :label="address.id"
              class="address-item"
            >
              <div class="address-info">
                <div class="address-header">
                  <span class="name">{{ address.receiverName }}</span>
                  <span class="phone">{{ address.receiverPhone }}</span>
                  <el-tag v-if="address.isDefault === 1" type="success" size="small">默认</el-tag>
                </div>
                <div class="address-detail">
                  {{ address.province }}{{ address.city }}{{ address.district }}{{ address.detailAddress }}
                </div>
              </div>
            </el-radio>
          </el-radio-group>
        </div>

        <!-- 订单备注 -->
        <div class="checkout-section">
          <div class="section-header">
            <span class="section-icon">📝</span>
            <h4 class="section-title">订单备注</h4>
          </div>
          <el-input
            v-model="remark"
            type="textarea"
            :rows="3"
            placeholder="请输入订单备注（选填）"
            maxlength="200"
            show-word-limit
            class="remark-input"
          />
        </div>

        <!-- 优惠券选择 -->
        <div class="checkout-section">
          <div class="section-header">
            <span class="section-icon">🎫</span>
            <h4 class="section-title">优惠券</h4>
          </div>
          <div v-if="coupons.length > 0" class="coupon-list">
            <el-radio-group v-model="selectedCouponId" class="coupon-radio-group">
              <el-radio :label="null" class="coupon-item">
                <div class="coupon-info">
                  <span class="coupon-name">不使用优惠券</span>
                </div>
              </el-radio>
              <el-radio
                v-for="coupon in coupons"
                :key="coupon.id"
                :label="coupon.id"
                class="coupon-item"
              >
                <div class="coupon-info">
                  <span class="coupon-name">{{ coupon.name }}</span>
                  <span class="coupon-desc">
                    {{ coupon.type === 1 ? `满${coupon.minAmount}减${coupon.discountAmount}` : 
                       coupon.type === 2 ? `${(coupon.discountRate * 10).toFixed(1)}折` : 
                       '立减10元' }}
                  </span>
                </div>
              </el-radio>
            </el-radio-group>
          </div>
          <div v-else class="no-coupon">
            <span class="no-coupon-text">暂无可用优惠券</span>
          </div>
        </div>

        <!-- 订单摘要 -->
        <div class="checkout-section summary-section">
          <div class="section-header">
            <span class="section-icon">💰</span>
            <h4 class="section-title">订单摘要</h4>
          </div>
          <div class="summary-row">
            <span class="summary-label">商品总数</span>
            <span class="summary-value">{{ totalItems }} 件</span>
          </div>
          <div class="summary-row" v-if="couponDiscount > 0">
            <span class="summary-label">优惠券优惠</span>
            <span class="summary-value discount">-¥{{ couponDiscount.toFixed(2) }}</span>
          </div>
          <div class="summary-row total">
            <span class="summary-label">订单金额</span>
            <span class="summary-value total-price">¥{{ finalPrice.toFixed(2) }}</span>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="checkoutDialogVisible = false" class="cancel-btn">取消</el-button>
        <el-button type="primary" @click="confirmCheckout" class="confirm-btn">
          <span class="btn-icon">✅</span>
          <span class="btn-text">确认下单</span>
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onActivated } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, ArrowDown } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getCart, updateCartItem, removeFromCart, refreshCart, clearCart } from '@/api/cart'
import { createOrderFromCart } from '@/api/order'
import { getAddresses } from '@/api/address'
import { getUserCoupons } from '@/api/coupon'

const router = useRouter()
const userStore = useUserStore()

const cartItems = ref([])
const addresses = ref([])
const selectedAddressId = ref(null)
const checkoutDialogVisible = ref(false)
const remark = ref('')
const coupons = ref([])
const selectedCouponId = ref(null)

const totalItems = computed(() => {
  return cartItems.value.reduce((sum, item) => sum + item.quantity, 0)
})

const totalPrice = computed(() => {
  return cartItems.value.reduce((sum, item) => sum + item.product.price * item.quantity, 0)
})

const couponDiscount = computed(() => {
  if (!selectedCouponId.value) {
    return 0
  }
  const coupon = coupons.value.find(c => c.id === selectedCouponId.value)
  if (!coupon) {
    return 0
  }
  
  const discount = coupon.type === 1 
    ? coupon.discountAmount 
    : coupon.type === 2 
      ? Math.min(totalPrice.value * (1 - coupon.discountRate), coupon.maxDiscount || Infinity)
      : 10
  
  return Math.min(discount, totalPrice.value)
})

const finalPrice = computed(() => {
  return Math.max(0, totalPrice.value - couponDiscount.value)
})

onMounted(async () => {
  await loadCart()
  await loadAddresses()
})

onActivated(async () => {
  await loadCart()
})

const loadCart = async () => {
  try {
    console.log('开始加载购物车...')
    const res = await getCart()
    console.log('购物车数据:', res)
    console.log('购物车数据类型:', typeof res)
    console.log('购物车数据是否为数组:', Array.isArray(res))
    cartItems.value = res
    console.log('cartItems.value:', cartItems.value)
  } catch (error) {
    console.error('获取购物车失败:', error)
    ElMessage.error({ message: '获取购物车失败', duration: 800 })
  }
}

const forceRefreshCart = async () => {
  try {
    console.log('强制刷新购物车...')
    const res = await refreshCart()
    cartItems.value = res
    console.log('购物车已刷新:', res)
    ElMessage.success('购物车已刷新')
  } catch (error) {
    console.error('刷新购物车失败:', error)
    ElMessage.error('刷新购物车失败')
  }
}

const clearAllItems = async () => {
  try {
    await ElMessageBox.confirm('确定要清空购物车吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await clearCart()
    cartItems.value = []
    ElMessage.success('购物车已清空')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('清空购物车失败')
    }
  }
}

const loadAddresses = async () => {
  try {
    const res = await getAddresses()
    addresses.value = res
    if (addresses.value.length > 0) {
      const defaultAddress = addresses.value.find(addr => addr.isDefault === 1)
      selectedAddressId.value = defaultAddress ? defaultAddress.id : addresses.value[0].id
    }
  } catch (error) {
    console.error('获取地址失败', error)
  }
}

const loadCoupons = async () => {
  try {
    const res = await getUserCoupons()
    coupons.value = res.filter(coupon => {
      const now = new Date()
      const validStart = new Date(coupon.startTime)
      const validEnd = new Date(coupon.endTime)
      return coupon.status === 1 && 
             now >= validStart && 
             now <= validEnd &&
             totalPrice.value >= (coupon.minAmount || 0)
    })
  } catch (error) {
    console.error('获取优惠券失败', error)
  }
}

const updateQuantity = async (item) => {
  try {
    await updateCartItem({
      cartId: item.id,
      quantity: item.quantity
    })
    ElMessage.success({ message: '更新成功', duration: 800 })
  } catch (error) {
    ElMessage.error({ message: '更新失败', duration: 800 })
  }
}

const removeItem = async (cartId) => {
  try {
    await ElMessageBox.confirm('确定要删除该商品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await removeFromCart(cartId)
    await loadCart()
    ElMessage.success({ message: '删除成功', duration: 800 })
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error({ message: '删除失败', duration: 800 })
    }
  }
}

const checkout = async () => {
  if (addresses.value.length === 0) {
    ElMessage.warning({ message: '请先添加收货地址', duration: 800 })
    router.push('/profile')
    return
  }
  await loadCoupons()
  checkoutDialogVisible.value = true
}

const confirmCheckout = async () => {
  if (!selectedAddressId.value) {
    ElMessage.warning('请选择收货地址')
    return
  }
  try {
    console.log('开始创建订单，参数:', {
      addressId: selectedAddressId.value,
      couponId: selectedCouponId.value,
      remark: remark.value
    })
    await createOrderFromCart(selectedAddressId.value, selectedCouponId.value, remark.value)
    ElMessage.success('订单创建成功')
    checkoutDialogVisible.value = false
    remark.value = ''
    selectedCouponId.value = null
    cartItems.value = []
    router.push('/orders')
  } catch (error) {
    console.error('创建订单失败:', error)
    console.error('错误响应:', error.response)
    console.error('错误数据:', error.response?.data)
    const errorMessage = error.response?.data?.message || error.message || '创建订单失败'
    ElMessage.error(errorMessage)
  }
}

const handleImageError = (event) => {
  event.target.src = 'https://via.placeholder.com/80x80?text=No+Image'
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

.cart-container {
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

/* 页面标题 */
.page-header {
  margin-bottom: 40px;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.page-icon {
  font-size: 40px;
}

.page-title {
  font-family: 'Orbitron', sans-serif;
  font-size: 32px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
}

.item-count {
  margin-left: auto;
  padding: 8px 16px;
  background: var(--bg-lighter);
  border-radius: 20px;
  font-size: 14px;
  color: var(--text-secondary);
}

/* 空购物车 */
.empty-cart {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.empty-content {
  text-align: center;
  padding: 60px 40px;
}

.empty-icon {
  font-size: 80px;
  margin-bottom: 24px;
}

.empty-title {
  font-size: 24px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 12px 0;
}

.empty-desc {
  font-size: 16px;
  color: var(--text-secondary);
  margin: 0 0 32px 0;
}

.shop-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 14px 32px;
  background: var(--gradient-1);
  border-radius: 8px;
  text-decoration: none;
  color: var(--text-primary);
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 4px 16px var(--shadow-color);
}

.shop-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px var(--shadow-color);
}

.btn-icon {
  font-size: 20px;
}

.btn-text {
  font-size: 16px;
  font-weight: 600;
}

/* 购物车内容 */
.cart-content {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 24px;
}

/* 购物车列表 */
.cart-items {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.cart-item {
  display: grid;
  grid-template-columns: 120px 1fr 140px 140px 100px;
  gap: 20px;
  padding: 20px;
  background: var(--bg-card);
  border-radius: 12px;
  border: 1px solid var(--border-color);
  transition: all 0.3s ease;
  align-items: center;
}

.cart-item:hover {
  border-color: var(--primary-color);
  box-shadow: 0 4px 16px var(--shadow-color);
}

.item-image {
  width: 120px;
  height: 120px;
  border-radius: 8px;
  overflow: hidden;
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.item-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-desc {
  font-size: 13px;
  color: var(--text-secondary);
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-meta {
  margin-top: auto;
}

.item-price {
  font-family: 'Orbitron', sans-serif;
  font-size: 18px;
  font-weight: 700;
  color: var(--primary-color);
  text-shadow: 0 0 10px var(--shadow-color);
}

.quantity-input :deep(.el-input-number) {
  width: 120px;
}

.quantity-input :deep(.el-input__wrapper) {
  background: var(--bg-lighter);
  border: 1px solid var(--border-color);
  box-shadow: none;
}

.item-subtotal {
  display: flex;
  flex-direction: column;
  gap: 4px;
  align-items: flex-end;
}

.subtotal-label {
  font-size: 12px;
  color: var(--text-secondary);
}

.subtotal-value {
  font-family: 'Orbitron', sans-serif;
  font-size: 20px;
  font-weight: 700;
  color: var(--primary-color);
  text-shadow: 0 0 10px var(--shadow-color);
}

.item-actions {
  display: flex;
  justify-content: flex-end;
}

.delete-btn {
  background: var(--gradient-2);
  border: none;
  border-radius: 8px;
  padding: 8px 16px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.delete-btn:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(255, 107, 157, 0.3);
}

/* 结算区域 */
.cart-summary {
  position: sticky;
  top: 100px;
  align-self: start;
}

.summary-card {
  background: var(--bg-card);
  border-radius: 12px;
  border: 1px solid var(--border-color);
  padding: 24px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
}

.summary-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}

.summary-icon {
  font-size: 24px;
}

.summary-title {
  font-family: 'Orbitron', sans-serif;
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.summary-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 24px;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid var(--border-color);
}

.summary-row:last-child {
  border-bottom: none;
}

.summary-row.total {
  padding-top: 20px;
  margin-top: 8px;
  border-top: 2px solid var(--primary-color);
}

.summary-label {
  font-size: 14px;
  color: var(--text-secondary);
}

.summary-value {
  font-family: 'Orbitron', sans-serif;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.summary-value.total-price {
  font-size: 24px;
  font-weight: 700;
  color: var(--primary-color);
  text-shadow: 0 0 10px var(--shadow-color);
}

.summary-value.discount {
  color: #ff4757;
  font-size: 16px;
}

.coupon-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.coupon-radio-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
}

.coupon-item {
  width: 100%;
  margin: 0;
  padding: 12px 16px;
  border: 2px solid var(--border-color);
  border-radius: 8px;
  transition: all 0.3s ease;
  background: var(--bg-card);
}

.coupon-item:hover {
  border-color: var(--primary-color);
  box-shadow: 0 2px 8px var(--shadow-color);
}

.coupon-item.is-checked {
  border-color: var(--primary-color);
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1), rgba(118, 75, 162, 0.1));
}

.coupon-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  width: 100%;
}

.coupon-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}

.coupon-desc {
  font-size: 13px;
  color: var(--text-secondary);
}

.no-coupon {
  padding: 20px;
  text-align: center;
  border: 2px dashed var(--border-color);
  border-radius: 8px;
  background: var(--bg-card);
}

.no-coupon-text {
  font-size: 14px;
  color: var(--text-secondary);
}

.checkout-btn {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 16px;
  background: var(--gradient-1);
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 4px 16px var(--shadow-color);
}

.checkout-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px var(--shadow-color);
}

/* 结算对话框 */
.checkout-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.checkout-section {
  margin-bottom: 0;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
}

.section-icon {
  font-size: 20px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.address-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.address-item {
  width: 100%;
  margin: 0;
  padding: 16px;
  background: var(--bg-lighter);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  transition: all 0.3s ease;
}

.address-item:hover {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px var(--shadow-color);
}

.address-info {
  margin-left: 10px;
}

.address-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.name {
  font-weight: 600;
  color: var(--text-primary);
}

.phone {
  color: var(--text-secondary);
}

.address-detail {
  color: var(--text-secondary);
  line-height: 1.5;
}

.remark-input :deep(.el-textarea__inner) {
  background: var(--bg-lighter);
  border: 1px solid var(--border-color);
  color: var(--text-primary);
}

.remark-input :deep(.el-textarea__inner):focus {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px var(--shadow-color);
}

.summary-section {
  background: var(--bg-lighter);
  padding: 20px;
  border-radius: 8px;
}

.summary-section .summary-row {
  border-bottom-color: var(--border-color);
}

.cancel-btn {
  background: var(--bg-lighter);
  border: 1px solid var(--border-color);
  color: var(--text-primary);
  padding: 12px 24px;
  border-radius: 8px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.cancel-btn:hover {
  background: var(--border-color);
}

.confirm-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: var(--gradient-1);
  border: none;
  border-radius: 8px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.confirm-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px var(--shadow-color);
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .navbar-content,
  .main-content {
    max-width: 100%;
  }
  
  .cart-content {
    grid-template-columns: 1fr;
  }
  
  .cart-summary {
    position: static;
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
  
  .page-header {
    margin-bottom: 24px;
  }
  
  .page-icon {
    font-size: 32px;
  }
  
  .page-title {
    font-size: 24px;
  }
  
  .cart-item {
    grid-template-columns: 80px 1fr;
    grid-template-rows: auto auto auto;
    gap: 12px;
  }
  
  .item-image {
    grid-column: 1 / 2;
    grid-row: 1 / 2;
    width: 80px;
    height: 80px;
  }
  
  .item-info {
    grid-column: 2 / 3;
    grid-row: 1 / 2;
  }
  
  .item-quantity {
    grid-column: 1 / 2;
    grid-row: 2 / 3;
  }
  
  .item-subtotal {
    grid-column: 2 / 3;
    grid-row: 2 / 3;
    align-items: flex-start;
  }
  
  .item-actions {
    grid-column: 1 / 3;
    grid-row: 3 / 4;
  }
  
  .quantity-input :deep(.el-input-number) {
    width: 100%;
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
  
  .page-icon {
    font-size: 28px;
  }
  
  .page-title {
    font-size: 20px;
  }
  
  .item-count {
    display: none;
  }
  
  .empty-content {
    padding: 40px 20px;
  }
  
  .empty-icon {
    font-size: 60px;
  }
  
  .empty-title {
    font-size: 20px;
  }
  
  .empty-desc {
    font-size: 14px;
  }
  
  .shop-btn {
    padding: 12px 24px;
  }
  
  .cart-item {
    padding: 16px;
  }
}
</style>
