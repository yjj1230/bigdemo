<template>
  <div class="notification-container">
    <div class="header-section">
      <h1 class="page-title">消息中心</h1>
      <p class="page-subtitle">查看您的所有消息通知</p>
    </div>

    <div class="navigation-bar">
      <router-link to="/" class="nav-link">
        <span class="nav-icon">🏠</span>
        <span class="nav-text">首页</span>
      </router-link>
      <router-link to="/products" class="nav-link">
        <span class="nav-icon">🛍️</span>
        <span class="nav-text">商品</span>
      </router-link>
      <router-link to="/cart" class="nav-link">
        <span class="nav-icon">🛒</span>
        <span class="nav-text">购物车</span>
      </router-link>
      <router-link to="/orders" class="nav-link">
        <span class="nav-icon">📋</span>
        <span class="nav-text">我的订单</span>
      </router-link>
      <router-link to="/profile" class="nav-link">
        <span class="nav-icon">👤</span>
        <span class="nav-text">个人中心</span>
      </router-link>
    </div>

    <div class="notification-content">
      <div class="notification-tabs">
        <el-button
          :type="activeTab === 'all' ? 'primary' : 'default'"
          @click="activeTab = 'all'"
          class="tab-button"
        >
          全部通知
        </el-button>
        <el-button
          :type="activeTab === 'unread' ? 'primary' : 'default'"
          @click="activeTab = 'unread'"
          class="tab-button"
        >
          未读通知
          <el-badge v-if="unreadCount > 0" :value="unreadCount" class="badge" />
        </el-button>
      </div>

      <div class="notification-actions">
        <el-button @click="markAllAsRead" :disabled="unreadCount === 0">
          全部标记已读
        </el-button>
      </div>

      <div v-if="filteredNotifications.length > 0" class="notification-list">
        <div
          v-for="notification in filteredNotifications"
          :key="notification.id"
          class="notification-item"
          :class="{ unread: !notification.isRead }"
          @click="handleNotificationClick(notification)"
        >
          <div class="notification-icon">
            <span v-if="notification.type === '订单通知'">📦</span>
            <span v-else-if="notification.type === '系统通知'">🔔</span>
            <span v-else-if="notification.type === '营销通知'">🎉</span>
            <span v-else>📬</span>
          </div>
          <div class="notification-content">
            <div class="notification-header">
              <h4 class="notification-title">{{ notification.title }}</h4>
              <span class="notification-time">{{ formatTime(notification.createTime) }}</span>
            </div>
            <p class="notification-text">{{ notification.content }}</p>
            <div class="notification-footer">
              <el-tag :type="getTypeColor(notification.type)" size="small">
                {{ notification.type }}
              </el-tag>
              <el-button
                type="danger"
                size="small"
                text
                @click.stop="deleteNotification(notification.id)"
              >
                删除
              </el-button>
            </div>
          </div>
          <div v-if="!notification.isRead" class="unread-dot"></div>
        </div>
      </div>

      <div v-else class="empty-notification">
        <el-empty :description="activeTab === 'unread' ? '暂无未读通知' : '暂无通知'" />
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getNotificationsByUserId, getUnreadNotifications, getUnreadCount, markAsRead, markAllAsRead, deleteNotification } from '@/api/notification'
import { ElMessage, ElMessageBox } from 'element-plus'

