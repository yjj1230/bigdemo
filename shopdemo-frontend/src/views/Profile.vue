<template>
  <div class="profile-container">
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
        <div v-if="userStore.token" class="user-dropdown">
          <el-dropdown @command="handleCommand">
            <span class="el-dropdown-link">
              欢迎，{{ userStore.userInfo.username }}
              <el-icon class="el-icon--right"><arrow-down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="orders">我的订单</el-dropdown-item>
                <el-dropdown-item command="cart">购物车</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
        <router-link v-else to="/login">登录</router-link>
      </div>
    </el-header>

    <el-main class="main-content">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-card class="profile-card">
            <template #header>
              <h3>个人信息</h3>
            </template>
            
            <el-form :model="profileForm" label-width="100px">
              <el-form-item label="用户名">
                <el-input v-model="profileForm.username" disabled />
              </el-form-item>
              
              <el-form-item label="邮箱">
                <el-input v-model="profileForm.email" />
              </el-form-item>
              
              <el-form-item label="手机号">
                <el-input v-model="profileForm.phone" />
              </el-form-item>
              
              <el-form-item>
                <el-button type="primary" @click="updateProfile">
                  保存修改
                </el-button>
              </el-form-item>
            </el-form>
          </el-card>
        </el-col>
        
        <el-col :span="16">
          <el-card class="address-card">
            <template #header>
              <div class="card-header">
                <h3>收货地址</h3>
                <el-button type="primary" size="small" @click="showAddAddressDialog">
                  添加地址
                </el-button>
              </div>
            </template>
            
            <el-empty v-if="addresses.length === 0" description="暂无收货地址，请添加地址" />
            <div v-else class="address-list">
              <div v-for="address in addresses" :key="address.id" class="address-item">
                <div class="address-info">
                  <div class="address-header">
                    <span class="receiver">{{ address.receiverName }}</span>
                    <span class="phone">{{ address.receiverPhone }}</span>
                    <el-tag v-if="address.isDefault === 1" type="success" size="small">默认</el-tag>
                  </div>
                  <div class="address-detail">
                    {{ address.province }} {{ address.city }} {{ address.district }} {{ address.detailAddress }}
                  </div>
                </div>
                <div class="address-actions">
                  <el-button size="small" @click="editAddress(address)">
                    编辑
                  </el-button>
                  <el-button size="small" type="primary" v-if="address.isDefault !== 1" @click="setDefault(address.id)">
                    设为默认
                  </el-button>
                  <el-button size="small" type="danger" @click="deleteAddress(address.id)">
                    删除
                  </el-button>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      
      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="12">
          <el-card class="password-card">
            <template #header>
              <h3>修改密码</h3>
            </template>
            
            <el-form :model="passwordForm" label-width="100px" :rules="passwordRules" ref="passwordFormRef">
              <el-form-item label="原密码" prop="oldPassword">
                <el-input
                  v-model="passwordForm.oldPassword"
                  type="password"
                  show-password
                />
              </el-form-item>
              
              <el-form-item label="新密码" prop="newPassword">
                <el-input
                  v-model="passwordForm.newPassword"
                  type="password"
                  show-password
                />
              </el-form-item>
              
              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input
                  v-model="passwordForm.confirmPassword"
                  type="password"
                  show-password
                />
              </el-form-item>
              
              <el-form-item>
                <el-button type="primary" @click="changePassword">
                  修改密码
                </el-button>
              </el-form-item>
            </el-form>
          </el-card>
        </el-col>
        
        <el-col :span="12">
          <el-card class="favorites-card">
            <template #header>
              <h3>我的收藏</h3>
            </template>
            
            <el-empty v-if="favorites.length === 0" description="暂无收藏" />
            <div v-else class="favorites-list">
              <div v-for="product in favorites" :key="product.id" class="favorite-item">
                <img :src="product.mainImage" class="product-image" />
                <div class="product-info">
                  <h4>{{ product.name }}</h4>
                  <p class="price">¥{{ product.price }}</p>
                  <div class="actions">
                    <el-button size="small" @click="viewProduct(product.id)">
                      查看详情
                    </el-button>
                    <el-button size="small" type="danger" @click="removeFavorite(product.id)">
                      取消收藏
                    </el-button>
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-main>
    
    <el-dialog
      v-model="addressDialogVisible"
      :title="editingAddress ? '编辑地址' : '添加地址'"
      width="500px"
    >
      <el-form :model="addressForm" label-width="80px" :rules="addressRules" ref="addressFormRef">
        <el-form-item label="收货人" prop="receiverName">
          <el-input v-model="addressForm.receiverName" placeholder="请输入收货人姓名" />
        </el-form-item>
        
        <el-form-item label="手机号" prop="receiverPhone">
          <el-input v-model="addressForm.receiverPhone" placeholder="请输入手机号" />
        </el-form-item>
        
        <el-form-item label="省份" prop="province">
          <el-input v-model="addressForm.province" placeholder="请输入省份" />
        </el-form-item>
        
        <el-form-item label="城市" prop="city">
          <el-input v-model="addressForm.city" placeholder="请输入城市" />
        </el-form-item>
        
        <el-form-item label="区县" prop="district">
          <el-input v-model="addressForm.district" placeholder="请输入区县" />
        </el-form-item>
        
        <el-form-item label="详细地址" prop="detailAddress">
          <el-input
            v-model="addressForm.detailAddress"
            type="textarea"
            :rows="3"
            placeholder="请输入详细地址"
          />
        </el-form-item>
        
        <el-form-item label="设为默认">
          <el-switch v-model="addressForm.isDefault" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="addressDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveAddress">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getUserInfo, updateProfile as updateProfileApi, changePassword as changePasswordApi } from '@/api/user'
