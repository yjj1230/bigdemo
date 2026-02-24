<template>
  <div class="home-container">
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
      <div class="banner">
        <el-carousel height="400px">
          <el-carousel-item v-for="item in banners" :key="item.id">
            <img :src="item.image" :alt="item.title" />
          </el-carousel-item>
        </el-carousel>
      </div>

      <div class="section">
        <h2>热门商品</h2>
        <el-row :gutter="20">
          <el-col :span="6" v-for="product in hotProducts" :key="product.id">
            <el-card :body-style="{ padding: '0px' }" class="product-card">
              <img :src="product.image" class="product-image" />
              <div class="product-info">
                <h3>{{ product.name }}</h3>
                <p class="price">¥{{ product.price }}</p>
                <el-button type="primary" size="small" @click="addToCart(product)">
                  加入购物车
                </el-button>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <div class="section">
        <h2>推荐商品</h2>
        <el-row :gutter="20">
          <el-col :span="6" v-for="product in recommendProducts" :key="product.id">
            <el-card :body-style="{ padding: '0px' }" class="product-card">
              <img :src="product.image" class="product-image" />
              <div class="product-info">
                <h3>{{ product.name }}</h3>
                <p class="price">¥{{ product.price }}</p>
                <el-button type="primary" size="small" @click="addToCart(product)">
                  加入购物车
                </el-button>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </el-main>

    <el-footer class="footer">
      <p>&copy; 2024 商城系统 版权所有</p>
    </el-footer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getRecommendations } from '@/api/product'
import { addToCart as addToCartApi } from '@/api/cart'

const router = useRouter()
const userStore = useUserStore()

const banners = ref([
  { id: 1, title: '促销活动', image: 'https://via.placeholder.com/1200x400' },
  { id: 2, title: '新品上市', image: 'https://via.placeholder.com/1200x400' },
  { id: 3, title: '限时优惠', image: 'https://via.placeholder.com/1200x400' }
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
.home-container {
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

.banner img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.section {
  margin: 40px 0;
}

.section h2 {
  margin-bottom: 20px;
  color: #303133;
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

.footer {
  background: #f5f7fa;
  text-align: center;
  padding: 20px;
  color: #909399;
}
</style>