export default {
  name: 'Notification',
  setup() {
    const userStore = useUserStore()
    const activeTab = ref('all')
    const allNotifications = ref([])
    const unreadNotifications = ref([])
    const unreadCount = ref(0)

    onMounted(async () => {
      await loadNotifications()
      await loadUnreadCount()
    })

    const filteredNotifications = computed(() => {
      return activeTab.value === 'all' ? allNotifications.value : unreadNotifications.value
    })

    const loadNotifications = async () => {
      try {
        const res = await getNotificationsByUserId(userStore.userInfo.id)
        allNotifications.value = res.data || []
      } catch (error) {
        console.error('获取通知失败', error)
      }
    }

    const loadUnreadNotifications = async () => {
      try {
        const res = await getUnreadNotifications(userStore.userInfo.id)
        unreadNotifications.value = res.data || []
      } catch (error) {
        console.error('获取未读通知失败', error)
      }
    }

    const loadUnreadCount = async () => {
      try {
        const res = await getUnreadCount(userStore.userInfo.id)
        unreadCount.value = res.data || 0
      } catch (error) {
        console.error('获取未读数量失败', error)
      }
    }

    const handleNotificationClick = async (notification) => {
      if (!notification.isRead) {
        try {
          await markAsRead(notification.id)
          notification.isRead = true
          unreadCount.value--
          await loadUnreadNotifications()
        } catch (error) {
          ElMessage.error('标记已读失败')
        }
      }
    }

    const markAllAsRead = async () => {
      try {
        await markAllAsRead(userStore.userInfo.id)
        ElMessage.success('已全部标记为已读')
        await loadNotifications()
        await loadUnreadNotifications()
        unreadCount.value = 0
      } catch (error) {
        ElMessage.error('操作失败')
      }
    }

    const deleteNotification = async (id) => {
      try {
        await ElMessageBox.confirm('确定要删除这条通知吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        await deleteNotification(id)
        ElMessage.success('删除成功')
        await loadNotifications()
        await loadUnreadNotifications()
        await loadUnreadCount()
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('删除失败')
        }
      }
    }

    const getTypeColor = (type) => {
      const colorMap = {
        '订单通知': 'primary',
        '系统通知': 'warning',
        '营销通知': 'success'
      }
      return colorMap[type] || 'info'
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

    return {
      activeTab,
      filteredNotifications,
      unreadCount,
      handleNotificationClick,
      markAllAsRead,
      deleteNotification,
      getTypeColor,
      formatTime
    }
  }
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Orbitron:wght@400;500;600;700&family=Noto+Sans+SC:wght@300;400;500;600&display=swap');

:root {
  --primary-color: #00ff88;
  --secondary-color: #00d4ff;
  --accent-color: #ff6b9d;
  --bg-dark: #0a0e27;
  --bg-card: #1a1f3a;
  --bg-lighter: #252b4d;
  --text-primary: #ffffff;
  --text-secondary: #a0a8c0;
  --border-color: #2d3561;
}

.notification-container {
  padding: 40px 20px;
  max-width: 1200px;
  margin: 0 auto;
  background: linear-gradient(180deg, var(--bg-dark) 0%, #0f142e 100%);
  min-height: 100vh;
  font-family: 'Noto Sans SC', sans-serif;
}

.header-section {
  text-align: center;
  margin-bottom: 50px;
}

.page-title {
  font-family: 'Orbitron', sans-serif;
  font-size: 36px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 10px 0;
  text-transform: uppercase;
  letter-spacing: 2px;
}

.page-subtitle {
  font-size: 16px;
  color: var(--text-secondary);
  margin: 0;
}

.navigation-bar {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-bottom: 40px;
  padding: 20px;
  background: var(--bg-card);
  border-radius: 12px;
  border: 1px solid var(--border-color);
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  background: var(--bg-lighter);
  border-radius: 8px;
  text-decoration: none;
  color: var(--text-primary);
  font-weight: 500;
  transition: all 0.3s ease;
  border: 1px solid transparent;
}

.nav-link:hover {
  background: linear-gradient(135deg, rgba(0, 255, 136, 0.15) 0%, rgba(0, 212, 255, 0.15) 100%);
  border-color: var(--primary-color);
  transform: translateY(-2px);
}

.nav-icon {
  font-size: 20px;
}

.nav-text {
  font-size: 14px;
  font-weight: 500;
}

.notification-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.notification-tabs {
  display: flex;
  gap: 16px;
  padding: 20px;
  background: var(--bg-card);
  border-radius: 12px;
  border: 1px solid var(--border-color);
}

.tab-button {
  padding: 12px 24px;
  font-size: 14px;
}

.badge {
  margin-left: 8px;
}

.notification-actions {
  display: flex;
  justify-content: flex-end;
  padding: 16px;
  background: var(--bg-card);
  border-radius: 12px;
  border: 1px solid var(--border-color);
}

.notification-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.notification-item {
  display: flex;
  gap: 16px;
  padding: 20px;
  background: var(--bg-card);
  border-radius: 12px;
  border: 1px solid var(--border-color);
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
}

.notification-item:hover {
  background: linear-gradient(135deg, rgba(0, 255, 136, 0.05) 0%, rgba(0, 212, 255, 0.05) 100%);
  border-color: var(--primary-color);
  transform: translateX(4px);
}

.notification-item.unread {
  background: linear-gradient(135deg, rgba(0, 255, 136, 0.1) 0%, rgba(0, 212, 255, 0.1) 100%);
}

.notification-icon {
  font-size: 32px;
  flex-shrink: 0;
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.notification-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.notification-time {
  font-size: 12px;
  color: var(--text-secondary);
}

.notification-text {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.6;
}

.notification-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.unread-dot {
  position: absolute;
  top: 20px;
  right: 20px;
  width: 8px;
  height: 8px;
  background: var(--accent-color);
  border-radius: 50%;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

.empty-notification {
  padding: 60px 20px;
  text-align: center;
}
</style>