import { getFavorites, removeFavorite as removeFavoriteApi } from '@/api/product'
import { getAddresses, addAddress, updateAddress as updateAddressApi, deleteAddress as deleteAddressApi, setDefaultAddress as setDefaultAddressApi } from '@/api/address'

const router = useRouter()
const userStore = useUserStore()
const passwordFormRef = ref(null)
const addressFormRef = ref(null)

const profileForm = reactive({
  username: '',
  email: '',
  phone: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const favorites = ref([])
const addresses = ref([])
const addressDialogVisible = ref(false)
const editingAddress = ref(null)
const addressForm = reactive({
  receiverName: '',
  receiverPhone: '',
  province: '',
  city: '',
  district: '',
  detailAddress: '',
  isDefault: false
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const addressRules = {
  receiverName: [
    { required: true, message: '请输入收货人姓名', trigger: 'blur' }
  ],
  receiverPhone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  province: [
    { required: true, message: '请输入省份', trigger: 'blur' }
  ],
  city: [
    { required: true, message: '请输入城市', trigger: 'blur' }
  ],
  district: [
    { required: true, message: '请输入区县', trigger: 'blur' }
  ],
  detailAddress: [
    { required: true, message: '请输入详细地址', trigger: 'blur' }
  ]
}

onMounted(async () => {
  await loadUserInfo()
  await loadFavorites()
  await loadAddresses()
})

const loadUserInfo = async () => {
  try {
    const res = await getUserInfo()
    profileForm.username = res.username
    profileForm.email = res.email
    profileForm.phone = res.phone || ''
  } catch (error) {
    ElMessage.error({ message: '获取用户信息失败', duration: 800 })
  }
}

const loadFavorites = async () => {
  try {
    const res = await getFavorites()
    favorites.value = res.records || []
  } catch (error) {
    ElMessage.error({ message: '获取收藏列表失败', duration: 800 })
  }
}

const loadAddresses = async () => {
  try {
    const res = await getAddresses()
    addresses.value = res || []
  } catch (error) {
    ElMessage.error({ message: '获取地址列表失败', duration: 800 })
  }
}

const updateProfile = async () => {
  try {
    await updateProfileApi(profileForm)
    ElMessage.success({ message: '修改成功', duration: 800 })
  } catch (error) {
    ElMessage.error({ message: '修改失败', duration: 800 })
  }
}

const changePassword = async () => {
  if (!passwordFormRef.value) return
  
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await changePasswordApi({
          oldPassword: passwordForm.oldPassword,
          newPassword: passwordForm.newPassword
        })
        ElMessage.success({ message: '密码修改成功，请重新登录', duration: 800 })
        userStore.logout()
        router.push('/login')
      } catch (error) {
        ElMessage.error({ message: '密码修改失败', duration: 800 })
      }
    }
  })
}

