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
            <el-table-column prop="role" label="角色" width="100">
              <template #default="{ row }">
                <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'success'">
                  {{ row.role }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200">
              <template #default="{ row }">
                <el-button
                  size="small"
                  @click="changeUserRole(row)"
                  v-if="row.username !== 'admin'"
                >
                  切换角色
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Goods, Document, User, SwitchButton, Refresh } from '@element-plus/icons-vue'
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
  updateUserRole
} from '@/api/admin'

const router = useRouter()
const userStore = useUserStore()

const activeMenu = ref('products')
const products = ref([])
const orders = ref([])
const users = ref([])
const productDialogVisible = ref(false)
const stockDialogVisible = ref(false)
const isEditMode = ref(false)
const currentProductId = ref(null)

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
  }
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
