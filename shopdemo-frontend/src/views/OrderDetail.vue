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
            <el-descriptions-item label="收货地址" :span="2">{{ order.receiverAddress }}</el-descriptions-item>
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
          
          <el-table-column label="操作" width="200">
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
              <el-button 
                v-if="order.status >= 1 && order.status !== 4 && !hasRefund(row.id)"
                type="danger" 
                size="small" 
                @click="openRefundDialog(row)"
              >
                申请退款
              </el-button>
              <el-button 
                v-if="hasRefund(row.id)"
                type="info" 
                size="small" 
                disabled
              >
                已申请退款
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="logistics-section" v-if="order.status >= 2">
          <h3>物流追踪</h3>
          <div v-if="logisticsList.length > 0" class="logistics-content">
            <div class="logistics-header">
              <div class="header-info">
                <span class="label">物流单号：</span>
                <span class="value">{{ logisticsList[0].logisticsNo }}</span>
              </div>
              <div class="header-info">
                <span class="label">物流公司：</span>
                <span class="value">{{ logisticsList[0].logisticsCompany }}</span>
              </div>
              <div class="header-info">
                <span class="label">当前状态：</span>
                <el-tag :type="getLogisticsStatusType(logisticsList[0].status)" class="status-tag">
                  {{ logisticsList[0].status }}
                </el-tag>
              </div>
            </div>

            <div class="logistics-timeline">
              <el-timeline>
                <el-timeline-item
                  v-for="(item, index) in logisticsList"
                  :key="item.id"
                  :timestamp="formatTime(item.createTime)"
                  :type="getLogisticsTimelineType(index)"
                  placement="top"
                >
                  <div class="timeline-content">
                    <div class="timeline-status">{{ item.status }}</div>
                    <div class="timeline-description">{{ item.description }}</div>
                    <div v-if="item.location" class="timeline-location">
                      <span class="location-icon">📍</span>
                      <span class="location-text">{{ item.location }}</span>
                    </div>
                  </div>
                </el-timeline-item>
              </el-timeline>
            </div>
          </div>
          <div v-else class="empty-logistics">
            <el-empty description="暂无物流信息，订单发货后将显示物流轨迹" />
          </div>
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

    <!-- 退款对话框 -->
    <el-dialog
      v-model="refundDialogVisible"
      :title="`申请退款：${currentOrderItem?.product?.name || ''}`"
      width="600px"
    >
      <el-form :model="refundForm" label-width="100px">
        <el-form-item label="商品名称">
          <span>{{ currentOrderItem?.product?.name }}</span>
        </el-form-item>
        <el-form-item label="购买数量">
          <span>{{ currentOrderItem?.quantity }}</span>
        </el-form-item>
        <el-form-item label="单价">
          <span>¥{{ currentOrderItem?.product?.price }}</span>
        </el-form-item>
        <el-form-item label="退款金额">
          <el-input-number v-model="refundForm.refundAmount" :min="0.01" :max="currentOrderItem?.product?.price * currentOrderItem?.quantity" :precision="2" :step="0.01" />
          <span class="refund-hint">最多可退款 ¥{{ (currentOrderItem?.product?.price * currentOrderItem?.quantity).toFixed(2) }}</span>
        </el-form-item>
        <el-form-item label="退款原因">
          <el-select v-model="refundForm.reason" placeholder="请选择退款原因" style="width: 100%">
            <el-option label="商品质量问题" value="商品质量问题" />
            <el-option label="商品描述不符" value="商品描述不符" />
            <el-option label="发错商品" value="发错商品" />
            <el-option label="商品损坏" value="商品损坏" />
            <el-option label="不想要了" value="不想要了" />
            <el-option label="其他原因" value="其他原因" />
          </el-select>
        </el-form-item>
        <el-form-item label="详细说明">
          <el-input
            v-model="refundForm.description"
            type="textarea"
            :rows="4"
            placeholder="请详细说明退款原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="refundDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="submitRefund" :loading="submittingRefund">提交退款申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { getOrderDetail } from '@/api/order'
import { addReview, canReview } from '@/api/product'
import { getLogisticsByOrderId } from '@/api/logistics'
import { getRefundsByOrderId, createRefund } from '@/api/refund'