const showAddAddressDialog = () => {
  editingAddress.value = null
  addressForm.receiverName = ''
  addressForm.receiverPhone = ''
  addressForm.province = ''
  addressForm.city = ''
  addressForm.district = ''
  addressForm.detailAddress = ''
  addressForm.isDefault = false
  addressDialogVisible.value = true
}

const editAddress = (address) => {
  editingAddress.value = address
  addressForm.receiverName = address.receiverName
  addressForm.receiverPhone = address.receiverPhone
  addressForm.province = address.province
  addressForm.city = address.city
  addressForm.district = address.district
  addressForm.detailAddress = address.detailAddress
  addressForm.isDefault = address.isDefault === 1
  addressDialogVisible.value = true
}

const saveAddress = async () => {
  if (!addressFormRef.value) return
  
  await addressFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const addressData = {
          ...addressForm,
          isDefault: addressForm.isDefault ? 1 : 0
        }
        if (editingAddress.value) {
          await updateAddressApi({
            id: editingAddress.value.id,
            ...addressData
          })
          ElMessage.success({ message: '更新成功', duration: 800 })
        } else {
          await addAddress(addressData)
          ElMessage.success({ message: '添加成功', duration: 800 })
        }
        addressDialogVisible.value = false
        await loadAddresses()
      } catch (error) {
        ElMessage.error({ message: editingAddress.value ? '更新失败' : '添加失败', duration: 800 })
      }
    }
  })
}

const setDefault = async (addressId) => {
  try {
    await setDefaultAddressApi(addressId)
    ElMessage.success({ message: '设置成功', duration: 800 })
    await loadAddresses()
  } catch (error) {
    ElMessage.error({ message: '设置失败', duration: 800 })
  }
}

const deleteAddress = async (addressId) => {
  try {
    await ElMessageBox.confirm('确定要删除该地址吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteAddressApi(addressId)
    ElMessage.success({ message: '删除成功', duration: 800 })
    await loadAddresses()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error({ message: '删除失败', duration: 800 })
    }
  }
}

const viewProduct = (productId) => {
  router.push(`/product/${productId}`)
}

const removeFavorite = async (productId) => {
  try {
    await removeFavoriteApi(productId)
    ElMessage.success({ message: '取消收藏成功', duration: 800 })
    await loadFavorites()
  } catch (error) {
    ElMessage.error({ message: '取消收藏失败', duration: 800 })
  }
}

const handleCommand = async (command) => {
  switch (command) {
    case 'profile':
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
.profile-container {
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

.user-info {
  display: flex;
  align-items: center;
}

.user-dropdown {
  display: flex;
  align-items: center;
}

.el-dropdown-link {
  cursor: pointer;
  color: #606266;
  display: flex;
  align-items: center;
  font-size: 14px;
}

.el-dropdown-link:hover {
  color: #409eff;
}

.el-icon--right {
  margin-left: 5px;
}

.main-content {
  flex: 1;
  padding: 20px;
}

.profile-card,
.password-card,
.favorites-card,
.address-card {
  margin-bottom: 20px;
}

.profile-card h3,
.password-card h3,
.favorites-card h3,
.address-card h3 {
  margin: 0;
  color: #303133;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  color: #303133;
}

.address-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.address-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 15px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  transition: all 0.3s;
}

.address-item:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.address-info {
  flex: 1;
}

.address-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 10px;
}

.receiver {
  font-weight: bold;
  font-size: 16px;
  color: #303133;
}

.phone {
  color: #606266;
}

.address-detail {
  color: #606266;
  line-height: 1.5;
}

.address-actions {
  display: flex;
  gap: 10px;
  flex-shrink: 0;
}

.favorites-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.favorite-item {
  display: flex;
  gap: 15px;
  padding: 15px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.product-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
}

.product-info {
  flex: 1;
}

.product-info h4 {
  margin: 0 0 10px 0;
  font-size: 16px;
  color: #303133;
}

.price {
  color: #f56c6c;
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 10px;
}

.actions {
  display: flex;
  gap: 10px;
}
</style>
