<template>
  <div class="points-container">
    <div class="header-section">
      <h1 class="page-title">积分中心</h1>
      <p class="page-subtitle">赚取积分，兑换好礼</p>
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

    <div class="points-content">
      <div class="points-summary">
        <el-card class="summary-card">
          <div class="summary-content">
            <div class="points-display">
              <div class="points-icon">🎁</div>
              <div class="points-info">
                <div class="points-label">我的积分</div>
                <div class="points-value">{{ totalPoints }}</div>
              </div>
            </div>
            <div class="sign-in-section">
              <el-button 
                type="primary" 
                :disabled="hasSignedIn"
                @click="handleSignIn"
                :loading="signInLoading"
                class="sign-in-button"
              >
                <span v-if="!hasSignedIn">每日签到 +5积分</span>
                <span v-else>今日已签到</span>
              </el-button>
            </div>
          </div>
        </el-card>
      </div>

      <div class="points-rules">
        <el-card class="rules-card">
          <template #header>
            <h3 class="card-title">积分规则</h3>
          </template>
          <div class="rules-list">
            <div class="rule-item">
              <span class="rule-icon">🛒</span>
              <span class="rule-text">购物获得积分：每消费1元获得1积分</span>
            </div>
            <div class="rule-item">
              <span class="rule-icon">⭐</span>
              <span class="rule-text">评价获得积分：每次评价获得10积分</span>
            </div>
            <div class="rule-item">
              <span class="rule-icon">📅</span>
              <span class="rule-text">每日签到：每日签到获得5积分</span>
            </div>
            <div class="rule-item">
              <span class="rule-icon">🎁</span>
              <span class="rule-text">积分兑换：积分可在积分商城兑换商品</span>
            </div>
          </div>
        </el-card>
      </div>

      <div class="points-history">
        <el-card class="history-card">
          <template #header>
            <h3 class="card-title">积分记录</h3>
          </template>
          <div v-if="pointsList.length > 0" class="history-list">
            <div 
              v-for="item in pointsList" 
              :key="item.id" 
              class="history-item"
            >
              <div class="item-info">
                <div class="item-type">{{ item.type }}</div>
                <div class="item-description">{{ item.description }}</div>
                <div class="item-time">{{ formatTime(item.createTime) }}</div>
              </div>
              <div class="item-points" :class="{ positive: item.points > 0, negative: item.points < 0 }">
                {{ item.points > 0 ? '+' : '' }}{{ item.points }}
              </div>
            </div>
          </div>
          <div v-else class="empty-history">
            <el-empty description="暂无积分记录" />
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getTotalPoints, getRecentPoints, signIn } from '@/api/points'
import { ElMessage } from 'element-plus'

export default {
  name: 'Points',
  setup() {
    const userStore = useUserStore()
    const totalPoints = ref(0)
    const pointsList = ref([])
    const hasSignedIn = ref(false)
    const signInLoading = ref(false)

    onMounted(async () => {
      await loadTotalPoints()
      await loadRecentPoints()
      checkSignInStatus()
    })

    const loadTotalPoints = async () => {
      try {
        const res = await getTotalPoints(userStore.userInfo.id)
        totalPoints.value = res.data.totalPoints || 0
      } catch (error) {
        console.error('获取积分失败', error)
      }
    }

    const loadRecentPoints = async () => {
      try {
        const res = await getRecentPoints(userStore.userInfo.id, 20)
        pointsList.value = res.data || []
      } catch (error) {
        console.error('获取积分记录失败', error)
      }
    }

    const checkSignInStatus = () => {
      const today = new Date().toDateString()
      const lastSignIn = localStorage.getItem('lastSignInDate')
      hasSignedIn.value = lastSignIn === today
    }

    const handleSignIn = async () => {
      if (hasSignedIn.value) {
        ElMessage.warning('今日已签到')
        return
      }

      signInLoading.value = true
      try {
        await signIn(userStore.userInfo.id)
        ElMessage.success('签到成功，获得5积分')
        
        const today = new Date().toDateString()
        localStorage.setItem('lastSignInDate', today)
        hasSignedIn.value = true
        
        await loadTotalPoints()
        await loadRecentPoints()
      } catch (error) {
        ElMessage.error('签到失败：' + (error.message || '未知错误'))
      } finally {
        signInLoading.value = false
      }
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
      totalPoints,
      pointsList,
      hasSignedIn,
      signInLoading,
      handleSignIn,
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

.points-container {
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

.points-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.points-summary {
  margin-bottom: 24px;
}

.summary-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
}

.summary-content {
  padding: 40px;
}

.points-display {
  display: flex;
  align-items: center;
  gap: 24px;
  margin-bottom: 32px;
}

.points-icon {
  font-size: 64px;
}

.points-info {
  flex: 1;
}

.points-label {
  font-size: 16px;
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.points-value {
  font-family: 'Orbitron', sans-serif;
  font-size: 48px;
  font-weight: 700;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.sign-in-section {
  display: flex;
  justify-content: center;
}

.sign-in-button {
  padding: 16px 48px;
  font-size: 18px;
  min-width: 200px;
  height: 56px;
}

.points-rules {
  margin-bottom: 24px;
}

.rules-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
}

.card-title {
  font-family: 'Orbitron', sans-serif;
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.rules-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.rule-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: var(--bg-lighter);
  border-radius: 8px;
}

.rule-icon {
  font-size: 24px;
}

.rule-text {
  font-size: 14px;
  color: var(--text-secondary);
}

.points-history {
  margin-bottom: 24px;
}

.history-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.history-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: var(--bg-lighter);
  border-radius: 8px;
  transition: all 0.3s ease;
}

.history-item:hover {
  background: linear-gradient(135deg, rgba(0, 255, 136, 0.1) 0%, rgba(0, 212, 255, 0.1) 100%);
}

.item-info {
  flex: 1;
}

.item-type {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.item-description {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 4px;
}

.item-time {
  font-size: 12px;
  color: var(--text-secondary);
}

.item-points {
  font-family: 'Orbitron', sans-serif;
  font-size: 20px;
  font-weight: 700;
  padding: 8px 16px;
  border-radius: 8px;
  min-width: 80px;
  text-align: center;
}

.item-points.positive {
  color: var(--primary-color);
  background: rgba(0, 255, 136, 0.1);
}

.item-points.negative {
  color: var(--accent-color);
  background: rgba(255, 107, 157, 0.1);
}

.empty-history {
  padding: 60px 20px;
  text-align: center;
}
</style>