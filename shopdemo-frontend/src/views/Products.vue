<template>
  <div class="products-container">
    <!-- 导航栏 -->
    <nav class="navbar">
      <div class="navbar-content">
        <div class="logo">
          <span class="logo-icon">🛒️</span>
          <span class="logo-text">智能商城</span>
        </div>
        <div class="nav-links">
          <router-link to="/" class="nav-link">
            <span class="nav-icon">🏠</span>
            <span class="nav-text">首页</span>
          </router-link>
          <router-link to="/products" class="nav-link active">
            <span class="nav-icon">📦</span>
            <span class="nav-text">商品</span>
          </router-link>
          <router-link to="/cart" class="nav-link">
            <span class="nav-icon">🛒</span>
            <span class="nav-text">购物车</span>
          </router-link>
          <router-link to="/orders" class="nav-link">
            <span class="nav-icon">📋</span>
            <span class="nav-text">订单</span>
          </router-link>
          <router-link to="/ai" class="nav-link">
            <span class="nav-icon">🤖</span>
            <span class="nav-text">AI助手</span>
          </router-link>
          <router-link to="/favorites" class="nav-link">
            <span class="nav-icon">❤️</span>
            <span class="nav-text">收藏</span>
          </router-link>
          <router-link to="/coupons" class="nav-link">
            <span class="nav-icon">🎫</span>
            <span class="nav-text">优惠券</span>
          </router-link>
          <router-link to="/profile" class="nav-link">
            <span class="nav-icon">👤</span>
            <span class="nav-text">个人中心</span>
          </router-link>
        </div>
        <div class="user-actions">
          <div v-if="userStore.token" class="user-menu">
            <el-dropdown @command="handleCommand" class="user-dropdown">
              <div class="user-avatar">
                <span class="avatar-icon">👤</span>
                <span class="username">{{ userStore.userInfo.username }}</span>
                <el-icon class="dropdown-icon"><arrow-down /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu class="dropdown-menu">
                  <el-dropdown-item command="profile">
                    <span class="dropdown-icon">👤</span>
                    <span class="dropdown-text">个人中心</span>
                  </el-dropdown-item>
                  <el-dropdown-item command="orders">
                    <span class="dropdown-icon">📋</span>
                    <span class="dropdown-text">我的订单</span>
                  </el-dropdown-item>
                  <el-dropdown-item command="cart">
                    <span class="dropdown-icon">🛒</span>
                    <span class="dropdown-text">购物车</span>
                  </el-dropdown-item>
                  <el-dropdown-item divided command="logout">
                    <span class="dropdown-icon">🚪</span>
                    <span class="dropdown-text">退出登录</span>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
          <router-link v-else to="/login" class="login-btn">
            <span class="login-icon">🔐</span>
            <span class="login-text">登录</span>
          </router-link>
        </div>
      </div>
    </nav>

    <!-- 主要内容 -->
    <main class="main-content">
      <!-- 搜索区域 -->
      <section class="search-section">
        <div class="search-container">
          <div class="search-icon">🔍</div>
          <el-input
            v-model="searchKeyword"
            placeholder="搜索商品名称、品牌、型号..."
            size="large"
            clearable
            @clear="handleSearch"
            @focus="showSearchSuggestions = true"
            @blur="hideSearchSuggestions"
            class="search-input"
          >
            <template #append>
              <el-button :icon="Search" @click="handleSearch" class="search-button">
                搜索
              </el-button>
            </template>
          </el-input>
        </div>

        <!-- 搜索建议 -->
        <div v-if="showSearchSuggestions" class="search-suggestions">
          <div v-if="hotSearchKeywords.length > 0" class="suggestion-section">
            <div class="suggestion-header">
              <span class="suggestion-icon">🔥</span>
              <span class="suggestion-title">热门搜索</span>
            </div>
            <div class="suggestion-tags">
              <span 
                v-for="(item, index) in hotSearchKeywords" 
                :key="item.id" 
                class="suggestion-tag hot-tag"
                @click="selectSearchKeyword(item.keyword)"
              >
                <span class="tag-rank">{{ index + 1 }}</span>
                <span class="tag-text">{{ item.keyword }}</span>
                <span class="tag-count">{{ item.searchCount }}</span>
              </span>
            </div>
          </div>

          <div v-if="searchHistory.length > 0" class="suggestion-section">
            <div class="suggestion-header">
              <span class="suggestion-icon">🕐</span>
              <span class="suggestion-title">搜索历史</span>
              <el-button 
                type="text" 
                size="small" 
                @click="clearSearchHistory"
                class="clear-btn"
              >
                清空
              </el-button>
            </div>
            <div class="suggestion-tags">
              <span 
                v-for="item in searchHistory" 
                :key="item.id" 
                class="suggestion-tag history-tag"
                @click="selectSearchKeyword(item.keyword)"
              >
                <span class="tag-text">{{ item.keyword }}</span>
                <el-icon 
                  class="delete-icon" 
                  @click.stop="deleteSearchHistoryItem(item.id)"
                >
                  <close />
                </el-icon>
              </span>
            </div>
          </div>
        </div>
      </section>

      <!-- 高级筛选面板 -->
      <section class="filter-section">
        <el-collapse v-model="filterPanelVisible" class="filter-collapse">
          <el-collapse-item name="1">
            <template #title>
              <div class="filter-title">
                <span class="filter-icon">🎛️</span>
                <span class="filter-text">高级筛选</span>
                <span v-if="hasActiveFilters" class="filter-badge">{{ activeFilterCount }}</span>
              </div>
            </template>
            <div class="filter-content">
              <div class="filter-row">
                <div class="filter-item">
                  <label class="filter-label">商品分类</label>
                  <el-select v-model="filterForm.categoryId" placeholder="全部分类" clearable class="filter-select">
                    <el-option label="全部分类" :value="null" />
                    <el-option v-for="category in categories" :key="category.id" :label="category.name" :value="category.id" />
                  </el-select>
                </div>
                <div class="filter-item">
                  <label class="filter-label">价格区间</label>
                  <div class="price-range">
                    <el-input-number v-model="filterForm.minPrice" :min="0" :precision="2" placeholder="最低价" class="price-input" />
                    <span class="price-separator">-</span>
                    <el-input-number v-model="filterForm.maxPrice" :min="0" :precision="2" placeholder="最高价" class="price-input" />
                  </div>
                </div>
                <div class="filter-item">
                  <label class="filter-label">排序方式</label>
                  <el-select v-model="filterForm.sortBy" class="filter-select">
                    <el-option label="最新上架" value="time_desc" />
                    <el-option label="价格从低到高" value="price_asc" />
                    <el-option label="价格从高到低" value="price_desc" />
                    <el-option label="销量从高到低" value="sales_desc" />
                  </el-select>
                </div>
              </div>
              <div class="filter-actions">
                <el-button @click="resetFilters" class="reset-btn">
                  <span class="btn-icon">🔄</span>
                  <span class="btn-text">重置</span>
                </el-button>
                <el-button type="primary" @click="applyFilters" class="apply-btn">
                  <span class="btn-icon">✅</span>
                  <span class="btn-text">应用筛选</span>
                </el-button>
              </div>
            </div>
          </el-collapse-item>
        </el-collapse>
      </section>

      <!-- 商品列表 -->
      <section class="products-section">
        <div class="section-header">
          <div class="header-content">
            <span class="section-icon">📦</span>
            <h2 class="section-title">商品列表</h2>
            <div class="result-count">
              共 <span class="count-number">{{ total }}</span> 件商品
            </div>
          </div>
        </div>

        <div class="products-grid">
          <div 
            v-for="product in products" 
            :key="product.id" 
            class="product-card"
            @click="viewDetail(product)"
          >
            <div class="product-image-wrapper">
              <img 
                :src="product.mainImage || product.image" 
                :alt="product.name" 
                class="product-image"
                @error="handleImageError"
              />
              <div class="product-overlay">
                <div class="overlay-actions">
                  <el-button type="primary" size="small" class="quick-view-btn" @click.stop="viewDetail(product)">
                    <span class="btn-icon">👁️</span>
                    <span class="btn-text">查看详情</span>
                  </el-button>
                  <el-button type="success" size="small" class="quick-add-btn" @click.stop="addToCart(product)">
                    <span class="btn-icon">🛒</span>
                    <span class="btn-text">加入购物车</span>
                  </el-button>
                </div>
              </div>
            </div>
            <div class="product-info">
              <h3 class="product-name">{{ product.name }}</h3>
              <div class="product-meta">
                <span class="price">¥{{ product.price }}</span>
                <span class="stock">库存: {{ product.stock || 0 }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 分页 -->
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="total"
            :page-sizes="[8, 16, 24, 32]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            class="pagination"
          />
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, ArrowDown, Close } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getProducts, searchProducts, filterProducts as apiFilterProducts } from '@/api/product'
import { getCategories } from '@/api/category'
import { addToCart as addToCartApi } from '@/api/cart'
import { 
  addSearchHistory as apiAddSearchHistory,
  getSearchHistory as apiGetSearchHistory,
  deleteSearchHistory as apiDeleteSearchHistory,
  clearSearchHistory as apiClearSearchHistory,
  getHotSearch as apiGetHotSearch
} from '@/api/search'

