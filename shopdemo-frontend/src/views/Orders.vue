<template>
  <div class="orders-container">
    <el-header class="header">
      <div class="logo">商城系统</div>
      <div class="nav">
        <router-link to="/home">首页</router-link>
        <router-link to="/products">商品</router-link>
        <router-link to="/cart">购物车</router-link>
        <router-link to="/orders">订单</router-link>
        <router-link to="/profile">个人中心</router-link>
      </div>
      <div class="user-info">
        <div v-if="userStore.token" class="user-dropdown">
          <el-dropdown @command="handleCommand">
            <span class="el-dropdown-link">
              欢迎，{{ userStore.userInfo.username }}
              <el-icon class="el-icon--right"><arrow-down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="orders">我的订单</el-dropdown-item>
                <el-dropdown-item command="cart">购物车</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
        <router-link v-else to="/login">登录</router-link>
      </div>
    </el-header>

    <el-main class="main-content">
      <el-card v-if="orders.length === 0" class="empty-orders">
        <el-empty description="暂无订单" />
        <el-button type="primary" @click="$router.push('/products')">
          去购物
        </el-button>
      </el-card>

      <div v-else>
        <el-card v-for="order in orders" :key="order.id" class="order-card">
          <template #header>
            <div class="order-header">
              <span>订单号：{{ order.orderNo }}</span>
              <el-tag :type="getStatusType(order.status)">
                {{ getStatusText(order.status) }}
              </el-tag>
            </div>
          </template>
          
          <div class="order-info">
            <div class="info-item">
              <span>下单时间：</span>
              <span>{{ order.createTime }}</span>
            </div>
            <div class="info-item">
              <span>订单金额：</span>
              <span class="amount">¥{{ order.totalAmount }}</span>
            </div>
            <div class="info-item">
              <span>收货地址：</span>
              <span>{{ order.address }}</span>
            </div>
          </div>

          <el-table :data="order.items" style="margin-top: 20px;">
            <el-table-column label="商品信息" width="400">
              <template #default="{ row }">
                <div class="product-info">
                  <img :src="row.productImage || row.product?.image" class="product-image" @error="handleImageError" />
                  <div>
                    <h4>{{ row.productName || row.product?.name }}</h4>
                    <p>¥{{ row.price }} × {{ row.quantity }}</p>
                  </div>
                </div>
              </template>
            </el-table-column>
            
            <el-table-column label="小计" width="120">
              <template #default="{ row }">
                <span>¥{{ (row.price * row.quantity).toFixed(2) }}</span>
              </template>
            </el-table-column>
          </el-table>

          <div class="order-actions">
            <el-button
              v-if="order.status === 0"
              type="primary"
              @click="payOrder(order.id)"
            >
              立即支付
            </el-button>
            <el-button
              v-if="order.status === 0"
              type="danger"
              @click="cancelOrder(order.id)"
            >
              取消订单
            </el-button>
            <el-button
              v-if="order.status === 2"
              type="success"
              @click="confirmOrder(order.id)"
            >
              确认收货
            </el-button>
            <el-button
              @click="viewDetail(order.id)"
            >
              查看详情
            </el-button>
          </div>
        </el-card>
      </div>
    </el-main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getOrders, payOrder as payOrderApi, cancelOrder as cancelOrderApi, confirmOrder as confirmOrderApi } from '@/api/order'

const router = useRouter()
const userStore = useUserStore()

const orders = ref([])

onMounted(async () => {
  await loadOrders()
})

const loadOrders = async () => {
  try {
    const res = await getOrders()
    orders.value = res
  } catch (error) {
    ElMessage.error({ message: '获取订单列表失败', duration: 800 })
  }
}

const payOrder = async (orderId) => {
  try {
    await payOrderApi(orderId)
    ElMessage.success({ message: '支付成功', duration: 800 })
    await loadOrders()
  } catch (error) {
    ElMessage.error({ message: '支付失败', duration: 800 })
  }
}

const cancelOrder = async (orderId) => {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await cancelOrderApi(orderId)
    ElMessage.success({ message: '取消成功', duration: 800 })
    await loadOrders()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error({ message: '取消失败', duration: 800 })
    }
  }
}

const confirmOrder = async (orderId) => {
  try {
    await confirmOrderApi(orderId)
    ElMessage.success({ message: '确认收货成功', duration: 800 })
    await loadOrders()
  } catch (error) {
    ElMessage.error({ message: '确认收货失败', duration: 800 })
  }
}

const viewDetail = (orderId) => {
  router.push(`/order/${orderId}`)
}

const getStatusType = (status) => {
  const statusMap = {
    0: 'warning',
    1: 'success',
    2: 'info',
    3: 'success',
    4: 'danger'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status) => {
  const statusMap = {
    0: '待支付',
    1: '已支付',
    2: '已发货',
    3: '已完成',
    4: '已取消'
  }
  return statusMap[status] || '未知'
}

const handleImageError = (event) => {
  event.target.src = 'https://via.placeholder.com/60x60?text=No+Image'
}

const handleCommand = async (command) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'orders':
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
.orders-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.logo {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
}

.nav {
  display: flex;
  gap: 20px;
}

.nav a {
  text-decoration: none;
  color: #606266;
  font-weight: 500;
}

.nav a:hover,
.nav a.router-link-active {
  color: #409eff;
}

.user-info {
  display: flex;
  align-items: center;
}

.user-dropdown {
  display: flex;
  align-items: center;
}

.el-dropdown-link {
  cursor: pointer;
  color: #606266;
  display: flex;
  align-items: center;
  font-size: 14px;
}

.el-dropdown-link:hover {
  color: #409eff;
}

.el-icon--right {
  margin-left: 5px;
}

.main-content {
  flex: 1;
  padding: 20px;
}

.empty-orders {
  text-align: center;
  margin-top: 100px;
}

.order-card {
  margin-bottom: 20px;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-info {
  margin: 20px 0;
}

.info-item {
  display: flex;
  margin-bottom: 10px;
}

.info-item span:first-child {
  color: #909399;
  margin-right: 10px;
}

.amount {
  color: #f56c6c;
  font-size: 18px;
  font-weight: bold;
}

.product-info {
  display: flex;
  gap: 15px;
  align-items: center;
}

.product-image {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
}

.product-info h4 {
  margin: 0 0 5px 0;
  font-size: 14px;
  color: #303133;
}

.product-info p {
  margin: 0;
  font-size: 12px;
  color: #909399;
}

.order-actions {
  display: flex;
  gap: 10px;
  margin-top: 20px;
  justify-content: flex-end;
}
</style>