const route = useRoute()
const userStore = useUserStore()
const order = ref(null)
const logisticsList = ref([])
const refundList = ref([])
const reviewDialogVisible = ref(false)
const refundDialogVisible = ref(false)
const currentProduct = ref(null)
const currentOrderItem = ref(null)
const reviewForm = ref({
  rating: 5,
  content: ''
})
const refundForm = ref({
  refundAmount: 0,
  reason: '',
  description: ''
})
const reviewedProducts = ref([])
const submittingReview = ref(false)
const submittingRefund = ref(false)

onMounted(async () => {
  await loadOrderDetail()
  await loadLogistics()
  await loadRefunds()
})

const loadOrderDetail = async () => {
  try {
    const id = route.params.id
    order.value = await getOrderDetail(id)
    console.log('订单详情:', order.value)
  } catch (error) {
    console.error('获取订单详情失败', error)
    ElMessage.error('获取订单详情失败')
  }
}

const loadLogistics = async () => {
  try {
    const id = route.params.id
    const res = await getLogisticsByOrderId(id)
    logisticsList.value = res || []
    console.log('物流信息:', logisticsList.value)
  } catch (error) {
    console.log('暂无物流信息', error)
    logisticsList.value = []
  }
}

const loadRefunds = async () => {
  try {
    const id = route.params.id
    const res = await getRefundsByOrderId(id)
    refundList.value = res || []
    console.log('退款信息:', refundList.value)
  } catch (error) {
    console.log('暂无退款信息', error)
    refundList.value = []
  }
}

const hasRefund = (orderItemId) => {
  return refundList.value.some(refund => 
    refund.orderItemId === orderItemId && refund.status !== '审核拒绝'
  )
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

const openRefundDialog = (orderItem) => {
  currentOrderItem.value = orderItem
  refundForm.value = {
    refundAmount: orderItem.product.price * orderItem.quantity,
    reason: '',
    description: ''
  }
  refundDialogVisible.value = true
}

const submitRefund = async () => {
  if (!currentOrderItem.value) return
  
  if (!refundForm.value.reason) {
    ElMessage.warning('请选择退款原因')
    return
  }
  
  if (!refundForm.value.description.trim()) {
    ElMessage.warning('请输入详细说明')
    return
  }
  
  submittingRefund.value = true
  
  try {
    await createRefund({
      orderId: order.value.id,
      orderItemId: currentOrderItem.value.id,
      reason: refundForm.value.reason + '：' + refundForm.value.description,
      refundAmount: refundForm.value.refundAmount
    })
    ElMessage.success('退款申请已提交，请等待审核')
    refundDialogVisible.value = false
  } catch (error) {
    if (error.message && error.message.includes('已申请退款')) {
      ElMessage.error(error.message)
    } else {
      ElMessage.error('退款申请失败：' + (error.message || '未知错误'))
    }
  } finally {
    submittingRefund.value = false
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

const getLogisticsTimelineType = (index) => {
  return index === 0 ? 'primary' : 'success'
}

const getLogisticsStatusType = (status) => {
  const statusMap = {
    '已揽收': 'success',
    '运输中': 'primary',
    '派送中': 'warning',
    '已签收': 'success'
  }
  return statusMap[status] || 'info'
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
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

.refund-hint {
  margin-left: 10px;
  color: #909399;
  font-size: 12px;
}

.order-timeline {
  margin-top: 30px;
}

.logistics-section {
  margin-top: 30px;
}

.logistics-content {
  background: #f5f7fa;
  padding: 20px;
  border-radius: 8px;
}

.logistics-header {
  padding: 16px;
  background: #fff;
  border-radius: 8px;
  margin-bottom: 20px;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 16px;
}

.header-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-info .label {
  color: #909399;
  font-size: 14px;
}

.header-info .value {
  color: #303133;
  font-size: 14px;
  font-weight: 600;
}

.status-tag {
  font-size: 14px;
  font-weight: 600;
}

.logistics-timeline {
  padding: 16px;
  background: #fff;
  border-radius: 8px;
}

.timeline-content {
  padding-left: 12px;
}

.timeline-status {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.timeline-description {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.timeline-location {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #409eff;
  font-size: 13px;
}

.location-icon {
  font-size: 16px;
}

.location-text {
  font-weight: 500;
}

.empty-logistics {
  padding: 40px 20px;
  text-align: center;
}

.loading-card {
  text-align: center;
  margin-top: 100px;
}
</style>