const router = useRouter()
const userStore = useUserStore()

const searchKeyword = ref('')
const products = ref([])
const currentPage = ref(1)
const pageSize = ref(8)
const total = ref(0)
const categories = ref([])
const filterPanelVisible = ref([])
const filterForm = ref({
  keyword: '',
  categoryId: null,
  minPrice: null,
  maxPrice: null,
  sortBy: 'time_desc'
})
const showSearchSuggestions = ref(false)
const hotSearchKeywords = ref([])
const searchHistory = ref([])

const hasActiveFilters = computed(() => {
  return filterForm.value.categoryId !== null || 
         filterForm.value.minPrice !== null || 
         filterForm.value.maxPrice !== null ||
         filterForm.value.sortBy !== 'time_desc'
})

const activeFilterCount = computed(() => {
  let count = 0
  if (filterForm.value.categoryId !== null) count++
  if (filterForm.value.minPrice !== null) count++
  if (filterForm.value.maxPrice !== null) count++
  if (filterForm.value.sortBy !== 'time_desc') count++
  return count
})

onMounted(async () => {
  await loadCategories()
  await loadProducts()
  await loadHotSearch()
  if (userStore.token) {
    await loadSearchHistory()
  }
})

const loadCategories = async () => {
  try {
    const res = await getCategories()
    categories.value = res.data || []
  } catch (error) {
    console.error('获取分类失败:', error)
  }
}

