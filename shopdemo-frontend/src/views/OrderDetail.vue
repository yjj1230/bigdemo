<template>
  <div class="order-detail-container">
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
        <span v-if="userStore.token">欢迎，{{ userStore.userInfo.username }}</span>
        <router-link v-else to="/login">登录</router-link>
      </div>
    </el-header>

    <el-main class="main-content">
      <el-card v-if="order" class="order-card">
        <template #header>
          <div class="order-header">
            <h2>订单详情</h2>
            <el-tag :type="getStatusType(order.status)">
              {{ getStatusText(order.status) }}
            </el-tag>
          </div>
        </template>
        
        <div class="order-info">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="订单号">{{ order.orderNo }}</el-descriptions-item>
            <el-descriptions-item label="下单时间">{{ order.createTime }}</el-descriptions-item>
            <el-descriptions-item label="订单金额">
              <span class="amount">¥{{ order.totalAmount }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="收货人">{{ order.receiverName }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ order.receiverPhone }}</el-descriptions-item>
            <el-descriptions-item label="收货地址" :span="2">{{ order.address }}</el-descriptions-item>
            <el-descriptions-item label="备注">{{ order.remark || '无' }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <h3>商品信息</h3>
        <el-table :data="order.items" style="margin-top: 20px;">
          <el-table-column label="商品信息" width="400">
            <template #default="{ row }">
              <div class="product-info">
                <img :src="row.product.image" class="product-image" />
                <div>
                  <h4>{{ row.product.name }}</h4>
                  <p>{{ row.product.description }}</p>
                </div>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="单价" width="120">
            <template #default="{ row }">
              <span>¥{{ row.product.price }}</span>
            </template>
          </el-table-column>
          
          <el-table-column label="数量" width="100">
            <template #default="{ row }">
              <span>{{ row.quantity }}</span>
            </template>
          </el-table-column>
          
          <el-table-column label="小计" width="120">
            <template #default="{ row }">
              <span class="subtotal">¥{{ (row.product.price * row.quantity).toFixed(2) }}</span>
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button 
                v-if="!reviewedProducts.includes(row.product.id)" 
                type="primary" 
                size="small" 
                @click="openReviewDialog(row.product)"
                :disabled="order.status !== 3"
              >
                评价
              </el-button>
              <el-button 
                v-else 
                type="info" 
                size="small" 
                disabled
              >
                已评价
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="order-timeline" v-if="order.timeline && order.timeline.length > 0">
          <h3>订单状态</h3>
          <el-timeline>
            <el-timeline-item
              v-for="(item, index) in order.timeline"
              :key="index"
              :timestamp="item.time"
              :type="getTimelineType(item.status)"
            >
              {{ item.description }}
            </el-timeline-item>
          </el-timeline>
        </div>
      </el-card>

      <el-card v-else class="loading-card">
        <el-skeleton :rows="5" animated />
      </el-card>
    </el-main>

    <!-- 评价对话框 -->
    <el-dialog
      v-model="reviewDialogVisible"
      :title="`评价商品：${currentProduct?.name || ''}`"
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getOrderDetail } from '@/api/order'
import { addReview, canReview } from '@/api/product'

const route = useRoute()
const order = ref(null)
const reviewDialogVisible = ref(false)
const currentProduct = ref(null)
const reviewForm = ref({
  rating: 5,
  content: ''
})
const reviewedProducts = ref([]) // 已评价商品ID列表
const submittingReview = ref(false) // 评价提交加载状态

onMounted(async () => {
  await loadOrderDetail()
})

const loadOrderDetail = async () => {
  try {
    const id = route.params.id
    order.value = await getOrderDetail(id)
  } catch (error) {
    ElMessage.error('获取订单详情失败')
  }
}

const openReviewDialog = async (product) => {
  currentProduct.value = product
  reviewForm.value = {
    rating: 5,
    content: ''
  }
  
  // 检查是否可以评价
  try {
    const can = await canReview(product.id)
    if (can) {
      reviewDialogVisible.value = true
    } else {
      ElMessage.warning('您还没有购买过该商品，无法评价')
    }
  } catch (error) {
    ElMessage.error('检查评价权限失败')
  }
}

const submitReview = async () => {
  if (!currentProduct.value) return
  
  if (!reviewForm.value.content.trim()) {
    ElMessage.warning('请输入评价内容')
    return
  }
  
  submittingReview.value = true
  
  try {
    await addReview({
      productId: currentProduct.value.id,
      rating: reviewForm.value.rating,
      content: reviewForm.value.content
    })
    ElMessage.success('评价成功')
    reviewDialogVisible.value = false
    
    // 添加到已评价商品列表
    if (!reviewedProducts.value.includes(currentProduct.value.id)) {
      reviewedProducts.value.push(currentProduct.value.id)
    }
  } catch (error) {
    ElMessage.error('评价失败：' + (error.message || '未知错误'))
  } finally {
    submittingReview.value = false
  }
}

const getStatusType = (status) => {
  const statusMap = {
    0: 'warning',
    1: 'success',
    2: 'primary',
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

const getTimelineType = (status) => {
  const typeMap = {
    0: 'warning',
    1: 'success',
    2: 'primary',
    3: 'success',
    4: 'danger'
  }
  return typeMap[status] || 'info'
}
</script>

<style scoped>
.order-detail-container {
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

.main-content {
  flex: 1;
  padding: 20px;
}

.order-card {
  margin-bottom: 20px;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-header h2 {
  margin: 0;
  color: #303133;
}

.amount {
  color: #f56c6c;
  font-size: 24px;
  font-weight: bold;
}

.order-info {
  margin: 20px 0;
}

h3 {
  margin: 30px 0 20px 0;
  color: #303133;
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

.subtotal {
  color: #f56c6c;
  font-weight: bold;
}

.order-timeline {
  margin-top: 30px;
}

.loading-card {
  text-align: center;
  margin-top: 100px;
}
</style>
