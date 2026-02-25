<template>
  <div class="agent-page">
    <!-- 侧边栏 -->
    <div class="sidebar">
      <div class="sidebar-header">
        <div class="logo">
          <span class="logo-icon">🤖</span>
          <span class="logo-text">智能助手</span>
        </div>
      </div>

      <!-- 快捷功能 -->
      <div class="quick-actions">
        <h3>快捷功能</h3>
        <div class="action-grid">
          <div class="action-item" @click="quickAction('order')">
            <div class="action-icon">📦</div>
            <div class="action-label">查订单</div>
          </div>
          <div class="action-item" @click="quickAction('logistics')">
            <div class="action-icon">🚚</div>
            <div class="action-label">查物流</div>
          </div>
          <div class="action-item" @click="quickAction('coupon')">
            <div class="action-icon">🎫</div>
            <div class="action-label">领优惠券</div>
          </div>
          <div class="action-item" @click="quickAction('points')">
            <div class="action-icon">💰</div>
            <div class="action-label">查积分</div>
          </div>
          <div class="action-item" @click="quickAction('recommend')">
            <div class="action-icon">🌟</div>
            <div class="action-label">商品推荐</div>
          </div>
          <div class="action-item" @click="quickAction('compare')">
            <div class="action-icon">📊</div>
            <div class="action-label">商品对比</div>
          </div>
          <div class="action-item" @click="quickAction('cart')">
            <div class="action-icon">🛒</div>
            <div class="action-label">购物车</div>
          </div>
          <div class="action-item" @click="quickAction('help')">
            <div class="action-icon">❓</div>
            <div class="action-label">帮助中心</div>
          </div>
        </div>
      </div>

      <!-- 常用问题 -->
      <div class="common-questions">
        <h3>常用问题</h3>
        <div class="question-list">
          <div
            v-for="(question, index) in commonQuestions"
            :key="index"
            class="question-item"
            @click="sendSuggestion(question)"
          >
            <span class="question-icon">💬</span>
            <span class="question-text">{{ question }}</span>
          </div>
        </div>
      </div>

      <!-- 用户信息 -->
      <div class="user-info" v-if="userInfo">
        <div class="user-avatar">
          {{ userInfo.nickname ? userInfo.nickname.charAt(0) : 'U' }}
        </div>
        <div class="user-details">
          <div class="user-name">{{ userInfo.nickname || '用户' }}</div>
          <div class="user-stats">
            <span>订单: {{ orderCount }}</span>
            <span>积分: {{ points }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 主聊天区域 -->
    <div class="main-chat">
      <!-- 顶部工具栏 -->
      <div class="chat-toolbar">
        <div class="toolbar-left">
          <button class="toolbar-btn" @click="clearChat" title="清空聊天">
            <span>🗑️</span>
            <span>清空</span>
          </button>
          <button class="toolbar-btn" @click="exportChat" title="导出聊天记录">
            <span>📥</span>
            <span>导出</span>
          </button>
        </div>
        <div class="toolbar-right">
          <button class="toolbar-btn" @click="toggleSettings" title="设置">
            <span>⚙️</span>
          </button>
        </div>
      </div>

      <!-- 聊天消息区域 -->
      <div class="chat-messages" ref="messagesContainer">
        <div
          v-for="(message, index) in messages"
          :key="index"
          :class="['message', message.role]"
        >
          <div class="message-content">
            <div class="message-avatar">
              {{ message.role === 'user' ? '👤' : '🤖' }}
            </div>
            <div class="message-body">
              <div class="message-text">
                <pre v-if="message.isJson">{{ message.content }}</pre>
                <span v-else>{{ message.content }}</span>
              </div>
              <div class="navigation-actions" v-if="message.navigationType">
                <el-button
                  type="primary"
                  size="small"
                  @click="navigateTo(message.navigationType, message.navigationParams)"
                  class="nav-button"
                >
                  <span class="nav-icon">🔗</span>
                  <span>查看详情</span>
                </el-button>
              </div>
              <div class="message-actions" v-if="message.role === 'assistant'">
                <button class="action-icon-btn" @click="copyMessage(message.content)" title="复制">
                  📋
                </button>
                <button class="action-icon-btn" @click="likeMessage(index)" title="点赞">
                  {{ message.liked ? '❤️' : '🤍' }}
                </button>
              </div>
            </div>
          </div>
          <div class="message-time">
            {{ message.time }}
          </div>
        </div>

        <div v-if="loading" class="message assistant">
          <div class="message-content">
            <div class="message-avatar">🤖</div>
            <div class="message-body">
              <div class="message-text loading">
                <span class="loading-dots">正在思考</span>
                <span class="dots">...</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 快捷建议 -->
      <div class="chat-suggestions" v-if="messages.length === 0">
        <div class="suggestions-header">
          <span class="suggestions-title">💡 您可以尝试：</span>
        </div>
        <div class="suggestion-buttons">
          <button
            v-for="(suggestion, index) in suggestions"
            :key="index"
            @click="sendSuggestion(suggestion)"
            class="suggestion-btn"
          >
            {{ suggestion }}
          </button>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="chat-input-area">
        <div class="input-toolbar">
          <button class="input-tool-btn" @click="insertEmoji" title="表情">
            😊
          </button>
          <button class="input-tool-btn" @click="clearInput" title="清空输入">
            🗑️
          </button>
        </div>
        <div class="input-container">
          <textarea
            v-model="inputMessage"
            @keydown.enter.exact.prevent="sendMessage"
            @keydown.enter.shift.prevent="inputMessage += '\n'"
            placeholder="输入您的问题，按Enter发送，Shift+Enter换行..."
            rows="1"
            ref="inputTextarea"
            class="message-input"
          ></textarea>
          <button
            @click="sendMessage"
            :disabled="loading || !inputMessage.trim()"
            class="send-button"
          >
            <span v-if="!loading">发送</span>
            <span v-else>发送中...</span>
          </button>
        </div>
      </div>
    </div>

    <!-- 设置面板 -->
    <div class="settings-panel" v-if="showSettings">
      <div class="settings-header">
        <h3>⚙️ 设置</h3>
        <button class="close-btn" @click="toggleSettings">✕</button>
      </div>
      <div class="settings-content">
        <div class="setting-item">
          <label>消息字体大小</label>
          <select v-model="fontSize" @change="changeFontSize">
            <option value="small">小</option>
            <option value="medium">中</option>
            <option value="large">大</option>
          </select>
        </div>
        <div class="setting-item">
          <label>自动滚动</label>
          <input type="checkbox" v-model="autoScroll" />
        </div>
        <div class="setting-item">
          <label>显示时间</label>
          <input type="checkbox" v-model="showTime" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { sendMessage as apiSendMessage } from '@/api/agent'
import { getUserInfo } from '@/api/user'

const router = useRouter()
const messages = ref([])
const inputMessage = ref('')
const loading = ref(false)
const messagesContainer = ref(null)
const inputTextarea = ref(null)
const showSettings = ref(false)
const fontSize = ref('medium')
const autoScroll = ref(true)
const showTime = ref(true)
const userInfo = ref(null)
const orderCount = ref(0)
const points = ref(0)

const suggestions = [
  '帮我查一下订单',
  '查询订单的物流信息',
  '领个优惠券',
  '我的积分有多少',
  '我的优惠券',
  '你好'
]

const commonQuestions = [
  '如何查询订单？',
  '如何使用优惠券？',
  '积分如何使用？',
  '如何申请退款？',
  '配送时间是多久？',
  '如何联系客服？'
]

const quickActions = {
  order: '帮我查一下订单',
  logistics: '查询订单的物流信息',
  coupon: '领个优惠券',
  points: '我的积分有多少',
  recommend: '推荐一些商品',
  compare: '对比商品',
  cart: '我的购物车',
  help: '你能帮我做什么？'
}

const formatTime = () => {
  const now = new Date()
  const hours = String(now.getHours()).padStart(2, '0')
  const minutes = String(now.getMinutes()).padStart(2, '0')
  return `${hours}:${minutes}`
}

const scrollToBottom = async () => {
  if (autoScroll.value) {
    await nextTick()
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  }
}

const parseResponse = (response) => {
  try {
    const parsed = JSON.parse(response)
    return {
      content: parsed.message,
      navigationType: parsed.navigationType,
      navigationParams: parsed.navigationParams,
      isJson: true
    }
  } catch (e) {
    return {
      content: response,
      navigationType: null,
      navigationParams: null,
      isJson: false
    }
  }
}

const navigateTo = (type, params) => {
  switch (type) {
    case 'order':
      if (params && params.orderId) {
        router.push(`/orders/${params.orderId}`)
      }
      break
    case 'orders':
      router.push('/orders')
      break
    case 'logistics':
      if (params && params.orderId) {
        router.push(`/logistics/${params.orderId}`)
      }
      break
    case 'coupons':
      router.push('/coupons')
      break
    case 'points':
      router.push('/points')
      break
    case 'cart':
      router.push('/cart')
      break
    case 'products':
      router.push('/products')
      break
    default:
      ElMessage.warning('暂不支持跳转到该页面')
  }
}

const sendMessage = async () => {
  const message = inputMessage.value.trim()
  if (!message || loading.value) {
    return
  }

  messages.value.push({
    role: 'user',
    content: message,
    time: formatTime()
  })

  inputMessage.value = ''
  loading.value = true

  await scrollToBottom()

  try {
    const response = await apiSendMessage(message)
    const parsedResponse = parseResponse(response)
    
    messages.value.push({
      role: 'assistant',
      content: parsedResponse.content,
      navigationType: parsedResponse.navigationType,
      navigationParams: parsedResponse.navigationParams,
      isJson: parsedResponse.isJson,
      time: formatTime(),
      liked: false
    })

    await scrollToBottom()
  } catch (error) {
    ElMessage.error('发送消息失败：' + (error.message || '未知错误'))
    messages.value.push({
      role: 'assistant',
      content: '抱歉，处理您的请求时出现了错误。请稍后再试。',
      time: formatTime()
    })
    await scrollToBottom()
  } finally {
    loading.value = false
    inputTextarea.value?.focus()
  }
}

const sendSuggestion = (suggestion) => {
  inputMessage.value = suggestion
  sendMessage()
}

const quickAction = (action) => {
  const message = quickActions[action]
  if (message) {
    sendSuggestion(message)
  }
}

const clearChat = () => {
  if (messages.value.length > 0) {
    messages.value = [{
      role: 'assistant',
      content: '聊天记录已清空。有什么可以帮您的吗？',
      time: formatTime()
    }]
    ElMessage.success('聊天记录已清空')
  }
}

const exportChat = () => {
  if (messages.value.length === 0) {
    ElMessage.warning('没有可导出的聊天记录')
    return
  }
  
  const chatText = messages.value.map(msg => {
    const role = msg.role === 'user' ? '用户' : '助手'
    return `[${msg.time}] ${role}: ${msg.content}`
  }).join('\n\n')
  
  const blob = new Blob([chatText], { type: 'text/plain;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `聊天记录_${formatTime()}.txt`
  a.click()
  URL.revokeObjectURL(url)
  
  ElMessage.success('聊天记录已导出')
}

const toggleSettings = () => {
  showSettings.value = !showSettings.value
}

const changeFontSize = () => {
  const sizes = {
    small: '13px',
    medium: '14px',
    large: '16px'
  }
  document.documentElement.style.setProperty('--message-font-size', sizes[fontSize.value])
}

const insertEmoji = () => {
  const emojis = ['😊', '😄', '👍', '❤️', '🎉', '💪', '🙏', '✨']
  const randomEmoji = emojis[Math.floor(Math.random() * emojis.length)]
  inputMessage.value += randomEmoji
  inputTextarea.value?.focus()
}

const clearInput = () => {
  inputMessage.value = ''
  inputTextarea.value?.focus()
}

const copyMessage = (content) => {
  navigator.clipboard.writeText(content).then(() => {
    ElMessage.success('已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败')
  })
}

const likeMessage = (index) => {
  messages.value[index].liked = !messages.value[index].liked
}

const loadUserInfo = async () => {
  try {
    const user = await getUserInfo()
    userInfo.value = user.data || user
    orderCount.value = 0
    points.value = 0
    
    ElMessage.success('用户信息加载成功')
  } catch (error) {
    console.error('获取用户信息失败:', error)
    ElMessage.warning('获取用户信息失败，部分功能可能受限')
  }
}

onMounted(() => {
  messages.value.push({
    role: 'assistant',
    content: '您好！我是您的智能助手。我可以帮您查询订单、物流、优惠券、积分等信息。请问有什么可以帮您的吗？',
    time: formatTime()
  })
  
  inputTextarea.value?.focus()
  loadUserInfo()
})

watch(inputMessage, () => {
  if (inputTextarea.value) {
    inputTextarea.value.style.height = 'auto'
    inputTextarea.value.style.height = inputTextarea.value.scrollHeight + 'px'
  }
})
</script>

<style scoped>
.agent-page {
  display: flex;
  height: calc(100vh - 60px);
  background: #f5f7fa;
  overflow: hidden;
}

/* 侧边栏样式 */
.sidebar {
  width: 280px;
  background: white;
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid #e4e7ed;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo-icon {
  font-size: 28px;
}

.logo-text {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.quick-actions {
  padding: 20px;
  border-bottom: 1px solid #e4e7ed;
}

.quick-actions h3 {
  margin: 0 0 16px 0;
  font-size: 14px;
  font-weight: 600;
  color: #606266;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px 8px;
  background: #f5f7fa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.action-item:hover {
  background: #e6f7ff;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(102, 126, 234, 0.2);
}

.action-icon {
  font-size: 24px;
  margin-bottom: 8px;
}

.action-label {
  font-size: 12px;
  color: #606266;
}

.common-questions {
  padding: 20px;
  border-bottom: 1px solid #e4e7ed;
  flex: 1;
  overflow-y: auto;
}

.common-questions h3 {
  margin: 0 0 16px 0;
  font-size: 14px;
  font-weight: 600;
  color: #606266;
}

.question-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.question-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  background: #f5f7fa;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
}

.question-item:hover {
  background: #e6f7ff;
  transform: translateX(4px);
}

.question-icon {
  font-size: 16px;
}

.question-text {
  font-size: 13px;
  color: #606266;
  flex: 1;
}

.user-info {
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: 600;
}

.user-details {
  flex: 1;
}

.user-name {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 4px;
}

.user-stats {
  font-size: 12px;
  opacity: 0.9;
  display: flex;
  gap: 16px;
}

/* 主聊天区域样式 */
.main-chat {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: white;
}

.chat-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  border-bottom: 1px solid #e4e7ed;
  background: #f5f7fa;
}

.toolbar-left,
.toolbar-right {
  display: flex;
  gap: 8px;
}

.toolbar-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 12px;
  background: white;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  color: #606266;
  transition: all 0.3s;
}

.toolbar-btn:hover {
  background: #e6f7ff;
  border-color: #667eea;
  color: #667eea;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.message.user {
  align-items: flex-end;
}

.message.assistant {
  align-items: flex-start;
}

.message-content {
  display: flex;
  gap: 12px;
  max-width: 80%;
}

.message.user .message-content {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  background: #e0e0e0;
  flex-shrink: 0;
}

.message.user .message-avatar {
  background: #667eea;
  color: white;
}

.message-body {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.message-text {
  padding: 12px 16px;
  border-radius: 12px;
  background: #f5f7fa;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
  font-size: var(--message-font-size, 14px);
}

.message.user .message-text {
  background: #667eea;
  color: white;
  border-radius: 12px 12px 0 12px;
}

.message.assistant .message-text {
  border-radius: 12px 12px 12px 0;
}

.message-text pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
  font-family: inherit;
}

.message-text.loading {
  color: #999;
}

.loading-dots {
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.navigation-actions {
  display: flex;
  gap: 8px;
}

.nav-button {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 13px;
  transition: all 0.3s;
}

.nav-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(102, 126, 234, 0.3);
}

.nav-icon {
  font-size: 14px;
}

.message-actions {
  display: flex;
  gap: 4px;
  margin-top: 4px;
}

.action-icon-btn {
  padding: 4px 8px;
  background: transparent;
  border: none;
  cursor: pointer;
  font-size: 14px;
  opacity: 0.6;
  transition: all 0.3s;
}

.action-icon-btn:hover {
  opacity: 1;
  transform: scale(1.1);
}

.message-time {
  font-size: 12px;
  color: #999;
  padding: 0 52px;
}

.chat-suggestions {
  padding: 20px;
  background: #f5f7fa;
  border-top: 1px solid #e4e7ed;
}

.suggestions-header {
  margin-bottom: 12px;
}

.suggestions-title {
  font-size: 14px;
  color: #606266;
}

.suggestion-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.suggestion-btn {
  padding: 8px 16px;
  border: 1px solid #667eea;
  background: white;
  color: #667eea;
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.suggestion-btn:hover {
  background: #667eea;
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(102, 126, 234, 0.3);
}

.chat-input-area {
  padding: 16px 20px;
  background: white;
  border-top: 1px solid #e4e7ed;
}

.input-toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}

.input-tool-btn {
  padding: 6px 10px;
  background: #f5f7fa;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  cursor: pointer;
  font-size: 16px;
  transition: all 0.3s;
}

.input-tool-btn:hover {
  background: #e6f7ff;
  border-color: #667eea;
}

.input-container {
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

.message-input {
  flex: 1;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  padding: 12px;
  font-size: 14px;
  resize: none;
  outline: none;
  transition: border-color 0.3s;
  min-height: 44px;
  max-height: 120px;
  font-family: inherit;
}

.message-input:focus {
  border-color: #667eea;
}

.send-button {
  padding: 12px 24px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s;
  white-space: nowrap;
}

.send-button:hover:not(:disabled) {
  background: #5568d3;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(102, 126, 234, 0.3);
}

.send-button:disabled {
  background: #ccc;
  cursor: not-allowed;
}

/* 设置面板样式 */
.settings-panel {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
  z-index: 1000;
  min-width: 320px;
}

.settings-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #e4e7ed;
}

.settings-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.close-btn {
  background: transparent;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: #909399;
  transition: all 0.3s;
}

.close-btn:hover {
  color: #606266;
}

.settings-content {
  padding: 20px;
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.setting-item label {
  font-size: 14px;
  color: #606266;
}

.setting-item select {
  padding: 6px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  font-size: 14px;
  outline: none;
}

.setting-item input[type="checkbox"] {
  width: 18px;
  height: 18px;
  cursor: pointer;
}

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 6px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
}

::-webkit-scrollbar-thumb {
  background: #888;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: #555;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .sidebar {
    display: none;
  }
  
  .message-content {
    max-width: 90%;
  }
}
</style>