const loadHotSearch = async () => {
  try {
    const res = await apiGetHotSearch(8)
    hotSearchKeywords.value = res.data || []
  } catch (error) {
    console.error('获取热门搜索失败:', error)
  }
}

const loadSearchHistory = async () => {
  try {
    const res = await apiGetSearchHistory(10)
    searchHistory.value = res.data || []
  } catch (error) {
    console.error('获取搜索历史失败:', error)
  }
}

const loadProducts = async () => {
  try {
    const res = await getProducts({
      page: currentPage.value,
      size: pageSize.value
    })
    products.value = res.records
    total.value = res.total
  } catch (error) {
    ElMessage.error('获取商品列表失败')
  }
}

const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    await loadProducts()
    return
  }
  
  if (userStore.token) {
    await apiAddSearchHistory(searchKeyword.value)
    await loadSearchHistory()
  }
  
  try {
    const res = await searchProducts(searchKeyword.value)
    products.value = res
    total.value = res.length
    showSearchSuggestions.value = false
  } catch (error) {
    ElMessage.error('搜索失败')
  }
}

const selectSearchKeyword = async (keyword) => {
  searchKeyword.value = keyword
  await handleSearch()
}

const hideSearchSuggestions = () => {
  setTimeout(() => {
    showSearchSuggestions.value = false
  }, 200)
}

