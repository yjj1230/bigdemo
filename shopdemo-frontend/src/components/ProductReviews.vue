<template>
  <div class="reviews-container">
    <!-- 评价统计卡片 -->
    <div class="reviews-summary">
      <div class="summary-card">
        <div class="rating-display">
          <div class="average-rating">{{ averageRating }}</div>
          <div class="rating-stars">
            <span v-for="i in 5" :key="i" class="star" :class="{ active: i <= averageRating }">★</span>
          </div>
          <div class="total-reviews">共 {{ totalReviews }} 条评价</div>
        </div>
        <div class="rating-distribution">
          <div v-for="i in 5" :key="i" class="rating-bar">
            <span class="rating-label">{{ 6 - i }}星</span>
            <div class="bar-container">
              <div class="bar-fill" :style="{ width: getRatingPercentage(6 - i) + '%' }"></div>
            </div>
            <span class="rating-count">{{ getRatingCount(6 - i) }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 评价筛选 -->
    <div class="reviews-filter">
      <el-radio-group v-model="filterType" @change="handleFilterChange" class="filter-group">
        <el-radio-button label="all">全部评价</el-radio-button>
        <el-radio-button label="5">5星好评</el-radio-button>
        <el-radio-button label="4">4星好评</el-radio-button>
        <el-radio-button label="3">3星中评</el-radio-button>
        <el-radio-button label="2">2星差评</el-radio-button>
        <el-radio-button label="1">1星差评</el-radio-button>
        <el-radio-button label="hasImage">有图评价</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 评价列表 -->
    <div class="reviews-list">
      <div v-if="loading" class="loading-state">
        <el-icon class="is-loading"><loading /></el-icon>
        <span>加载中...</span>
      </div>
      
      <div v-else-if="filteredReviews.length === 0" class="empty-state">
        <span class="empty-icon">📝</span>
        <p>暂无评价</p>
      </div>
      
      <div v-else class="review-items">
        <div v-for="review in filteredReviews" :key="review.id" class="review-item">
          <div class="review-header">
            <div class="user-info">
              <span class="username">{{ review.isAnonymous ? '匿名用户' : review.username }}</span>
              <div class="rating-stars">
                <span v-for="i in 5" :key="i" class="star" :class="{ active: i <= review.rating }">★</span>
              </div>
            </div>
            <span class="review-time">{{ formatTime(review.createTime) }}</span>
          </div>
          
          <div class="review-content">
            <p class="review-text">{{ review.content }}</p>
            <div v-if="review.images" class="review-images">
              <img 
                v-for="(image, index) in getImages(review.images)" 
                :key="index" 
                :src="image" 
                alt="评价图片"
                class="review-image"
                @click="previewImage(image)"
              />
            </div>
          </div>
          
          <div class="review-footer">
            <div class="review-actions">
              <el-button 
                :icon="review.isLiked ? 'filled' : 'outline'" 
                size="small" 
                @click="toggleLike(review)"
                class="like-btn"
              >
                <span class="like-icon">👍</span>
                <span class="like-count">{{ review.likeCount || 0 }}</span>
              </el-button>
            </div>
            
            <div v-if="review.reply" class="merchant-reply">
              <div class="reply-header">
                <span class="reply-label">商家回复：</span>
                <span class="reply-time">{{ formatTime(review.replyTime) }}</span>
              </div>
              <p class="reply-content">{{ review.reply }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="totalReviews"
        :page-sizes="[10, 20, 30, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        class="pagination"
      />
    </div>

    <!-- 图片预览 -->
    <el-image-viewer
      v-if="showImageViewer"
      :url-list="previewImages"
      :initial-index="currentImageIndex"
      @close="showImageViewer = false"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Loading } from '@element-plus/icons-vue'
import { ElImageViewer } from 'element-plus'
import { getReviews, getReviewStatistics } from '@/api/product'

const props = defineProps({
  productId: {
    type: Number,
    required: true
  }
})

const loading = ref(false)
const filterType = ref('all')
const currentPage = ref(1)
const pageSize = ref(10)
const reviews = ref([])
const statistics = ref({})
const showImageViewer = ref(false)
const previewImages = ref([])
const currentImageIndex = ref(0)

const averageRating = computed(() => {
  return statistics.value.averageRating ? statistics.value.averageRating.toFixed(1) : '0.0'
})

const totalReviews = computed(() => {
  return statistics.value.totalCount || 0
})

const filteredReviews = computed(() => {
  if (filterType.value === 'all') {
    return reviews.value
  } else if (filterType.value === 'hasImage') {
    return reviews.value.filter(review => review.images)
  } else {
    return reviews.value.filter(review => review.rating === parseInt(filterType.value))
  }
})

const getRatingPercentage = (rating) => {
  const count = getRatingCount(rating)
  const total = totalReviews.value
  return total > 0 ? (count / total) * 100 : 0
}

const getRatingCount = (rating) => {
  return statistics.value.ratingDistribution?.[rating] || 0
}

const getImages = (images) => {
  if (!images) return []
  try {
    return JSON.parse(images)
  } catch {
    return []
  }
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (days === 0) return '今天'
  if (days === 1) return '昨天'
  if (days < 7) return `${days}天前`
  
  return date.toLocaleDateString('zh-CN')
}

const toggleLike = (review) => {
  review.isLiked = !review.isLiked
  review.likeCount = review.isLiked ? (review.likeCount || 0) + 1 : (review.likeCount || 1) - 1
}

const previewImage = (image) => {
  previewImages.value = [image]
  currentImageIndex.value = 0
  showImageViewer.value = true
}

const handleFilterChange = () => {
  currentPage.value = 1
}

const handleSizeChange = (size) => {
  pageSize.value = size
  loadReviews()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  loadReviews()
}

const loadReviews = async () => {
  loading.value = true
  try {
    const response = await getReviews(props.productId)
    if (response.code === 200) {
      reviews.value = response.data.records || []
    }
  } catch (error) {
    console.error('加载评价失败:', error)
  } finally {
    loading.value = false
  }
}

const loadStatistics = async () => {
  try {
    const response = await getReviewStatistics(props.productId)
    if (response.code === 200) {
      statistics.value = response.data || {}
    }
  } catch (error) {
    console.error('加载评价统计失败:', error)
  }
}

onMounted(() => {
  loadReviews()
  loadStatistics()
})
</script>

<style scoped>
.reviews-container {
  padding: 20px;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  min-height: 100vh;
  color: #e0e0e0;
}

.reviews-summary {
  margin-bottom: 20px;
}

.summary-card {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(0, 255, 255, 0.1);
  border-radius: 12px;
  padding: 24px;
  display: flex;
  gap: 40px;
}

.rating-display {
  text-align: center;
  min-width: 200px;
}

.average-rating {
  font-size: 48px;
  font-weight: bold;
  color: #00ff88;
  margin-bottom: 8px;
}

.rating-stars {
  font-size: 24px;
  margin-bottom: 8px;
}

.star {
  color: #444;
  margin: 0 2px;
  transition: color 0.3s;
}

.star.active {
  color: #ffd700;
}

.total-reviews {
  color: #888;
  font-size: 14px;
}

.rating-distribution {
  flex: 1;
}

.rating-bar {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.rating-label {
  width: 40px;
  font-size: 14px;
  color: #888;
}

.bar-container {
  flex: 1;
  height: 8px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
  margin: 0 12px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #00ff88, #00d4ff);
  border-radius: 4px;
  transition: width 0.3s;
}

.rating-count {
  width: 40px;
  text-align: right;
  font-size: 14px;
  color: #888;
}

.reviews-filter {
  margin-bottom: 20px;
}

.filter-group {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.reviews-list {
  min-height: 400px;
}

.loading-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #888;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.review-items {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.review-item {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(0, 255, 255, 0.1);
  border-radius: 12px;
  padding: 20px;
  transition: all 0.3s;
}

.review-item:hover {
  border-color: rgba(0, 255, 255, 0.3);
  box-shadow: 0 8px 24px rgba(0, 255, 255, 0.1);
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.username {
  font-weight: 500;
  color: #e0e0e0;
}

.review-time {
  font-size: 14px;
  color: #888;
}

.review-content {
  margin-bottom: 16px;
}

.review-text {
  color: #c0c0c0;
  line-height: 1.6;
  margin-bottom: 12px;
}

.review-images {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.review-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.3s;
}

.review-image:hover {
  transform: scale(1.1);
}

.review-footer {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.review-actions {
  display: flex;
  gap: 8px;
}

.like-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 12px;
  background: rgba(0, 255, 255, 0.1);
  border: 1px solid rgba(0, 255, 255, 0.2);
  color: #00ff88;
  border-radius: 20px;
  transition: all 0.3s;
}

.like-btn:hover {
  background: rgba(0, 255, 255, 0.2);
  border-color: rgba(0, 255, 255, 0.4);
}

.like-icon {
  font-size: 16px;
}

.like-count {
  font-size: 14px;
}

.merchant-reply {
  flex: 1;
  background: rgba(255, 136, 0, 0.1);
  border-left: 3px solid #ff8800;
  padding: 12px;
  border-radius: 8px;
}

.reply-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.reply-label {
  font-weight: 500;
  color: #ff8800;
}

.reply-time {
  font-size: 12px;
  color: #888;
}

.reply-content {
  color: #c0c0c0;
  font-size: 14px;
  line-height: 1.6;
}

.pagination-container {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

.pagination {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(0, 255, 255, 0.1);
  border-radius: 8px;
  padding: 8px 16px;
}

@media (max-width: 768px) {
  .summary-card {
    flex-direction: column;
    gap: 20px;
  }
  
  .rating-display {
    min-width: auto;
  }
  
  .review-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .review-footer {
    flex-direction: column;
  }
}
</style>
