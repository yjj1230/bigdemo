<template>
  <div class="admin-container">
    <el-container>
      <el-aside width="200px">
        <el-menu
          :default-active="activeMenu"
          @select="handleMenuSelect"
        >
          <el-menu-item index="products">
            <el-icon><Goods /></el-icon>
            <span>商品管理</span>
          </el-menu-item>
          <el-menu-item index="orders">
            <el-icon><Document /></el-icon>
            <span>订单管理</span>
          </el-menu-item>
          <el-menu-item index="users">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="coupons">
            <el-icon><Ticket /></el-icon>
            <span>优惠券管理</span>
          </el-menu-item>
          <el-menu-item index="refunds">
            <el-icon><RefreshRight /></el-icon>
            <span>退款管理</span>
          </el-menu-item>
          <el-menu-item index="logout" divided>
            <el-icon><SwitchButton /></el-icon>
            <span>退出登录</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-main>
        <div v-if="activeMenu === 'products'">
          <h2>商品管理</h2>
          <div class="actions">
            <el-button type="primary" @click="showAddProductDialog">
              添加商品
            </el-button>
            <el-button type="success" @click="loadProducts" :icon="Refresh">
              刷新列表
            </el-button>
          </div>
          <el-table :data="products" style="width: 100%; margin-top: 20px;">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="name" label="商品名称" />
            <el-table-column prop="price" label="价格" width="120" />
            <el-table-column prop="stock" label="库存" width="100" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'info'">
                  {{ row.status === 1 ? '上架' : '下架' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="300">
              <template #default="{ row }">
                <el-button size="small" @click="editProduct(row)">
                  编辑
                </el-button>
                <el-button size="small" type="primary" @click="updateStock(row)">
                  更新库存
                </el-button>
                <el-button
                  size="small"
                  :type="row.status === 1 ? 'warning' : 'success'"
                  @click="toggleProductStatus(row)"
                >
                  {{ row.status === 1 ? '下架' : '上架' }}
                </el-button>
                <el-button size="small" type="danger" @click="deleteProduct(row.id)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div v-if="activeMenu === 'orders'">
          <h2>订单管理</h2>
          <el-table :data="orders" style="width: 100%; margin-top: 20px;">
            <el-table-column prop="id" label="订单ID" width="100" />
            <el-table-column prop="orderNo" label="订单号" />
            <el-table-column prop="totalAmount" label="总金额" width="120" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200">
              <template #default="{ row }">
                <el-button
                  v-if="row.status === 1"
                  size="small"
                  type="primary"
                  @click="shipOrder(row.id)"
                >
                  发货
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div v-if="activeMenu === 'users'">
          <h2>用户管理</h2>
          <el-table :data="users" style="width: 100%; margin-top: 20px;">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="username" label="用户名" />
            <el-table-column prop="email" label="邮箱" />
            <el-table-column prop="phone" label="手机号" />
            <el-table-column prop="nickname" label="昵称" />
            <el-table-column prop="role" label="角色" width="100">
              <template #default="{ row }">
                <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'success'">
                  {{ row.role }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                  {{ row.status === 1 ? '正常' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="300">
              <template #default="{ row }">
                <el-button
                  size="small"
                  @click="editUser(row)"
                  v-if="row.username !== 'admin'"
                >
                  编辑
                </el-button>
                <el-button
                  size="small"
                  @click="changeUserRole(row)"
                  v-if="row.username !== 'admin'"
                >
                  切换角色
                </el-button>
                <el-button
                  size="small"
                  type="danger"
                  @click="deleteUser(row.id)"
                  v-if="row.username !== 'admin'"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div v-if="activeMenu === 'coupons'">
          <h2>优惠券管理</h2>
          <div class="actions">
            <el-button type="primary" @click="showAddCouponDialog">
              添加优惠券
            </el-button>
            <el-button type="success" @click="loadCoupons" :icon="Refresh">
              刷新列表
            </el-button>
          </div>
          <el-table :data="coupons" style="width: 100%; margin-top: 20px;">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="name" label="优惠券名称" />
            <el-table-column prop="type" label="类型" width="100">
              <template #default="{ row }">
                <el-tag :type="getCouponTypeColor(row.type)">
                  {{ getCouponTypeText(row.type) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="discountAmount" label="优惠金额" width="120">
              <template #default="{ row }">
                {{ row.type === 1 ? `¥${row.discountAmount}` : '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="discountRate" label="折扣比例" width="120">
              <template #default="{ row }">
                {{ row.type === 2 ? `${(row.discountRate * 100).toFixed(0)}%` : '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="totalCount" label="总数量" width="100" />
            <el-table-column prop="receivedCount" label="已领取" width="100" />
            <el-table-column prop="usedCount" label="已使用" width="100" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getCouponStatusColor(row.status)">
                  {{ getCouponStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="300">
              <template #default="{ row }">
                <el-button size="small" @click="editCoupon(row)">
                  编辑
                </el-button>
                <el-button size="small" type="primary" @click="showDistributeDialog(row)">
                  发放
                </el-button>
                <el-button size="small" type="danger" @click="deleteCoupon(row.id)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div v-if="activeMenu === 'refunds'">
          <h2>退款管理</h2>
          <div class="actions">
            <el-button type="primary" @click="loadRefunds('pending')">
              待审核
            </el-button>
            <el-button type="success" @click="loadRefunds('approved')">
              待处理
            </el-button>
            <el-button type="warning" @click="loadRefunds('processing')">
              退款中
            </el-button>
            <el-button type="info" @click="loadRefunds('all')">
              全部退款
            </el-button>
          </div>
          <el-table :data="refunds" style="width: 100%; margin-top: 20px;">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="refundNo" label="退款单号" width="180" />
            <el-table-column prop="orderId" label="订单ID" width="100" />
            <el-table-column prop="userId" label="用户ID" width="100" />
            <el-table-column prop="refundAmount" label="退款金额" width="120">
              <template #default="{ row }">
                ¥{{ row.refundAmount }}
              </template>
            </el-table-column>
            <el-table-column prop="reason" label="退款原因" width="200" />
            <el-table-column prop="status" label="状态" width="120">
              <template #default="{ row }">
                <el-tag :type="getRefundStatusType(row.status)">
                  {{ row.status }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="申请时间" width="180" />
            <el-table-column label="操作" width="400">
              <template #default="{ row }">
                <el-button
                  v-if="row.status === '待审核'"
                  size="small"
                  type="success"
                  @click="approveRefund(row.id)"
                >
                  审核通过
                </el-button>
                <el-button
                  v-if="row.status === '待审核'"
                  size="small"
                  type="danger"
                  @click="showRejectDialog(row)"
                >
                  审核拒绝
                </el-button>
                <el-button
                  v-if="row.status === '审核通过'"
                  size="small"
                  type="primary"
                  @click="showProcessDialog(row)"
                >
                  处理退款
                </el-button>
                <el-button
                  v-if="row.status === '退款中'"
                  size="small"
                  type="success"
                  @click="completeRefund(row.id)"
                >
                  完成退款
                </el-button>
                <el-button
                  size="small"
                  @click="viewRefundDetail(row)"
                >
                  查看详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-main>
    </el-container>

    <el-dialog
      v-model="productDialogVisible"
      :title="isEditMode ? '编辑商品' : '添加商品'"
      width="500px"
    >
      <el-form :model="productForm" label-width="100px">
        <el-form-item label="商品名称">
          <el-input v-model="productForm.name" />
        </el-form-item>
        <el-form-item label="价格">
          <el-input-number v-model="productForm.price" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="库存">
          <el-input-number v-model="productForm.stock" :min="0" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="productForm.description" type="textarea" />
        </el-form-item>
        <el-form-item label="图片URL">
          <el-input v-model="productForm.mainImage" />
        </el-form-item>
        <el-form-item label="分类ID">
          <el-input-number v-model="productForm.categoryId" :min="1" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="productDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveProduct">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="stockDialogVisible"
      title="更新库存"
      width="400px"
    >
      <el-form :model="stockForm" label-width="80px">
        <el-form-item label="库存数量">
          <el-input-number v-model="stockForm.stock" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="stockDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveStock">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="userDialogVisible"
      title="编辑用户"
      width="500px"
    >
      <el-form :model="userForm" label-width="100px">
        <el-form-item label="用户名">
          <el-input v-model="userForm.username" disabled />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="userForm.email" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="userForm.phone" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="userForm.nickname" />
        </el-form-item>
        <el-form-item label="头像">
          <el-input v-model="userForm.avatar" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="userForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="userDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveUser">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="couponDialogVisible"
      :title="isCouponEditMode ? '编辑优惠券' : '添加优惠券'"
      width="600px"
    >
      <el-form :model="couponForm" label-width="120px">
        <el-form-item label="优惠券名称">
          <el-input v-model="couponForm.name" />
        </el-form-item>
        <el-form-item label="优惠券类型">
          <el-select v-model="couponForm.type" placeholder="请选择类型">
            <el-option label="满减券" :value="1" />
            <el-option label="折扣券" :value="2" />
            <el-option label="免邮券" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="优惠金额" v-if="couponForm.type === 1">
          <el-input-number v-model="couponForm.discountAmount" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="折扣比例" v-if="couponForm.type === 2">
          <el-input-number v-model="couponForm.discountRate" :min="0" :max="1" :precision="2" :step="0.01" />
        </el-form-item>
        <el-form-item label="最低金额">
          <el-input-number v-model="couponForm.minAmount" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="最大优惠" v-if="couponForm.type === 2">
          <el-input-number v-model="couponForm.maxDiscount" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="总数量">
          <el-input-number v-model="couponForm.totalCount" :min="1" />
        </el-form-item>
        <el-form-item label="每人限领">
          <el-input-number v-model="couponForm.limitPerUser" :min="1" />
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker v-model="couponForm.startTime" type="datetime" placeholder="选择开始时间" />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker v-model="couponForm.endTime" type="datetime" placeholder="选择结束时间" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="couponForm.status" placeholder="请选择状态">
            <el-option label="草稿" :value="1" />
            <el-option label="可领取" :value="2" />
            <el-option label="已结束" :value="3" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="couponDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveCoupon">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="distributeDialogVisible"
      title="发放优惠券"
      width="500px"
    >
      <el-form :model="distributeForm" label-width="100px">
        <el-form-item label="优惠券ID">
          <el-input v-model="distributeForm.couponId" disabled />
        </el-form-item>
        <el-form-item label="优惠券名称">
          <el-input v-model="distributeForm.couponName" disabled />
        </el-form-item>
        <el-form-item label="用户ID">
          <el-input-number v-model="distributeForm.userId" :min="1" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="distributeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="distributeCoupon">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="rejectDialogVisible"
      title="审核拒绝"
      width="500px"
    >
      <el-form :model="rejectForm" label-width="100px">
        <el-form-item label="退款单号">
          <el-input v-model="rejectForm.refundNo" disabled />
        </el-form-item>
        <el-form-item label="拒绝原因">
          <el-input v-model="rejectForm.rejectReason" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmReject">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="processDialogVisible"
      title="处理退款"
      width="500px"
    >
      <el-form :model="processForm" label-width="100px">
        <el-form-item label="退款单号">
          <el-input v-model="processForm.refundNo" disabled />
        </el-form-item>
        <el-form-item label="退款方式">
          <el-select v-model="processForm.refundMethod" placeholder="请选择退款方式">
            <el-option label="原路退回" value="原路退回" />
            <el-option label="银行转账" value="银行转账" />
          </el-select>
        </el-form-item>
        <el-form-item label="退款账户">
          <el-input v-model="processForm.refundAccount" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="processDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmProcess">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Goods, Document, User, SwitchButton, Refresh, Ticket, RefreshRight } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import {
  getAllProductsIncludingOffShelf,
  createProduct,
  updateProduct,
  deleteProduct as deleteProductApi,
  onShelfProduct,
  offShelfProduct,
  updateProductStock,
  getAllOrders,
  shipOrder as shipOrderApi,
  getAllUsers,
  updateUserRole,
  deleteUser as deleteUserApi,
  updateUserById
} from '@/api/admin'
import {
  getAllCoupons,
  createCoupon as createCouponApi,
  updateCoupon as updateCouponApi,
  deleteCoupon as deleteCouponApi,
  distributeCoupon as distributeCouponApi
} from '@/api/coupon'
import {
  getPendingRefunds,
  getApprovedRefunds,
  getProcessingRefunds,
  getAllRefunds,
  approveRefund as approveRefundApi,
  rejectRefund as rejectRefundApi,
  processRefund as processRefundApi,
  completeRefund as completeRefundApi
} from '@/api/refund'

const router = useRouter()
const userStore = useUserStore()

const activeMenu = ref('products')
const products = ref([])
const orders = ref([])
const users = ref([])
const coupons = ref([])
const refunds = ref([])
const productDialogVisible = ref(false)
const stockDialogVisible = ref(false)
const isEditMode = ref(false)
const currentProductId = ref(null)
const couponDialogVisible = ref(false)
const isCouponEditMode = ref(false)
const currentCouponId = ref(null)
const distributeDialogVisible = ref(false)
const rejectDialogVisible = ref(false)
const processDialogVisible = ref(false)

const productForm = ref({
  name: '',
  price: 0,
  stock: 0,
  description: '',
  mainImage: '',
  categoryId: 1
})

const stockForm = ref({
  stock: 0
})

const userDialogVisible = ref(false)
const userForm = ref({
  id: null,
  username: '',
  email: '',
  phone: '',
  nickname: '',
  avatar: '',
  status: 1
})

const couponForm = ref({
  name: '',
  type: 1,
  discountAmount: 0,
  discountRate: 0,
  minAmount: 0,
  maxDiscount: 0,
  totalCount: 100,
  limitPerUser: 1,
  startTime: null,
  endTime: null,
  status: 2
})

const distributeForm = ref({
  couponId: null,
  couponName: '',
  userId: null
})

const rejectForm = ref({
  id: null,
  refundNo: '',
  rejectReason: ''
})

const processForm = ref({
  id: null,
  refundNo: '',
  refundMethod: '',
  refundAccount: ''
})

const loadProducts = async () => {
  try {
    const res = await getAllProductsIncludingOffShelf()
    products.value = res
  } catch (error) {
    ElMessage.error({ message: '获取商品列表失败', duration: 800 })
  }
}

const loadOrders = async () => {
  try {
    const res = await getAllOrders()
    orders.value = res
  } catch (error) {
    ElMessage.error({ message: '获取订单列表失败', duration: 800 })
  }
}

const loadUsers = async () => {
  try {
    const res = await getAllUsers()
    users.value = res
  } catch (error) {
    ElMessage.error({ message: '获取用户列表失败', duration: 800 })
  }
}

const loadCoupons = async () => {
  try {
    const res = await getAllCoupons()
    coupons.value = res
  } catch (error) {
    ElMessage.error({ message: '获取优惠券列表失败', duration: 800 })
  }
}

const handleMenuSelect = async (index) => {
  if (index === 'logout') {
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
    return
  }
  
  activeMenu.value = index
  if (index === 'products') {
    loadProducts()
  } else if (index === 'orders') {
    loadOrders()
  } else if (index === 'users') {
    loadUsers()
  } else if (index === 'coupons') {
    loadCoupons()
  } else if (index === 'refunds') {
    loadRefunds('pending')
  }
}

const showAddCouponDialog = () => {
  isCouponEditMode.value = false
  const now = new Date()
  const endTime = new Date()
  endTime.setMonth(endTime.getMonth() + 1)
  
  // 格式化为ISO-8601格式（LocalDateTime期望的格式）
  const formatDateTime = (date) => {
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')
    const seconds = String(date.getSeconds()).padStart(2, '0')
    return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`
  }
  
  couponForm.value = {
    name: '',
    type: 1,
    discountAmount: 0,
    discountRate: 0,
    minAmount: 0,
    maxDiscount: 0,
    totalCount: 100,
    limitPerUser: 1,
    startTime: formatDateTime(now),
    endTime: formatDateTime(endTime),
    status: 2
  }
  couponDialogVisible.value = true
}

const editCoupon = (coupon) => {
  isCouponEditMode.value = true
  currentCouponId.value = coupon.id
  couponForm.value = {
    id: coupon.id,
    name: coupon.name,
    type: coupon.type,
    discountAmount: coupon.discountAmount,
    discountRate: coupon.discountRate,
    minAmount: coupon.minAmount,
    maxDiscount: coupon.maxDiscount,
    totalCount: coupon.totalCount,
    limitPerUser: coupon.limitPerUser,
    startTime: coupon.startTime,
    endTime: coupon.endTime,
    status: coupon.status
  }
  couponDialogVisible.value = true
}

const saveCoupon = async () => {
  try {
    if (isCouponEditMode.value) {
      await updateCouponApi(couponForm.value.id, couponForm.value)
      ElMessage.success({ message: '更新成功', duration: 800 })
    } else {
      await createCouponApi(couponForm.value)
      ElMessage.success({ message: '添加成功', duration: 800 })
    }
    couponDialogVisible.value = false
    await loadCoupons()
  } catch (error) {
    ElMessage.error({ message: isCouponEditMode.value ? '更新失败' : '添加失败', duration: 800 })
  }
}

const showDistributeDialog = (coupon) => {
  distributeForm.value = {
    couponId: coupon.id,
    couponName: coupon.name,
    userId: null
  }
  distributeDialogVisible.value = true
}

const distributeCoupon = async () => {
  try {
    await distributeCouponApi(distributeForm.value.couponId, distributeForm.value.userId)
    ElMessage.success({ message: '发放成功', duration: 800 })
    distributeDialogVisible.value = false
    await loadCoupons()
  } catch (error) {
    ElMessage.error({ message: '发放失败', duration: 800 })
  }
}

const deleteCoupon = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该优惠券吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteCouponApi(id)
    ElMessage.success({ message: '删除成功', duration: 800 })
    loadCoupons()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error({ message: '删除失败', duration: 800 })
    }
  }
}

const getCouponTypeText = (type) => {
  const typeMap = {
    1: '满减券',
    2: '折扣券',
    3: '免邮券'
  }
  return typeMap[type] || '未知'
}

const getCouponTypeColor = (type) => {
  const colorMap = {
    1: 'success',
    2: 'warning',
    3: 'info'
  }
  return colorMap[type] || 'info'
}

const getCouponStatusText = (status) => {
  const statusMap = {
    1: '草稿',
    2: '可领取',
    3: '已结束'
  }
  return statusMap[status] || '未知'
}

const getCouponStatusColor = (status) => {
  const colorMap = {
    1: 'info',
    2: 'success',
    3: 'danger'
  }
  return colorMap[status] || 'info'
}

const showAddProductDialog = () => {
  isEditMode.value = false
  productForm.value = {
    name: '',
    price: 0,
    stock: 0,
    description: '',
    mainImage: '',
    categoryId: 1,
    status: 1
  }
  productDialogVisible.value = true
}

const editProduct = (product) => {
  isEditMode.value = true
  currentProductId.value = product.id
  productForm.value = {
    id: product.id,
    name: product.name,
    price: product.price,
    stock: product.stock,
    description: product.description || '',
    mainImage: product.mainImage || '',
    categoryId: product.categoryId || 1
  }
  productDialogVisible.value = true
}

const saveProduct = async () => {
  try {
    if (isEditMode.value) {
      await updateProduct(productForm.value)
      ElMessage.success({ message: '更新成功', duration: 800 })
    } else {
      await createProduct(productForm.value)
      ElMessage.success({ message: '添加成功', duration: 800 })
    }
    productDialogVisible.value = false
    await loadProducts()
  } catch (error) {
    ElMessage.error({ message: isEditMode.value ? '更新失败' : '添加失败', duration: 800 })
  }
}

const deleteProduct = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该商品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteProductApi(id)
    ElMessage.success({ message: '删除成功', duration: 800 })
    loadProducts()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error({ message: '删除失败', duration: 800 })
    }
  }
}

const updateStock = (product) => {
  currentProductId.value = product.id
  stockForm.value = {
    stock: product.stock
  }
  stockDialogVisible.value = true
}

const saveStock = async () => {
  try {
    await updateProductStock(currentProductId.value, stockForm.value.stock)
    ElMessage.success({ message: '更新成功', duration: 800 })
    stockDialogVisible.value = false
    loadProducts()
  } catch (error) {
    ElMessage.error({ message: '更新失败', duration: 800 })
  }
}

const toggleProductStatus = async (product) => {
  try {
    if (product.status === 1) {
      await offShelfProduct(product.id)
      ElMessage.success({ message: '下架成功', duration: 800 })
    } else {
      await onShelfProduct(product.id)
      ElMessage.success({ message: '上架成功', duration: 800 })
    }
    loadProducts()
  } catch (error) {
    ElMessage.error({ message: '操作失败', duration: 800 })
  }
}

const shipOrder = async (id) => {
  try {
    await ElMessageBox.confirm('确定要发货吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await shipOrderApi(id)
    ElMessage.success({ message: '发货成功', duration: 800 })
    loadOrders()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error({ message: '发货失败', duration: 800 })
    }
  }
}

const changeUserRole = async (user) => {
  try {
    const newRole = user.role === 'ADMIN' ? 'USER' : 'ADMIN'
    await ElMessageBox.confirm(`确定要将该用户角色切换为${newRole}吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await updateUserRole(user.id, newRole)
    ElMessage.success({ message: '角色切换成功', duration: 800 })
    loadUsers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error({ message: '角色切换失败', duration: 800 })
    }
  }
}

const editUser = (user) => {
  userForm.value = {
    id: user.id,
    username: user.username,
    email: user.email,
    phone: user.phone,
    nickname: user.nickname,
    avatar: user.avatar,
    status: user.status
  }
  userDialogVisible.value = true
}

const saveUser = async () => {
  try {
    await updateUserById(userForm.value.id, userForm.value)
    ElMessage.success({ message: '更新成功', duration: 800 })
    userDialogVisible.value = false
    loadUsers()
  } catch (error) {
    ElMessage.error({ message: '更新失败', duration: 800 })
  }
}

const deleteUser = async (userId) => {
  try {
    await ElMessageBox.confirm('确定要删除该用户吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteUserApi(userId)
    ElMessage.success({ message: '删除成功', duration: 800 })
    loadUsers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error({ message: '删除失败', duration: 800 })
    }
  }
}

const getStatusType = (status) => {
  const statusMap = {
    0: 'info',
    1: 'warning',
    2: 'success',
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

const loadRefunds = async (type) => {
  try {
    let res
    switch (type) {
      case 'pending':
        res = await getPendingRefunds()
        break
      case 'approved':
        res = await getApprovedRefunds()
        break
      case 'processing':
        res = await getProcessingRefunds()
        break
      case 'all':
        res = await getAllRefunds()
        break
      default:
        res = await getPendingRefunds()
    }
    refunds.value = res
  } catch (error) {
    ElMessage.error({ message: '获取退款列表失败', duration: 800 })
  }
}

const approveRefund = async (id) => {
  try {
    await ElMessageBox.confirm('确定要审核通过该退款申请吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await approveRefundApi(id)
    ElMessage.success({ message: '审核通过', duration: 800 })
    loadRefunds('pending')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error({ message: '审核失败', duration: 800 })
    }
  }
}

const showRejectDialog = (refund) => {
  rejectForm.value = {
    id: refund.id,
    refundNo: refund.refundNo,
    rejectReason: ''
  }
  rejectDialogVisible.value = true
}

const confirmReject = async () => {
  try {
    if (!rejectForm.value.rejectReason.trim()) {
      ElMessage.error({ message: '请输入拒绝原因', duration: 800 })
      return
    }
    await rejectRefundApi(rejectForm.value.id, rejectForm.value.rejectReason)
    ElMessage.success({ message: '审核拒绝', duration: 800 })
    rejectDialogVisible.value = false
    loadRefunds('pending')
  } catch (error) {
    ElMessage.error({ message: '审核失败', duration: 800 })
  }
}

const showProcessDialog = (refund) => {
  processForm.value = {
    id: refund.id,
    refundNo: refund.refundNo,
    refundMethod: '',
    refundAccount: ''
  }
  processDialogVisible.value = true
}

const confirmProcess = async () => {
  try {
    if (!processForm.value.refundMethod) {
      ElMessage.error({ message: '请选择退款方式', duration: 800 })
      return
    }
    if (!processForm.value.refundAccount.trim()) {
      ElMessage.error({ message: '请输入退款账户', duration: 800 })
      return
    }
    await processRefundApi(processForm.value.id, processForm.value.refundMethod, processForm.value.refundAccount)
    ElMessage.success({ message: '处理成功', duration: 800 })
    processDialogVisible.value = false
    loadRefunds('approved')
  } catch (error) {
    ElMessage.error({ message: '处理失败', duration: 800 })
  }
}

const completeRefund = async (id) => {
  try {
    await ElMessageBox.confirm('确定要完成该退款吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await completeRefundApi(id)
    ElMessage.success({ message: '退款完成', duration: 800 })
    loadRefunds('processing')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error({ message: '操作失败', duration: 800 })
    }
  }
}

const viewRefundDetail = (refund) => {
  ElMessageBox.alert(`
    <div style="text-align: left;">
      <p><strong>退款单号：</strong>${refund.refundNo}</p>
      <p><strong>订单ID：</strong>${refund.orderId}</p>
      <p><strong>用户ID：</strong>${refund.userId}</p>
      <p><strong>退款金额：</strong>¥${refund.refundAmount}</p>
      <p><strong>退款原因：</strong>${refund.reason}</p>
      <p><strong>退款状态：</strong>${refund.status}</p>
      <p><strong>申请时间：</strong>${refund.createTime}</p>
      ${refund.rejectReason ? `<p><strong>拒绝原因：</strong>${refund.rejectReason}</p>` : ''}
      ${refund.refundMethod ? `<p><strong>退款方式：</strong>${refund.refundMethod}</p>` : ''}
      ${refund.refundAccount ? `<p><strong>退款账户：</strong>${refund.refundAccount}</p>` : ''}
    </div>
  `, '退款详情', {
    dangerouslyUseHTMLString: true,
    confirmButtonText: '关闭'
  })
}

const getRefundStatusType = (status) => {
  const statusMap = {
    '待审核': 'warning',
    '审核通过': 'success',
    '审核拒绝': 'danger',
    '退款中': 'primary',
    '退款完成': 'success'
  }
  return statusMap[status] || 'info'
}

onMounted(() => {
  loadProducts()
})
</script>

<style scoped>
.admin-container {
  height: 100vh;
}

.admin-container :deep(.el-aside) {
  background: #545c64;
}

.admin-container :deep(.el-menu) {
  border-right: none;
}

.admin-container :deep(.el-menu-item) {
  color: #fff;
}

.admin-container :deep(.el-menu-item:hover) {
  background: #434a50;
}

.admin-container :deep(.el-menu-item.is-active) {
  background: #409eff;
}

.admin-container h2 {
  margin-top: 0;
  color: #303133;
}

.actions {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}
</style>