const deleteSearchHistoryItem = async (id) => {
  try {
    await apiDeleteSearchHistory(id)
    searchHistory.value = searchHistory.value.filter(item => item.id !== id)
    ElMessage.success('删除成功')
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const clearSearchHistory = async () => {
  try {
    await ElMessageBox.confirm('确定要清空搜索历史吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await apiClearSearchHistory()
    searchHistory.value = []
    ElMessage.success('清空成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('清空失败')
    }
  }
}

const applyFilters = async () => {
  try {
    const params = {
      keyword: filterForm.value.keyword || searchKeyword.value,
      categoryId: filterForm.value.categoryId,
      minPrice: filterForm.value.minPrice,
      maxPrice: filterForm.value.maxPrice,
      sortBy: filterForm.value.sortBy,
      page: currentPage.value,
      size: pageSize.value
    }
    const res = await apiFilterProducts(params)
    products.value = res.records
    total.value = res.total
    ElMessage.success('筛选成功')
  } catch (error) {
    ElMessage.error('筛选失败')
  }
}

const resetFilters = () => {
  filterForm.value = {
    keyword: '',
    categoryId: null,
    minPrice: null,
    maxPrice: null,
    sortBy: 'time_desc'
  }
  loadProducts()
}

const viewDetail = (product) => {
  router.push(`/product/${product.id}`)
}

const addToCart = async (product) => {
  if (!userStore.token) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  try {
    await addToCartApi({ productId: product.id, quantity: 1 })
    ElMessage.success('已加入购物车')
  } catch (error) {
    ElMessage.error('加入购物车失败')
  }
}

const handleSizeChange = (val) => {
  pageSize.value = val
  if (hasActiveFilters.value) {
    applyFilters()
  } else {
    loadProducts()
  }
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  if (hasActiveFilters.value) {
    applyFilters()
  } else {
    loadProducts()
  }
}

const handleImageError = (event) => {
  event.target.src = 'https://via.placeholder.com/300x300?text=No+Image'
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

.products-container {
  min-height: 100vh;
  background: linear-gradient(180deg, var(--bg-dark) 0%, #0f142e 100%);
  font-family: 'Noto Sans SC', sans-serif;
}

/* 导航栏 */
.navbar {
  background: rgba(26, 31, 58, 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid var(--border-color);
  position: sticky;
  top: 0;
  z-index: 1000;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
}

.navbar-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 16px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  font-size: 28px;
}

.logo-text {
  font-family: 'Orbitron', sans-serif;
  font-size: 20px;
  font-weight: 700;
  background: var(--gradient-1);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.nav-links {
  display: flex;
  gap: 8px;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 16px;
  background: var(--bg-lighter);
  border-radius: 8px;
  text-decoration: none;
  color: var(--text-secondary);
  font-weight: 500;
  transition: all 0.3s ease;
  border: 1px solid transparent;
}

.nav-link:hover,
.nav-link.active {
  background: linear-gradient(135deg, rgba(0, 255, 136, 0.15) 0%, rgba(0, 212, 255, 0.15) 100%);
  border-color: var(--primary-color);
  color: var(--text-primary);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px var(--shadow-color);
}

.nav-link.active {
  background: var(--gradient-1);
  border-color: var(--primary-color);
}

.nav-icon {
  font-size: 18px;
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

.user-actions {
  display: flex;
  align-items: center;
}

.user-menu {
  margin-right: 16px;
}

.user-dropdown {
  cursor: pointer;
}

.user-avatar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: var(--bg-lighter);
  border-radius: 8px;
  border: 1px solid var(--border-color);
  transition: all 0.3s ease;
}

.user-avatar:hover {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px var(--shadow-color);
}

.avatar-icon {
  font-size: 20px;
}

.username {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.dropdown-icon {
  color: var(--text-secondary);
  font-size: 14px;
}

.dropdown-menu {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3);
}

.dropdown-icon {
  margin-right: 8px;
  font-size: 16px;
}

.dropdown-text {
  font-size: 14px;
  color: var(--text-primary);
}

.login-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: var(--gradient-1);
  border-radius: 8px;
  text-decoration: none;
  color: var(--text-primary);
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px var(--shadow-color);
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px var(--shadow-color);
}

.login-icon {
  font-size: 18px;
}

.login-text {
  font-size: 14px;
  font-weight: 600;
}

/* 主要内容 */
.main-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 40px 24px;
}

/* 搜索区域 */
.search-section {
  margin-bottom: 40px;
  position: relative;
}

.search-container {
  display: flex;
  align-items: center;
  gap: 16px;
  background: var(--bg-card);
  border-radius: 12px;
  padding: 20px;
  border: 1px solid var(--border-color);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
  position: relative;
}

.search-icon {
  font-size: 32px;
}

.search-input {
  flex: 1;
}

.search-input :deep(.el-input__wrapper) {
  background: var(--bg-lighter);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  box-shadow: none;
  transition: all 0.3s ease;
}

.search-input :deep(.el-input__wrapper:hover) {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px var(--shadow-color);
}

.search-input :deep(.el-input__wrapper.is-focus) {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px var(--shadow-color);
}

.search-button {
  background: var(--gradient-1);
  border: none;
  border-radius: 0 8px 8px 0;
  color: var(--text-primary);
  font-weight: 600;
}

/* 搜索建议 */
.search-suggestions {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  margin-top: 8px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3);
  z-index: 100;
  max-height: 400px;
  overflow-y: auto;
}

.suggestion-section {
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-color);
}

.suggestion-section:last-child {
  border-bottom: none;
}

.suggestion-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.suggestion-icon {
  font-size: 20px;
  margin-right: 8px;
}

.suggestion-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-secondary);
}

