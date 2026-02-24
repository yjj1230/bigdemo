<template>
  <div class="logistics-container">
    <div class="header-section">
      <h1 class="page-title">物流追踪</h1>
      <p class="page-subtitle">实时追踪您的订单物流信息</p>
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

    <div class="logistics-content">
      <div class="search-section">
        <el-card class="search-card">
          <h3 class="section-title">物流查询</h3>
          <div class="search-form">
            <el-form :inline="true" @submit.prevent="searchByLogisticsNo">
              <el-form-item>
                <el-input 
                  v-model="logisticsNo" 
                  placeholder="请输入物流单号"
                  class="search-input"
                  clearable
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" native-type="submit" :loading="loading" class="search-button">
                  <span class="button-text">查询</span>
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-card>
      </div>

      <div v-if="logisticsList.length > 0" class="result-section">
        <el-card class="result-card">
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
              <el-tag :type="getStatusType(logisticsList[0].status)" class="status-tag">
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
                :type="getTimelineType(index)"
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
        </el-card>
      </div>

      <div v-else-if="hasSearched" class="empty-section">
        <el-empty description="未找到物流信息，请检查物流单号是否正确" />
      </div>

      <div v-else class="empty-section">
        <el-empty description="请输入物流单号查询物流信息" />
      </div>
    </div>
  </div>
</template>

<script>
import { trackLogistics } from '@/api/logistics'
import { ElMessage } from 'element-plus'

export default {
  name: 'Logistics',
  data() {
    return {
      logisticsNo: '',
      logisticsList: [],
      loading: false,
      hasSearched: false
    }
  },
  methods: {
    async searchByLogisticsNo() {
      if (!this.logisticsNo.trim()) {
        this.$message.warning('请输入物流单号')
        return
      }

      this.loading = true
      this.hasSearched = true
      try {
        const res = await trackLogistics(this.logisticsNo)
        this.logisticsList = res.data || []
        if (this.logisticsList.length === 0) {
          this.$message.info('未找到物流信息')
        } else {
          this.$message.success('查询成功')
        }
      } catch (error) {
        this.$message.error('查询失败，请稍后重试')
        this.logisticsList = []
      } finally {
        this.loading = false
      }
    },

    getStatusType(status) {
      const statusMap = {
        '已揽收': 'success',
        '运输中': 'primary',
        '派送中': 'warning',
        '已签收': 'success'
      }
      return statusMap[status] || 'info'
    },

    getTimelineType(index) {
      return index === 0 ? 'primary' : 'success'
    },

    formatTime(time) {
      if (!time) return ''
      const date = new Date(time)
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      const hours = String(date.getHours()).padStart(2, '0')
      const minutes = String(date.getMinutes()).padStart(2, '0')
      return `${year}-${month}-${day} ${hours}:${minutes}`
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

.logistics-container {
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

.logistics-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.search-section {
  margin-bottom: 24px;
}

.search-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
}

.section-title {
  font-family: 'Orbitron', sans-serif;
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 20px 0;
}

.search-form {
  display: flex;
  align-items: center;
  gap: 16px;
}

.search-input {
  width: 400px;
}

.search-button {
  padding: 12px 32px;
  font-size: 16px;
  min-width: 120px;
  height: 48px;
}

.result-section {
  animation: fadeInUp 0.4s ease-out;
}

.result-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
}

.logistics-header {
  padding: 24px;
  border-bottom: 1px solid var(--border-color);
  margin-bottom: 24px;
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
  color: var(--text-secondary);
  font-size: 14px;
}

.header-info .value {
  color: var(--text-primary);
  font-size: 14px;
  font-weight: 600;
}

.status-tag {
  font-size: 14px;
  font-weight: 600;
}

.logistics-timeline {
  padding: 24px;
}

.timeline-content {
  padding-left: 12px;
}

.timeline-status {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.timeline-description {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.timeline-location {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--accent-color);
  font-size: 13px;
}

.location-icon {
  font-size: 16px;
}

.location-text {
  font-weight: 500;
}

.empty-section {
  padding: 60px 20px;
  text-align: center;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>