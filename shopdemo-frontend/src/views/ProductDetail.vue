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
            
            <!-- 评价统计信息 -->
            <div class="review-stats" v-if="reviewStatistics">
              <div class="stats-header">
                <div class="rating-average">
                  <span class="average-score">{{ reviewStatistics.averageRating.toFixed(1) }}</span>
                  <el-rate 
                    v-model="reviewStatistics.averageRating" 
                    disabled 
                    :score-template="'{value}'"
                  />
                </div>
                <span class="review-count">({{ reviewStatistics.totalCount }}条评价)</span>
              </div>
              <div class="rating-distribution">
                <div v-for="item in [5, 4, 3, 2, 1]" :key="item" class="rating-item">
                  <span class="rating-star">{{ item }}星</span>
                  <div class="rating-bar-container">
                    <div 
                      class="rating-bar" 
                      :style="{ width: getRatingPercentage(item) + '%' }"
                    ></div>
                  </div>
                  <span class="rating-count">{{ getRatingCount(item) }}</span>
                </div>
              </div>
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
            <div class="review-controls">
              <el-select v-model="reviewFilter" placeholder="筛选评价" size="small" style="margin-right: 10px;">
                <el-option label="全部评价" value="all" />
                <el-option label="5星" value="5" />
                <el-option label="4星" value="4" />
                <el-option label="3星" value="3" />
                <el-option label="2星" value="2" />
                <el-option label="1星" value="1" />
              </el-select>
              <el-select v-model="reviewSort" placeholder="排序方式" size="small">
                <el-option label="最新评价" value="latest" />
                <el-option label="评分最高" value="highest" />
                <el-option label="评分最低" value="lowest" />
              </el-select>
              <el-button 
                v-if="canUserReview" 
                type="primary" 
                size="small"
                @click="reviewDialogVisible = true"
                style="margin-left: 10px;"
              >
                写评价
              </el-button>
            </div>
          </div>
        </template>
        <el-empty v-if="filteredReviews.length === 0" description="暂无评价" />
        <div v-else class="review-list">
          <div v-for="review in filteredReviews" :key="review.id" class="review-item">
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
          <el-button type="primary" @click="submitReview" :loading="submittingReview">提交评价</el-button>
        </template>
      </el-dialog>
    </el-main>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getProductDetail, getReviews, addReview, addFavorite, removeFavorite, canReview, getFavorites, getReviewStatistics } from '@/api/product'
import { addToCart as addToCartApi } from '@/api/cart'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const product = ref(null)
const quantity = ref(1)
const reviews = ref([])
const reviewStatistics = ref(null) // 评价统计数据
const isFavorited = ref(false)
const canUserReview = ref(false)
const reviewDialogVisible = ref(false)
const reviewForm = ref({
  rating: 5,
  content: ''
})
const reviewFilter = ref('all') // 评价筛选
const reviewSort = ref('latest') // 评价排序
const submittingReview = ref(false) // 评价提交加载状态

onMounted(async () => {
  await loadProduct()
  await loadReviews()
  await loadReviewStatistics()
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

const loadReviewStatistics = async () => {
  try {
    const id = route.params.id
    reviewStatistics.value = await getReviewStatistics(id)
  } catch (error) {
    console.error('获取评价统计数据失败:', error)
    reviewStatistics.value = null
  }
}

// 计算过滤和排序后的评价列表
const filteredReviews = computed(() => {
  let result = [...reviews.value]
  
  // 筛选评价
  if (reviewFilter.value !== 'all') {
    result = result.filter(review => review.rating === Number(reviewFilter.value))
  }
  
  // 排序评价
  switch (reviewSort.value) {
    case 'latest':
      result.sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
      break
    case 'highest':
      result.sort((a, b) => b.rating - a.rating)
      break
    case 'lowest':
      result.sort((a, b) => a.rating - b.rating)
      break
  }
  
  return result
})

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
  submittingReview.value = true
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
    await loadReviewStatistics() // 重新加载评价统计数据
    canUserReview.value = false
  } catch (error) {
    ElMessage.error({ message: error.message || '评价失败', duration: 800 })
  } finally {
    submittingReview.value = false
  }
}

const getRatingPercentage = (rating) => {
  if (!reviewStatistics.value || !reviewStatistics.value.ratingDistribution) {
    return 0
  }
  const totalCount = reviewStatistics.value.totalCount
  if (totalCount === 0) return 0
  
  const ratingItem = reviewStatistics.value.ratingDistribution.find(item => item.rating === rating)
  const count = ratingItem ? ratingItem.count : 0
  return (count / totalCount) * 100
}

const getRatingCount = (rating) => {
  if (!reviewStatistics.value || !reviewStatistics.value.ratingDistribution) {
    return 0
  }
  const ratingItem = reviewStatistics.value.ratingDistribution.find(item => item.rating === rating)
  return ratingItem ? ratingItem.count : 0
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

/* 评价统计样式 */
.review-stats {
  margin: 20px 0;
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 4px;
}

.stats-header {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.rating-average {
  display: flex;
  align-items: baseline;
  gap: 10px;
}

.average-score {
  font-size: 24px;
  font-weight: bold;
  color: #f56c6c;
}

.review-count {
  color: #909399;
  font-size: 14px;
}

.rating-distribution {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.rating-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.rating-star {
  width: 40px;
  font-size: 14px;
  color: #606266;
}

.rating-bar-container {
  flex: 1;
  height: 8px;
  background-color: #ebeef5;
  border-radius: 4px;
  overflow: hidden;
}

.rating-bar {
  height: 100%;
  background-color: #f56c6c;
  border-radius: 4px;
  transition: width 0.3s ease;
}

.rating-distribution .rating-count {
  width: 40px;
  text-align: right;
  font-size: 12px;
  color: #909399;
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

.review-controls {
  display: flex;
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