.clear-btn {
  color: var(--text-secondary);
  font-size: 12px;
}

.clear-btn:hover {
  color: var(--primary-color);
}

.suggestion-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.suggestion-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.hot-tag {
  background: linear-gradient(135deg, rgba(255, 107, 157, 0.15), rgba(255, 107, 157, 0.05));
  border: 1px solid rgba(255, 107, 157, 0.3);
  color: #ff6b9d;
}

.hot-tag:hover {
  background: linear-gradient(135deg, rgba(255, 107, 157, 0.25), rgba(255, 107, 157, 0.1));
  border-color: rgba(255, 107, 157, 0.5);
  transform: translateY(-2px);
}

.tag-rank {
  font-size: 12px;
  font-weight: 700;
  background: rgba(255, 107, 157, 0.2);
  padding: 2px 6px;
  border-radius: 8px;
}

.tag-text {
  font-weight: 500;
}

.tag-count {
  font-size: 11px;
  opacity: 0.7;
}

.history-tag {
  background: var(--bg-lighter);
  border: 1px solid var(--border-color);
  color: var(--text-secondary);
}

.history-tag:hover {
  background: var(--gradient-1);
  border-color: var(--primary-color);
  color: var(--text-primary);
  transform: translateY(-2px);
}

.delete-icon {
  font-size: 14px;
  opacity: 0.6;
  transition: all 0.3s ease;
}

.delete-icon:hover {
  opacity: 1;
  color: #ff4444;
}

/* 筛选区域 */
.filter-section {
  margin-bottom: 30px;
}

.filter-collapse {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  overflow: hidden;
}

.filter-collapse :deep(.el-collapse-item__header) {
  background: var(--bg-card);
  border: none;
  padding: 16px 20px;
  color: var(--text-primary);
  font-weight: 600;
}

.filter-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.filter-icon {
  font-size: 24px;
}

.filter-text {
  font-size: 16px;
  font-weight: 600;
}

.filter-badge {
  background: var(--gradient-1);
  color: var(--text-primary);
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 700;
  min-width: 20px;
  text-align: center;
}

.filter-content {
  padding: 24px 20px;
  border-top: 1px solid var(--border-color);
}

.filter-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.filter-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.filter-label {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-secondary);
}

.filter-select {
  width: 100%;
}

.filter-select :deep(.el-input__wrapper) {
  background: var(--bg-lighter);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  box-shadow: none;
  transition: all 0.3s ease;
}

.filter-select :deep(.el-input__wrapper:hover) {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px var(--shadow-color);
}

.filter-select :deep(.el-input__wrapper.is-focus) {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px var(--shadow-color);
}

.price-range {
  display: flex;
  align-items: center;
  gap: 8px;
}

.price-input {
  flex: 1;
}

.price-input :deep(.el-input__wrapper) {
  background: var(--bg-lighter);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  box-shadow: none;
  transition: all 0.3s ease;
}

.price-input :deep(.el-input__wrapper:hover) {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px var(--shadow-color);
}

.price-input :deep(.el-input__wrapper.is-focus) {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px var(--shadow-color);
}

.price-separator {
  color: var(--text-secondary);
  font-size: 16px;
}

.filter-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.reset-btn,
.apply-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  border-radius: 8px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.reset-btn {
  background: var(--bg-lighter);
  border: 1px solid var(--border-color);
  color: var(--text-secondary);
}

.reset-btn:hover {
  border-color: var(--primary-color);
  color: var(--text-primary);
  transform: translateY(-2px);
}

.apply-btn {
  background: var(--gradient-1);
  border: none;
  color: var(--text-primary);
}

.apply-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px var(--shadow-color);
}

/* 商品区域 */
.products-section {
  margin-bottom: 40px;
}

