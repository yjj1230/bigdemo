# 商城系统前端项目

基于 Vue 3 + Vite + Element Plus 的现代化电商前端应用

## 技术栈

- **Vue 3** - 渐进式JavaScript框架
- **Vite** - 下一代前端构建工具
- **Element Plus** - 基于Vue 3的组件库
- **Vue Router** - 官方路由管理器
- **Pinia** - Vue 3状态管理库
- **Axios** - HTTP客户端

## 项目结构

```
shopdemo-frontend/
├── public/                 # 静态资源
├── src/
│   ├── api/               # API接口
│   │   ├── user.js       # 用户相关API
│   │   ├── product.js    # 商品相关API
│   │   ├── cart.js       # 购物车相关API
│   │   └── order.js      # 订单相关API
│   ├── components/         # 公共组件
│   ├── router/            # 路由配置
│   │   └── index.js
│   ├── stores/            # 状态管理
│   │   └── user.js       # 用户状态
│   ├── utils/             # 工具函数
│   │   └── request.js    # Axios封装
│   ├── views/             # 页面组件
│   │   ├── Login.vue      # 登录页
│   │   ├── Register.vue   # 注册页
│   │   ├── Home.vue      # 首页
│   │   ├── Products.vue   # 商品列表
│   │   ├── ProductDetail.vue # 商品详情
│   │   ├── Cart.vue       # 购物车
│   │   ├── Orders.vue     # 订单列表
│   │   ├── OrderDetail.vue # 订单详情
│   │   └── Profile.vue    # 个人中心
│   ├── App.vue            # 根组件
│   └── main.js            # 入口文件
├── index.html             # HTML模板
├── package.json           # 项目配置
└── vite.config.js         # Vite配置
```

## 快速开始

### 安装依赖

```bash
cd shopdemo-frontend
npm install
```

### 开发模式

```bash
npm run dev
```

访问 http://localhost:3000

### 生产构建

```bash
npm run build
```

构建产物在 `dist` 目录

### 预览生产构建

```bash
npm run preview
```

## 功能特性

### 已实现功能

- ✅ 用户登录/注册
- ✅ 商品浏览
- ✅ 商品搜索
- ✅ 购物车管理
- ✅ 订单管理
- ✅ 个人中心
- ✅ 响应式设计
- ✅ 路由守卫
- ✅ 状态管理
- ✅ API请求封装

### 待实现功能

- ⏳ 商品详情页
- ⏳ 订单详情页
- ⏳ 个人中心页
- ⏳ 注册页
- ⏳ 管理后台
- ⏳ WebSocket实时通知
- ⏳ 支付功能

## 配置说明

### API代理配置

在 `vite.config.js` 中配置后端API代理：

```javascript
server: {
  port: 3000,
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true,
      rewrite: (path) => path.replace(/^\/api/, '')
    }
  }
}
```

### 环境变量

创建 `.env.development` 和 `.env.production` 文件：

```env
# .env.development
VITE_API_BASE_URL=http://localhost:8080

# .env.production
VITE_API_BASE_URL=https://your-api-domain.com
```

## API接口说明

### 用户相关

- `POST /api/user/login` - 用户登录
- `POST /api/user/register` - 用户注册
- `GET /api/user/info` - 获取用户信息
- `PUT /api/user/profile` - 更新用户信息
- `PUT /api/user/password` - 修改密码

### 商品相关

- `GET /api/product/list` - 获取商品列表
- `GET /api/product/:id` - 获取商品详情
- `GET /api/product/search` - 搜索商品
- `POST /api/product/favorite` - 添加收藏
- `DELETE /api/product/favorite/:id` - 取消收藏
- `GET /api/product/favorites` - 获取收藏列表
- `POST /api/product/review` - 添加评价
- `GET /api/product/reviews/:id` - 获取商品评价

### 购物车相关

- `GET /api/cart/list` - 获取购物车
- `POST /api/cart/add` - 添加到购物车
- `PUT /api/cart/update` - 更新购物车
- `DELETE /api/cart/remove/:id` - 删除购物车项
- `DELETE /api/cart/clear` - 清空购物车

### 订单相关

- `POST /api/order/create` - 创建订单
- `POST /api/order/createFromCart` - 从购物车创建订单
- `GET /api/order/list` - 获取订单列表
- `GET /api/order/:id` - 获取订单详情
- `POST /api/order/pay/:id` - 支付订单
- `POST /api/order/cancel/:id` - 取消订单
- `POST /api/order/finish/:id` - 完成订单

## 开发指南

### 添加新页面

1. 在 `src/views/` 创建页面组件
2. 在 `src/router/index.js` 添加路由配置
3. 在导航栏添加链接

### 添加新API

1. 在 `src/api/` 创建API文件
2. 导出API函数
3. 在页面中导入使用

### 状态管理

使用Pinia进行状态管理：

```javascript
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
userStore.setToken('token')
```

## 浏览器支持

- Chrome >= 87
- Firefox >= 78
- Safari >= 14
- Edge >= 88

## 许可证

MIT
