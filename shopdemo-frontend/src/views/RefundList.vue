<template>
  <div class="refund-list-container">
    <el-header class="header">
      <div class="logo">商城系统</div>
      <div class="nav">
        <router-link to="/home">首页</router-link>
        <router-link to="/products">商品</router-link>
        <router-link to="/cart">购物车</router-link>
        <router-link to="/orders">订单</router-link>
        <router-link to="/refunds">退款</router-link>
        <router-link to="/profile">个人中心</router-link>
      </div>
      <div class="user-info">
        <span v-if="userStore.token">欢迎，{{ userStore.userInfo.username }}</span>
        <router-link v-else to="/login">登录</router-link>
      </div>
    </el-header>

    <el-main class="main-content">
      <el-card class="refund-card">
        <template #header>
          <div class="card-header">
            <h2>我的退款</h2>
            <el-button type="primary" :icon="Refresh" @click="loadRefunds" :loading="loading">
              刷新
            </el-button>
          </div>
        </template>

        <el-tabs v-model="activeTab" @tab-change="handleTabChange">
          <el-tab-pane label="全部退款" name="all">
            <el-table :data="allRefunds" style="width: 100%">
              <el-table-column prop="refundNo" label="退款单号" width="180" />
              <el-table-column prop="orderId" label="订单ID" width="100" />
              <el-table-column prop="refundAmount" label="退款金额" width="120">
                <template #default="{ row }">
                  <span class="amount">¥{{ row.refundAmount }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="reason" label="退款原因" width="200" />
              <el-table-column prop="status" label="状态" width="120">
                <template #default="{ row }">
                  <el-tag :type="getStatusType(row.status)">
                    {{ row.status }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createTime" label="申请时间" width="180" />
              <el-table-column label="操作" width="120">
                <template #default="{ row }">
                  <el-button size="small" @click="viewRefundDetail(row)">
                    查看详情
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <el-tab-pane label="待审核" name="pending">
            <el-table :data="pendingRefunds" style="width: 100%">
              <el-table-column prop="refundNo" label="退款单号" width="180" />
              <el-table-column prop="orderId" label="订单ID" width="100" />
              <el-table-column prop="refundAmount" label="退款金额" width="120">
                <template #default="{ row }">
                  <span class="amount">¥{{ row.refundAmount }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="reason" label="退款原因" width="200" />
              <el-table-column prop="createTime" label="申请时间" width="180" />
              <el-table-column label="操作" width="120">
                <template #default="{ row }">
                  <el-button size="small" @click="viewRefundDetail(row)">
                    查看详情
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <el-tab-pane label="退款中" name="processing">
            <el-table :data="processingRefunds" style="width: 100%">
              <el-table-column prop="refundNo" label="退款单号" width="180" />
              <el-table-column prop="orderId" label="订单ID" width="100" />
              <el-table-column prop="refundAmount" label="退款金额" width="120">
                <template #default="{ row }">
                  <span class="amount">¥{{ row.refundAmount }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="reason" label="退款原因" width="200" />
              <el-table-column prop="refundMethod" label="退款方式" width="120" />
              <el-table-column prop="createTime" label="申请时间" width="180" />
              <el-table-column label="操作" width="120">
                <template #default="{ row }">
                  <el-button size="small" @click="viewRefundDetail(row)">
                    查看详情
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <el-tab-pane label="已完成" name="completed">
            <el-table :data="completedRefunds" style="width: 100%">
              <el-table-column prop="refundNo" label="退款单号" width="180" />
              <el-table-column prop="orderId" label="订单ID" width="100" />
              <el-table-column prop="refundAmount" label="退款金额" width="120">
                <template #default="{ row }">
                  <span class="amount">¥{{ row.refundAmount }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="reason" label="退款原因" width="200" />
              <el-table-column prop="status" label="状态" width="120">
                <template #default="{ row }">
                  <el-tag :type="getStatusType(row.status)">
                    {{ row.status }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createTime" label="申请时间" width="180" />
              <el-table-column label="操作" width="120">
                <template #default="{ row }">
                  <el-button size="small" @click="viewRefundDetail(row)">
                    查看详情
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
        </el-tabs>

        <el-empty v-if="!loading && currentRefunds.length === 0" description="暂无退款记录" />
      </el-card>
    </el-main>

    <el-dialog
      v-model="detailDialogVisible"
      title="退款详情"
      width="600px"
    >
      <div v-if="currentRefund" class="refund-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="退款单号">
            {{ currentRefund.refundNo }}
          </el-descriptions-item>
          <el-descriptions-item label="订单ID">
            {{ currentRefund.orderId }}
          </el-descriptions-item>
          <el-descriptions-item label="退款金额">
            <span class="amount">¥{{ currentRefund.refundAmount }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="退款原因">
            {{ currentRefund.reason }}
          </el-descriptions-item>
          <el-descriptions-item label="退款状态">
            <el-tag :type="getStatusType(currentRefund.status)">
              {{ currentRefund.status }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="申请时间">
            {{ currentRefund.createTime }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间">
            {{ currentRefund.updateTime }}
          </el-descriptions-item>
        </el-descriptions>

        <div v-if="currentRefund.rejectReason" class="reject-reason">
          <h4>拒绝原因</h4>
          <p>{{ currentRefund.rejectReason }}</p>
        </div>

        <div v-if="currentRefund.refundMethod" class="refund-info">
          <h4>退款信息</h4>
          <p><strong>退款方式：</strong>{{ currentRefund.refundMethod }}</p>
          <p><strong>退款账户：</strong>{{ currentRefund.refundAccount }}</p>
        </div>

        <div v-if="currentRefund.status === '待审核'" class="tips">
          <el-alert
            title="温馨提示"
            type="info"
            :closable="false"
            description="您的退款申请已提交，管理员正在审核中，请耐心等待。审核结果将通过系统通知您。"
          />
        </div>

        <div v-if="currentRefund.status === '审核通过'" class="tips">
          <el-alert
            title="温馨提示"
            type="success"
            :closable="false"
            description="您的退款申请已审核通过，财务人员正在处理退款，请耐心等待。"
          />
        </div>

        <div v-if="currentRefund.status === '退款中'" class="tips">
          <el-alert
            title="温馨提示"
            type="warning"
            :closable="false"
            description="财务人员正在处理您的退款，预计1-3个工作日内到账。"
          />
        </div>

        <div v-if="currentRefund.status === '退款完成'" class="tips">
          <el-alert
            title="温馨提示"
            type="success"
            :closable="false"
            description="您的退款已完成，款项已退回至您的账户。"
          />
        </div>

        <div v-if="currentRefund.status === '审核拒绝'" class="tips">
          <el-alert
            title="温馨提示"
            type="error"
            :closable="false"
            :description="`您的退款申请已被拒绝。拒绝原因：${currentRefund.rejectReason}`"
          />
        </div>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { getRefundsByUserId } from '@/api/refund'

const userStore = useUserStore()
const loading = ref(false)
const activeTab = ref('all')
const allRefunds = ref([])
const detailDialogVisible = ref(false)
const currentRefund = ref(null)
let refreshTimer = null
let websocket = null

const currentRefunds = computed(() => {
  switch (activeTab.value) {
    case 'pending':
      return pendingRefunds.value
    case 'processing':
      return processingRefunds.value
    case 'completed':
      return completedRefunds.value
    default:
      return allRefunds.value
  }
})

const pendingRefunds = computed(() => {
  return allRefunds.value.filter(r => r.status === '待审核')
})

const processingRefunds = computed(() => {
  return allRefunds.value.filter(r => r.status === '退款中')
})

const completedRefunds = computed(() => {
  return allRefunds.value.filter(r => r.status === '退款完成')
})

onMounted(async () => {
  await loadRefunds()
  startAutoRefresh()
  connectWebSocket()
})

onUnmounted(() => {
  stopAutoRefresh()
  disconnectWebSocket()
})

const loadRefunds = async () => {
  if (!userStore.token) {
    ElMessage.warning('请先登录')
    return
  }

  loading.value = true
  try {
    const res = await getRefundsByUserId(userStore.userInfo.id)
    allRefunds.value = res || []
  } catch (error) {
    console.error('获取退款列表失败', error)
    ElMessage.error('获取退款列表失败')
  } finally {
    loading.value = false
  }
}

const handleTabChange = (tabName) => {
  activeTab.value = tabName
}

const viewRefundDetail = (refund) => {
  currentRefund.value = refund
  detailDialogVisible.value = true
}

const getStatusType = (status) => {
  const statusMap = {
    '待审核': 'warning',
    '审核通过': 'success',
    '审核拒绝': 'danger',
    '退款中': 'primary',
    '退款完成': 'success'
  }
  return statusMap[status] || 'info'
}

const startAutoRefresh = () => {
  refreshTimer = setInterval(() => {
    loadRefunds()
  }, 30000)
}

const stopAutoRefresh = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

const connectWebSocket = () => {
  if (!userStore.token) {
    console.log('无法连接WebSocket：未登录')
    return
  }

  const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const wsUrl = `${wsProtocol}//${window.location.host}/ws/refund?token=${userStore.token}`
  
  console.log('尝试连接退款WebSocket:', wsUrl)

  try {
    websocket = new WebSocket(wsUrl)

    websocket.onopen = () => {
      console.log('✓ 退款WebSocket连接成功')
    }

    websocket.onmessage = (event) => {
      console.log('收到WebSocket消息:', event.data)
      try {
        const message = JSON.parse(event.data)
        console.log('解析后的消息:', message)
        if (message.type === 'refund_update') {
          console.log('收到退款更新通知:', message.data)
          ElMessage.success('退款状态已更新，请查看最新进度')
          loadRefunds()
        }
      } catch (error) {
        console.error('处理WebSocket消息失败:', error)
      }
    }

    websocket.onerror = (error) => {
      console.error('✗ 退款WebSocket连接错误:', error)
    }

    websocket.onclose = (event) => {
      console.log('✗ 退款WebSocket连接已关闭, code:', event.code, 'reason:', event.reason)
      // 5秒后尝试重连
      setTimeout(() => {
        if (userStore.token) {
          console.log('尝试重新连接WebSocket...')
          connectWebSocket()
        }
      }, 5000)
    }
  } catch (error) {
    console.error('创建退款WebSocket连接失败:', error)
  }
}

const disconnectWebSocket = () => {
  if (websocket) {
    websocket.close()
    websocket = null
  }
}
</script>

<style scoped>
.refund-list-container {
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

.refund-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  color: #303133;
}

.amount {
  color: #f56c6c;
  font-size: 18px;
  font-weight: bold;
}

.refund-detail {
  padding: 20px 0;
}

.reject-reason {
  margin-top: 20px;
  padding: 15px;
  background: #fef0f0;
  border-left: 4px solid #f56c6c;
  border-radius: 4px;
}

.reject-reason h4 {
  margin: 0 0 10px 0;
  color: #f56c6c;
}

.reject-reason p {
  margin: 0;
  color: #606266;
}

.refund-info {
  margin-top: 20px;
  padding: 15px;
  background: #f0f9ff;
  border-left: 4px solid #409eff;
  border-radius: 4px;
}

.refund-info h4 {
  margin: 0 0 10px 0;
  color: #409eff;
}

.refund-info p {
  margin: 5px 0;
  color: #606266;
}

.tips {
  margin-top: 20px;
}
</style>
