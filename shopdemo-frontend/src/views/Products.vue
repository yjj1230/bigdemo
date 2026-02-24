<template>
  <div class="products-container">
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
      <el-card class="search-card">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索商品"
          size="large"
          clearable
          @clear="handleSearch"
        >
          <template #append>
            <el-button :icon="Search" @click="handleSearch" />
          </template>
        </el-input>
      </el-card>

      <el-row :gutter="20" class="products-grid">
        <el-col :span="6" v-for="product in products" :key="product.id">
          <el-card :body-style="{ padding: '0px' }" class="product-card">
            <img 
              :src="product.mainImage || product.image" 
              class="product-image" 
              @error="handleImageError"
            />
            <div class="product-info">
              <h3>{{ product.name }}</h3>
              <p class="price">¥{{ product.price }}</p>
              <div class="actions">
                <el-button type="primary" size="small" @click="viewDetail(product)">
                  查看详情
                </el-button>
                <el-button type="success" size="small" @click="addToCart(product)">
                  加入购物车
                </el-button>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[8, 16, 24, 32]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        class="pagination"
      />
    </el-main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, ArrowDown } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getProducts, searchProducts } from '@/api/product'
import { addToCart as addToCartApi } from '@/api/cart'

const router = useRouter()
const userStore = useUserStore()

const searchKeyword = ref('')
const products = ref([])
const currentPage = ref(1)
const pageSize = ref(8)
const total = ref(0)

onMounted(async () => {
  await loadProducts()
})

const loadProducts = async () => {
  try {
    const res = await getProducts({
      page: currentPage.value,
      size: pageSize.value
    })
    products.value = res.records
    total.value = res.total
  } catch (error) {
    ElMessage.error('获取商品列表失败')
  }
}

const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    await loadProducts()
    return
  }
  try {
    const res = await searchProducts(searchKeyword.value)
    products.value = res
    total.value = res.length
  } catch (error) {
    ElMessage.error('搜索失败')
  }
}

const viewDetail = (product) => {
  router.push(`/product/${product.id}`)
}

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

const handleSizeChange = (val) => {
  pageSize.value = val
  loadProducts()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  loadProducts()
}

const handleImageError = (event) => {
  event.target.src = 'https://via.placeholder.com/300x300?text=No+Image'
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
.products-container {
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

.search-card {
  margin-bottom: 20px;
}

.products-grid {
  margin-bottom: 20px;
}

.product-card {
  margin-bottom: 20px;
  transition: transform 0.3s;
}

.product-card:hover {
  transform: translateY(-5px);
}

.product-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

.product-info {
  padding: 15px;
}

.product-info h3 {
  margin: 0 0 10px 0;
  font-size: 16px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.price {
  color: #f56c6c;
  font-size: 20px;
  font-weight: bold;
  margin: 10px 0;
}

.actions {
  display: flex;
  gap: 10px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
