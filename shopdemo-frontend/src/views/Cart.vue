<template>
  <div class="cart-container">
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
      <el-card v-if="cartItems.length === 0" class="empty-cart">
        <el-empty description="购物车是空的" />
        <el-button type="primary" @click="$router.push('/products')">
          去购物
        </el-button>
      </el-card>

      <div v-else>
        <el-card class="cart-items">
          <el-table :data="cartItems" style="width: 100%">
            <el-table-column label="商品信息" width="400">
              <template #default="{ row }">
                <div class="product-info">
                  <img 
                    :src="row.product.mainImage || row.product.image" 
                    class="product-image" 
                    @error="handleImageError"
                  />
                  <div>
                    <h4>{{ row.product.name }}</h4>
                    <p>{{ row.product.description }}</p>
                  </div>
                </div>
              </template>
            </el-table-column>
            
            <el-table-column label="单价" width="120">
              <template #default="{ row }">
                <span class="price">¥{{ row.product.price }}</span>
              </template>
            </el-table-column>
            
            <el-table-column label="数量" width="150">
              <template #default="{ row }">
                <el-input-number
                  v-model="row.quantity"
                  :min="1"
                  :max="row.product.stock"
                  @change="updateQuantity(row)"
                />
              </template>
            </el-table-column>
            
            <el-table-column label="小计" width="120">
              <template #default="{ row }">
                <span class="subtotal">¥{{ (row.product.price * row.quantity).toFixed(2) }}</span>
              </template>
            </el-table-column>
            
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button
                  type="danger"
                  size="small"
                  :icon="Delete"
                  @click="removeItem(row.id)"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <el-card class="cart-summary">
          <div class="summary-item">
            <span>商品总数：</span>
            <span>{{ totalItems }} 件</span>
          </div>
          <div class="summary-item">
            <span>总金额：</span>
            <span class="total-price">¥{{ totalPrice.toFixed(2) }}</span>
          </div>
          <el-button
            type="primary"
            size="large"
            @click="checkout"
            style="width: 100%"
          >
            去结算
          </el-button>
        </el-card>
      </div>

      <el-dialog
        v-model="checkoutDialogVisible"
        title="确认订单"
        width="600px"
      >
        <div class="checkout-dialog">
          <div class="section">
            <h4>收货地址</h4>
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

          <div class="section">
            <h4>订单备注</h4>
            <el-input
              v-model="remark"
              type="textarea"
              :rows="3"
              placeholder="请输入订单备注（选填）"
              maxlength="200"
              show-word-limit
            />
          </div>

          <div class="section summary">
            <div class="summary-item">
              <span>商品总数：</span>
              <span>{{ totalItems }} 件</span>
            </div>
            <div class="summary-item">
              <span>订单金额：</span>
              <span class="total-price">¥{{ totalPrice.toFixed(2) }}</span>
            </div>
          </div>
        </div>
        <template #footer>
          <el-button @click="checkoutDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmCheckout">确认下单</el-button>
        </template>
      </el-dialog>
    </el-main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, ArrowDown } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getCart, updateCartItem, removeFromCart } from '@/api/cart'
import { createOrderFromCart } from '@/api/order'
import { getAddresses } from '@/api/address'

const router = useRouter()
const userStore = useUserStore()

const cartItems = ref([])
const addresses = ref([])
const selectedAddressId = ref(null)
const checkoutDialogVisible = ref(false)
const remark = ref('')

const totalItems = computed(() => {
  return cartItems.value.reduce((sum, item) => sum + item.quantity, 0)
})

const totalPrice = computed(() => {
  return cartItems.value.reduce((sum, item) => sum + item.product.price * item.quantity, 0)
})

onMounted(async () => {
  await loadCart()
  await loadAddresses()
})

const loadCart = async () => {
  try {
    const res = await getCart()
    cartItems.value = res
  } catch (error) {
    ElMessage.error({ message: '获取购物车失败', duration: 800 })
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

const checkout = () => {
  if (addresses.value.length === 0) {
    ElMessage.warning({ message: '请先添加收货地址', duration: 800 })
    router.push('/profile')
    return
  }
  checkoutDialogVisible.value = true
}

const confirmCheckout = async () => {
  if (!selectedAddressId.value) {
    ElMessage.warning('请选择收货地址')
    return
  }
  try {
    await createOrderFromCart(selectedAddressId.value, remark.value)
    ElMessage.success('订单创建成功')
    checkoutDialogVisible.value = false
    remark.value = ''
    router.push('/orders')
  } catch (error) {
    ElMessage.error(error.message || '创建订单失败')
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
.cart-container {
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

.empty-cart {
  text-align: center;
  margin-top: 100px;
}

.cart-items {
  margin-bottom: 20px;
}

.product-info {
  display: flex;
  gap: 15px;
  align-items: center;
}

.product-image {
  width: 80px;
  height: 80px;
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

.price,
.subtotal {
  color: #f56c6c;
  font-weight: bold;
  font-size: 16px;
}

.cart-summary {
  position: sticky;
  bottom: 20px;
  z-index: 100;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 15px;
  font-size: 16px;
}

.total-price {
  color: #f56c6c;
  font-size: 24px;
  font-weight: bold;
}

.checkout-dialog {
  max-height: 600px;
  overflow-y: auto;
}

.section {
  margin-bottom: 20px;
}

.section h4 {
  margin: 0 0 15px 0;
  color: #303133;
  font-size: 16px;
}

.address-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.address-item {
  width: 100%;
  margin: 0;
  padding: 15px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  transition: all 0.3s;
}

.address-item:hover {
  border-color: #409eff;
}

.address-info {
  margin-left: 10px;
}

.address-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.name {
  font-weight: bold;
  color: #303133;
}

.phone {
  color: #606266;
}

.address-detail {
  color: #909399;
  line-height: 1.5;
}

.summary {
  background: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
}

.summary .summary-item {
  font-size: 16px;
}
</style>
