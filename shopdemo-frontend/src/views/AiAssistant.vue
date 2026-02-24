<template>
  <div class="ai-assistant-container">
    <!-- 顶部标题区域 -->
    <div class="header-section">
      <h1 class="page-title">AI 智能助手</h1>
      <p class="page-subtitle">智能科技，贴心服务</p>
    </div>
    
    <!-- 导航栏 -->
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
    
    <!-- 功能卡片网格 -->
    <div class="ai-features">
      <!-- 智能客服 -->
      <div class="ai-feature-card chat-card">
        <div class="card-header">
          <div class="icon-wrapper">
            <span class="icon">💬</span>
          </div>
          <h3 class="card-title">智能客服</h3>
        </div>
        <p class="card-description">结合您的订单、购物车等历史数据，智能回答您的问题</p>
        <div class="chat-container">
          <div class="chat-messages" ref="chatMessages">
            <div 
              v-for="(msg, index) in customerServiceMessages" 
              :key="index"
              :class="['message', msg.sender === '您' ? 'user-message' : 'bot-message']"
            >
              <span class="sender">{{ msg.sender }}</span>
              <span class="content">{{ msg.content }}</span>
            </div>
            <div v-if="customerServiceMessages.length === 0" class="empty-state">
              <span class="empty-icon">🤖</span>
              <p>开始与AI助手对话吧！</p>
            </div>
          </div>
          <div class="chat-input">
            <el-input 
              v-model="customerServiceQuestion" 
              placeholder="请输入您的问题，例如：我的订单什么时候能到？"
              @keyup.enter="sendCustomerServiceQuestion"
              class="custom-input"
            />
            <el-button 
              type="primary" 
              @click="sendCustomerServiceQuestion"
              class="send-button"
              :loading="loading"
            >
              <span class="button-text">发送</span>
            </el-button>
          </div>
        </div>
      </div>
      
      <!-- 智能搜索 -->
      <div class="ai-feature-card search-card">
        <div class="card-header">
          <div class="icon-wrapper">
            <span class="icon">🔍</span>
          </div>
          <h3 class="card-title">智能搜索</h3>
        </div>
        <p class="card-description">结合数据库商品信息，提供更智能的搜索结果</p>
        <div class="search-container">
          <el-input 
            v-model="searchQuery" 
            placeholder="请输入搜索词，例如：便宜的手机"
            @keyup.enter="performIntelligentSearch"
            class="custom-input"
          />
          <el-button 
            type="primary" 
            @click="performIntelligentSearch"
            class="search-button"
            :loading="loading"
          >
            <span class="button-text">搜索</span>
          </el-button>
          <div v-if="searchResult" class="result-container">
            <div class="result-header">
              <span class="result-icon">✨</span>
              <h4>搜索结果</h4>
            </div>
            <div class="result-content">{{ searchResult }}</div>
          </div>
          <div v-else-if="searchResult === ''" class="debug-info">
            <p>搜索结果为空字符串</p>
          </div>
          <div v-else class="debug-info">
            <p>搜索结果未设置</p>
          </div>
        </div>
      </div>
      
      <!-- 个性化推荐 -->
      <div class="ai-feature-card recommend-card">
        <div class="card-header">
          <div class="icon-wrapper">
            <span class="icon">🎯</span>
          </div>
          <h3 class="card-title">个性化推荐</h3>
        </div>
        <p class="card-description">基于您的购买历史和浏览行为，推荐适合您的商品</p>
        <div class="recommend-container">
          <el-form @submit.prevent="getPersonalizedRecommendation" class="custom-form">
            <el-form-item label="商品分类">
              <el-input v-model="recommendCategory" placeholder="例如：手机" class="custom-input" />
            </el-form-item>
            <el-form-item label="用户偏好">
              <el-input v-model="recommendPreferences" placeholder="例如：喜欢性价比高的产品" class="custom-input" />
            </el-form-item>
            <el-form-item label="推荐数量">
              <el-input-number v-model="recommendCount" :min="1" :max="20" :step="1" class="custom-number" />
            </el-form-item>
            <el-form-item label="推荐类型">
              <el-select v-model="recommendType" placeholder="选择推荐类型" class="custom-select">
                <el-option label="热门商品" value="hot" />
                <el-option label="新品推荐" value="new" />
                <el-option label="基于历史" value="history" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" native-type="submit" class="submit-button" :loading="loading">
                <span class="button-text">获取推荐</span>
              </el-button>
            </el-form-item>
          </el-form>
          <div class="result-container" v-if="recommendationResult">
            <div class="result-header">
              <span class="result-icon">🎁</span>
              <h4>推荐结果</h4>
            </div>
            <div class="result-content">{{ recommendationResult }}</div>
          </div>
        </div>
      </div>
      
      <!-- 智能订单助手 -->
      <div class="ai-feature-card order-card">
        <div class="card-header">
          <div class="icon-wrapper">
            <span class="icon">📦</span>
          </div>
          <h3 class="card-title">智能订单助手</h3>
        </div>
        <p class="card-description">查询订单信息并让AI解释订单状态</p>
        <div class="order-assistant-container">
          <el-input 
            v-model="orderNo" 
            placeholder="请输入订单号，例如：ORD202402220001"
            @keyup.enter="getOrderAssistant"
            class="custom-input"
          />
          <el-button 
            type="primary" 
            @click="getOrderAssistant"
            class="query-button"
            :loading="loading"
          >
            <span class="button-text">查询</span>
          </el-button>
          <div class="result-container" v-if="orderAssistantResult">
            <div class="result-header">
              <span class="result-icon">📋</span>
              <h4>订单信息</h4>
            </div>
            <div class="result-content">{{ orderAssistantResult }}</div>
          </div>
        </div>
      </div>
      
      <!-- 智能商品对比 -->
      <div class="ai-feature-card compare-card">
        <div class="card-header">
          <div class="icon-wrapper">
            <span class="icon">⚖️</span>
          </div>
          <h3 class="card-title">智能商品对比</h3>
        </div>
        <p class="card-description">结合您的购买历史，对比两款商品的优缺点</p>
        <div class="comparison-container">
          <el-form @submit.prevent="compareProducts" class="custom-form">
            <el-form-item label="商品1 ID">
              <el-input v-model.number="product1Id" placeholder="请输入商品1 ID" class="custom-input" />
            </el-form-item>
            <el-form-item label="商品2 ID">
              <el-input v-model.number="product2Id" placeholder="请输入商品2 ID" class="custom-input" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" native-type="submit" class="submit-button" :loading="loading">
                <span class="button-text">对比</span>
              </el-button>
            </el-form-item>
          </el-form>
          <div class="result-container" v-if="comparisonResult">
            <div class="result-header">
              <span class="result-icon">📊</span>
              <h4>对比结果</h4>
            </div>
            <div class="result-content">{{ comparisonResult }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { 
  intelligentCustomerService, 
  personalizedRecommendation, 
  intelligentSearch, 
  intelligentOrderAssistant, 
  intelligentProductComparison 
} from '@/api/ai'
import { useUserStore } from '@/stores/user'

export default {
  name: 'AiAssistant',
  data() {
    return {
      loading: false,
      // 智能客服
      customerServiceQuestion: '',
      customerServiceMessages: [],
      
      // 智能搜索
      searchQuery: '',
      searchResult: '',
      
      // 个性化推荐
      recommendCount: 5,
      recommendType: 'hot',
      recommendCategory: '',
      recommendPreferences: '',
      recommendationResult: '',
      
      // 智能订单助手
      orderNo: '',
      orderAssistantResult: '',
      
      // 智能商品对比
      product1Id: null,
      product2Id: null,
      comparisonResult: ''
    }
  },
  computed: {
    userStore() {
      return useUserStore()
    },
    userId() {
      return this.userStore.userInfo.id
    }
  },
  methods: {
    // 发送智能客服问题
    async sendCustomerServiceQuestion() {
      if (!this.customerServiceQuestion.trim()) return
      
      // 检查是否登录
      const token = localStorage.getItem('token')
      if (!token) {
        this.$message.error('请先登录系统')
        return
      }
      
      // 添加用户问题到聊天记录
      this.customerServiceMessages.push({
        sender: '您',
        content: this.customerServiceQuestion
      })
      
      this.loading = true
      try {
        const answer = await intelligentCustomerService(this.userId, this.customerServiceQuestion)
        // 添加AI回答到聊天记录
        this.customerServiceMessages.push({
          sender: 'AI助手',
          content: answer
        })
        
        // 滚动到底部
        this.$nextTick(() => {
          this.$refs.chatMessages.scrollTop = this.$refs.chatMessages.scrollHeight
        })
      } catch (error) {
        this.$message.error('获取AI回答失败')
      } finally {
        this.customerServiceQuestion = ''
        this.loading = false
      }
    },
    
    // 执行智能搜索
    async performIntelligentSearch() {
      if (!this.searchQuery.trim()) return
      
      this.loading = true
      try {
        console.log('开始搜索，搜索词:', this.searchQuery)
        const result = await intelligentSearch(this.searchQuery)
        console.log('搜索结果:', result)
        this.searchResult = result
        console.log('searchResult已赋值:', this.searchResult)
      } catch (error) {
        console.error('搜索失败，错误信息:', error)
        this.$message.error('搜索失败')
      } finally {
        this.loading = false
      }
    },
    
    // 获取个性化推荐
    async getPersonalizedRecommendation() {
      // 检查是否登录
      const token = localStorage.getItem('token')
      if (!token) {
        this.$message.error('请先登录系统')
        return
      }
      
      // 检查必填字段
      if (!this.recommendCategory.trim()) {
        this.$message.error('请输入商品分类')
        return
      }
      if (!this.recommendPreferences.trim()) {
        this.$message.error('请输入用户偏好')
        return
      }
      
      this.loading = true
      try {
        const requestData = {
          category: this.recommendCategory,
          userPreferences: this.recommendPreferences
        }
        const result = await personalizedRecommendation(this.userId, requestData)
        this.recommendationResult = result
      } catch (error) {
        this.$message.error('获取推荐失败')
      } finally {
        this.loading = false
      }
    },
    
    // 获取订单助手
    async getOrderAssistant() {
      if (!this.orderNo.trim()) return
      
      // 检查是否登录
      const token = localStorage.getItem('token')
      if (!token) {
        this.$message.error('请先登录系统')
        return
      }
      
      this.loading = true
      try {
        const result = await intelligentOrderAssistant(this.userId, this.orderNo)
        this.orderAssistantResult = result
      } catch (error) {
        this.$message.error('获取订单信息失败')
      } finally {
        this.loading = false
      }
    },
    
    // 对比商品
    async compareProducts() {
      if (!this.product1Id || !this.product2Id) {
        this.$message.warning('请输入两个商品ID')
        return
      }
      
      // 检查是否登录
      const token = localStorage.getItem('token')
      if (!token) {
        this.$message.error('请先登录系统')
        return
      }
      
      this.loading = true
      try {
        const result = await intelligentProductComparison(this.userId, this.product1Id, this.product2Id)
        this.comparisonResult = result
      } catch (error) {
        this.$message.error('商品对比失败')
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
/* 引入字体 */
@import url('https://fonts.googleapis.com/css2?family=Orbitron:wght@400;500;600;700&family=Noto+Sans+SC:wght@300;400;500;600&display=swap');

/* CSS变量定义 */
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
  --shadow-color: rgba(0, 255, 136, 0.1);
  --gradient-1: linear-gradient(135deg, #00ff88 0%, #00d4ff 100%);
  --gradient-2: linear-gradient(135deg, #ff6b9d 0%, #00d4ff 100%);
}

.ai-assistant-container {
  padding: 40px 20px;
  max-width: 1400px;
  margin: 0 auto;
  background: linear-gradient(180deg, var(--bg-dark) 0%, #0f142e 100%);
  min-height: 100vh;
  font-family: 'Noto Sans SC', sans-serif;
}

/* 顶部标题区域 */
.header-section {
  text-align: center;
  margin-bottom: 50px;
  animation: fadeInDown 0.8s ease-out;
}

/* 导航栏 */
.navigation-bar {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-bottom: 40px;
  padding: 20px;
  background: var(--bg-card);
  border-radius: 12px;
  border: 1px solid var(--border-color);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
  animation: fadeIn 0.6s ease-out 0.2s both;
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
  box-shadow: 0 4px 12px var(--shadow-color);
}

.nav-link:active {
  transform: translateY(0);
}

.nav-icon {
  font-size: 20px;
  transition: transform 0.3s ease;
}

.nav-link:hover .nav-icon {
  transform: scale(1.2) rotate(10deg);
}

.nav-text {
  font-size: 14px;
  font-weight: 500;
  letter-spacing: 0.5px;
}

.page-title {
  font-family: 'Orbitron', sans-serif;
  font-size: 48px;
  font-weight: 700;
  background: var(--gradient-1);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 12px;
  letter-spacing: 2px;
  text-shadow: 0 0 30px var(--shadow-color);
}

.page-subtitle {
  font-size: 16px;
  color: var(--text-secondary);
  font-weight: 300;
  letter-spacing: 4px;
  text-transform: uppercase;
}

/* 功能卡片网格 */
.ai-features {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 24px;
  animation: fadeInUp 0.8s ease-out 0.2s both;
}

/* 功能卡片 */
.ai-feature-card {
  background: var(--bg-card);
  border-radius: 16px;
  padding: 28px;
  border: 1px solid var(--border-color);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.ai-feature-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: var(--gradient-1);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.ai-feature-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 16px 48px rgba(0, 255, 136, 0.2);
  border-color: var(--primary-color);
}

.ai-feature-card:hover::before {
  opacity: 1;
}

/* 卡片头部 */
.card-header {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.icon-wrapper {
  width: 56px;
  height: 56px;
  background: var(--bg-lighter);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  font-size: 28px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  transition: all 0.3s ease;
}

.ai-feature-card:hover .icon-wrapper {
  transform: scale(1.1) rotate(5deg);
  box-shadow: 0 8px 24px var(--shadow-color);
}

.card-title {
  font-family: 'Orbitron', sans-serif;
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.card-description {
  color: var(--text-secondary);
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 24px;
}

/* 聊天容器 */
.chat-container {
  background: var(--bg-lighter);
  border-radius: 12px;
  padding: 16px;
  border: 1px solid var(--border-color);
}

.chat-messages {
  height: 280px;
  overflow-y: auto;
  margin-bottom: 16px;
  padding: 12px;
  background: var(--bg-dark);
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.chat-messages::-webkit-scrollbar {
  width: 6px;
}

.chat-messages::-webkit-scrollbar-track {
  background: var(--bg-dark);
}

.chat-messages::-webkit-scrollbar-thumb {
  background: var(--border-color);
  border-radius: 3px;
}

.chat-messages::-webkit-scrollbar-thumb:hover {
  background: var(--primary-color);
}

.message {
  padding: 12px 16px;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  animation: messageSlideIn 0.3s ease-out;
  max-width: 85%;
}

.bot-message {
  background: var(--bg-lighter);
  align-self: flex-start;
  border: 1px solid var(--border-color);
}

.user-message {
  background: linear-gradient(135deg, rgba(0, 255, 136, 0.15) 0%, rgba(0, 212, 255, 0.15) 100%);
  align-self: flex-end;
  border: 1px solid var(--primary-color);
}

.sender {
  font-size: 12px;
  font-weight: 600;
  color: var(--primary-color);
  text-transform: uppercase;
  letter-spacing: 1px;
}

.content {
  font-size: 14px;
  color: var(--text-primary);
  line-height: 1.5;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--text-secondary);
  animation: fadeIn 0.5s ease-out;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
  opacity: 0.5;
}

.chat-input {
  display: flex;
  gap: 12px;
  align-items: stretch;
}

.chat-input .custom-input {
  flex: 1;
}

/* 自定义输入框 */
.custom-input {
  background: var(--bg-dark);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  color: var(--text-primary);
  transition: all 0.3s ease;
  height: 48px;
}

.custom-input:focus {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px var(--shadow-color);
}

.custom-input ::v-deep input {
  background: transparent;
  border: none;
  color: var(--text-primary);
  font-size: 14px;
  height: 100%;
}

.custom-input ::v-deep input::placeholder {
  color: var(--text-secondary);
}

/* 按钮 */
.send-button,
.search-button,
.query-button,
.submit-button {
  background: var(--gradient-1);
  border: none;
  border-radius: 8px;
  padding: 14px 32px;
  font-weight: 600;
  font-size: 16px;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px var(--shadow-color);
  min-width: 120px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.send-button:hover,
.search-button:hover,
.query-button:hover,
.submit-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px var(--shadow-color);
  background: linear-gradient(135deg, #00ffaa 0%, #00e4ff 100%);
}

.send-button:active,
.search-button:active,
.query-button:active,
.submit-button:active {
  transform: translateY(0);
}

.button-text {
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

/* 搜索容器 */
.search-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.search-container .custom-input {
  flex: 1;
}

.search-container .search-button {
  width: 100%;
  margin-top: 8px;
}

/* 推荐容器 */
.recommend-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.recommend-container .custom-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.recommend-container .submit-button {
  width: 100%;
  margin-top: 8px;
}

.custom-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.custom-form ::v-deep .el-form-item {
  margin-bottom: 0;
}

.custom-form ::v-deep .el-form-item__label {
  color: var(--text-secondary);
  font-size: 13px;
  font-weight: 500;
  margin-bottom: 8px;
}

.custom-number,
.custom-select {
  background: var(--bg-dark);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  transition: all 0.3s ease;
}

.custom-number:focus,
.custom-select:focus {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px var(--shadow-color);
}

/* 订单助手容器 */
.order-assistant-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.order-assistant-container .custom-input {
  flex: 1;
}

.order-assistant-container .query-button {
  width: 100%;
  margin-top: 8px;
}

/* 商品对比容器 */
.comparison-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.comparison-container .custom-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.comparison-container .submit-button {
  width: 100%;
  margin-top: 8px;
}

/* 结果容器 */
.result-container {
  background: var(--bg-dark);
  border-radius: 12px;
  padding: 20px;
  border: 1px solid var(--border-color);
  margin-top: 16px;
  animation: fadeInUp 0.4s ease-out;
}

.result-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border-color);
}

.result-icon {
  font-size: 24px;
}

.result-header h4 {
  font-family: 'Orbitron', sans-serif;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.result-content {
  color: var(--text-primary);
  font-size: 14px;
  line-height: 1.8;
  white-space: pre-wrap;
  word-wrap: break-word;
  min-height: 20px;
}

.debug-info {
  background: rgba(255, 107, 157, 0.1);
  border: 1px solid rgba(255, 107, 157, 0.3);
  border-radius: 8px;
  padding: 12px;
  margin-top: 16px;
  color: var(--accent-color);
  font-size: 12px;
}

/* 动画 */
@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
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

@keyframes messageSlideIn {
  from {
    opacity: 0;
    transform: translateX(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .ai-features {
    grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  }
}

@media (max-width: 768px) {
  .ai-assistant-container {
    padding: 20px 16px;
  }
  
  .page-title {
    font-size: 36px;
  }
  
  .page-subtitle {
    font-size: 14px;
  }
  
  .navigation-bar {
    flex-wrap: wrap;
    gap: 12px;
    padding: 16px;
  }
  
  .nav-link {
    flex: 1 1 calc(50% - 12px);
    min-width: calc(50% - 12px);
    justify-content: center;
    padding: 10px 12px;
  }
  
  .nav-icon {
    font-size: 18px;
  }
  
  .nav-text {
    font-size: 13px;
  }
  
  .ai-features {
    grid-template-columns: 1fr;
    gap: 20px;
  }
  
  .ai-feature-card {
    padding: 20px;
  }
  
  .chat-messages {
    height: 220px;
  }
  
  .icon-wrapper {
    width: 48px;
    height: 48px;
    font-size: 24px;
  }
}

@media (max-width: 480px) {
  .page-title {
    font-size: 28px;
  }
  
  .card-title {
    font-size: 18px;
  }
  
  .navigation-bar {
    gap: 8px;
    padding: 12px;
  }
  
  .nav-link {
    flex: 1 1 100%;
    min-width: calc(50% - 8px);
    padding: 8px 10px;
  }
  
  .nav-icon {
    font-size: 16px;
  }
  
  .nav-text {
    font-size: 12px;
  }
  
  .chat-input {
    flex-direction: column;
  }
  
  .send-button,
  .search-button,
  .query-button,
  .submit-button {
    width: 100%;
  }
}
</style>