.section-header {
  margin-bottom: 30px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-icon {
  font-size: 32px;
  margin-right: 12px;
}

.section-title {
  font-family: 'Orbitron', sans-serif;
  font-size: 28px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
  display: flex;
  align-items: center;
}

.result-count {
  font-size: 14px;
  color: var(--text-secondary);
}

.count-number {
  font-family: 'Orbitron', sans-serif;
  font-size: 18px;
  font-weight: 700;
  color: var(--primary-color);
  margin: 0 4px;
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
  margin-bottom: 40px;
}

/* 商品卡片 */
.product-card {
  background: var(--bg-card);
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid var(--border-color);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  position: relative;
}

.product-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 16px 48px rgba(0, 255, 136, 0.2);
  border-color: var(--primary-color);
}

.product-image-wrapper {
  position: relative;
  width: 100%;
  padding-top: 75%;
  overflow: hidden;
}

.product-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.product-card:hover .product-image {
  transform: scale(1.05);
}

.product-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.product-card:hover .product-overlay {
  opacity: 1;
}

.overlay-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.quick-view-btn,
.quick-add-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 20px;
  border-radius: 8px;
  font-weight: 600;
  transition: all 0.3s ease;
  min-width: 160px;
}

.quick-view-btn {
  background: var(--gradient-1);
  border: none;
  color: var(--text-primary);
}

.quick-view-btn:hover {
  transform: scale(1.05);
  box-shadow: 0 8px 24px var(--shadow-color);
}

.quick-add-btn {
  background: var(--gradient-2);
  border: none;
  color: var(--text-primary);
}

.quick-add-btn:hover {
  transform: scale(1.05);
  box-shadow: 0 8px 24px rgba(0, 255, 212, 0.2);
}

.btn-icon {
  font-size: 18px;
}

.btn-text {
  font-size: 14px;
}

.product-info {
  padding: 20px;
}

.product-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 12px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.price {
  font-family: 'Orbitron', sans-serif;
  font-size: 20px;
  font-weight: 700;
  color: var(--primary-color);
  text-shadow: 0 0 10px var(--shadow-color);
}

.stock {
  font-size: 13px;
  color: var(--text-secondary);
}

/* 分页 */
.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 40px;
}

.pagination :deep(.el-pagination) {
  background: var(--bg-card);
  border-radius: 12px;
  padding: 16px 24px;
  border: 1px solid var(--border-color);
}

.pagination :deep(.el-pager li) {
  background: var(--bg-lighter);
  border: 1px solid var(--border-color);
  color: var(--text-secondary);
  border-radius: 6px;
  margin: 0 4px;
  transition: all 0.3s ease;
}

.pagination :deep(.el-pager li:hover) {
  background: var(--gradient-1);
  border-color: var(--primary-color);
  color: var(--text-primary);
}

.pagination :deep(.el-pager li.is-active) {
  background: var(--gradient-1);
  border-color: var(--primary-color);
  color: var(--text-primary);
}

.pagination :deep(.btn-prev),
.pagination :deep(.btn-next) {
  background: var(--bg-lighter);
  border: 1px solid var(--border-color);
  color: var(--text-secondary);
  border-radius: 6px;
  transition: all 0.3s ease;
}

.pagination :deep(.btn-prev:hover),
.pagination :deep(.btn-next:hover) {
  background: var(--gradient-1);
  border-color: var(--primary-color);
  color: var(--text-primary);
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .navbar-content,
  .main-content {
    max-width: 100%;
  }
  
  .products-grid {
    grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  }
}

@media (max-width: 768px) {
  .navbar-content {
    padding: 12px 16px;
  }
  
  .nav-links {
    display: none;
  }
  
  .logo-text {
    font-size: 18px;
  }
  
  .main-content {
    padding: 24px 16px;
  }
  
  .search-container {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-icon {
    text-align: center;
  }
  
  .section-title {
    font-size: 22px;
  }
  
  .products-grid {
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 16px;
  }
  
  .overlay-actions {
    flex-direction: row;
  }
  
  .quick-view-btn,
  .quick-add-btn {
    min-width: auto;
    padding: 10px 16px;
  }
}

@media (max-width: 480px) {
  .navbar-content {
    padding: 10px 12px;
  }
  
  .logo-icon {
    font-size: 24px;
  }
  
  .logo-text {
    font-size: 16px;
  }
  
  .section-icon {
    font-size: 24px;
  }
  
  .section-title {
    font-size: 18px;
  }
  
  .products-grid {
    grid-template-columns: 1fr;
  }
  
  .overlay-actions {
    flex-direction: column;
  }
  
  .quick-view-btn,
  .quick-add-btn {
    min-width: 140px;
  }
}
</style>
