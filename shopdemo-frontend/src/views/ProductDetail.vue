<template>
  <div class="product-detail-container">
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
      <el-row :gutter="20" v-if="product">
        <el-col :span="12">
          <el-card class="product-image-card">
            <img 
              :src="product.mainImage || product.image" 
              class="product-image" 
              @error="handleImageError"
            />
          </el-card>
        </el-col>
        
        <el-col :span="12">
          <el-card class="product-info-card">
            <template #header>
              <h2>{{ product.name }}</h2>
            </template>
            
            <div class="price-section">
              <span class="price">¥{{ product.price }}</span>
              <el-tag type="success">库存: {{ product.stock }}</el-tag>
            </div>
            
            <div class="description">
              <h3>商品描述</h3>
              <p>{{ product.description }}</p>
            </div>
            
            <div class="action-section">
              <el-button
                v-if="!isFavorited"
                type="warning"
                size="large"
                @click="toggleFavorite"
              >
                收藏商品
              </el-button>
              <el-button
                v-else
                type="info"
                size="large"
                @click="toggleFavorite"
              >
                已收藏
              </el-button>
              <el-input-number
                v-model="quantity"
                :min="1"
                :max="product.stock"
                size="large"
              />
              <el-button
                type="primary"
                size="large"
                @click="addToCart"
              >
                加入购物车
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-card class="reviews-card" v-if="product">
        <template #header>
          <div class="reviews-header">
            <h3>商品评价</h3>
            <el-button 
              v-if="canUserReview" 
              type="primary" 
              size="small"
              @click="reviewDialogVisible = true"
            >
              写评价
            </el-button>
          </div>
        </template>
        <el-empty v-if="reviews.length === 0" description="暂无评价" />
        <div v-else class="review-list">
          <div v-for="review in reviews" :key="review.id" class="review-item">
            <div class="review-header">
              <span class="username">{{ review.username }}</span>
              <span class="time">{{ review.createTime }}</span>
            </div>
            <div class="review-content">{{ review.content }}</div>
            <el-rate v-model="review.rating" disabled />
          </div>
        </div>
      </el-card>

      <el-dialog
        v-model="reviewDialogVisible"
        title="商品评价"
        width="500px"
      >
        <el-form :model="reviewForm" label-width="80px">
          <el-form-item label="评分">
            <el-rate v-model="reviewForm.rating" />
          </el-form-item>
          <el-form-item label="评价内容">
            <el-input
              v-model="reviewForm.content"
              type="textarea"
              :rows="4"
              placeholder="请输入评价内容"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="reviewDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitReview">提交评价</el-button>
        </template>
      </el-dialog>
    </el-main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getProductDetail, getReviews, addReview, addFavorite, removeFavorite, canReview, getFavorites } from '@/api/product'
import { addToCart as addToCartApi } from '@/api/cart'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const product = ref(null)
const quantity = ref(1)
const reviews = ref([])
const isFavorited = ref(false)
const canUserReview = ref(false)
const reviewDialogVisible = ref(false)
const reviewForm = ref({
  rating: 5,
  content: ''
})

onMounted(async () => {
  await loadProduct()
  await loadReviews()
  if (userStore.token) {
    await checkCanReview()
    await checkFavorite()
  }
})

const loadProduct = async () => {
  try {
    const id = route.params.id
    product.value = await getProductDetail(id)
  } catch (error) {
    ElMessage.error({ message: '获取商品详情失败', duration: 800 })
  }
}

const checkFavorite = async () => {
  try {
    const favorites = await getFavorites()
    isFavorited.value = favorites.some(fav => fav.productId === Number(route.params.id))
  } catch (error) {
    isFavorited.value = false
  }
}

const loadReviews = async () => {
  try {
    const id = route.params.id
    const response = await getReviews(id)
    reviews.value = response.records || []
  } catch (error) {
    ElMessage.error({ message: '获取评价失败', duration: 800 })
  }
}

const checkCanReview = async () => {
  try {
    const id = route.params.id
    canUserReview.value = await canReview(id)
  } catch (error) {
    canUserReview.value = false
  }
}

const addToCart = async () => {
  if (!userStore.token) {
    ElMessage.warning({ message: '请先登录', duration: 800 })
    router.push('/login')
    return
  }
  try {
    await addToCartApi({ productId: product.value.id, quantity: quantity.value })
    ElMessage.success({ message: '已加入购物车', duration: 800 })
  } catch (error) {
    ElMessage.error({ message: '加入购物车失败', duration: 800 })
  }
}

const toggleFavorite = async () => {
  if (!userStore.token) {
    ElMessage.warning({ message: '请先登录', duration: 800 })
    router.push('/login')
    return
  }
  try {
    if (isFavorited.value) {
      await removeFavorite(product.value.id)
      ElMessage.success({ message: '取消收藏成功', duration: 800 })
    } else {
      await addFavorite(product.value.id)
      ElMessage.success({ message: '收藏成功', duration: 800 })
    }
    isFavorited.value = !isFavorited.value
  } catch (error) {
    ElMessage.error({ message: '操作失败', duration: 800 })
  }
}

const handleImageError = (event) => {
  event.target.src = 'https://via.placeholder.com/400x400?text=No+Image'
}

const submitReview = async () => {
  if (!reviewForm.value.content.trim()) {
    ElMessage.warning({ message: '请输入评价内容', duration: 800 })
    return
  }
  try {
    await addReview({
      productId: product.value.id,
      rating: reviewForm.value.rating,
      content: reviewForm.value.content
    })
    ElMessage.success({ message: '评价成功', duration: 800 })
    reviewDialogVisible.value = false
    reviewForm.value = { rating: 5, content: '' }
    await loadReviews()
    canUserReview.value = false
  } catch (error) {
    ElMessage.error({ message: error.message || '评价失败', duration: 800 })
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
.product-detail-container {
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
  height: 60px;
  position: sticky;
  top: 0;
  z-index: 1000;
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
  cursor: pointer;
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

.product-image-card {
  height: 100%;
}

.product-image {
  width: 100%;
  height: 400px;
  object-fit: cover;
}

.product-info-card h2 {
  margin: 0;
  color: #303133;
}

.price-section {
  margin: 20px 0;
  display: flex;
  align-items: center;
  gap: 20px;
}

.price {
  font-size: 32px;
  color: #f56c6c;
  font-weight: bold;
}

.description {
  margin: 30px 0;
}

.description h3 {
  margin-bottom: 10px;
  color: #303133;
}

.description p {
  color: #606266;
  line-height: 1.6;
}

.action-section {
  margin-top: 30px;
  display: flex;
  gap: 15px;
}

.reviews-card {
  margin-top: 20px;
}

.reviews-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.reviews-card h3 {
  margin: 0;
  color: #303133;
}

.review-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.review-item {
  padding: 20px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.review-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.username {
  font-weight: bold;
  color: #303133;
}

.time {
  color: #909399;
  font-size: 14px;
}

.review-content {
  color: #606266;
  line-height: 1.6;
  margin-bottom: 10px;
}
</style>
