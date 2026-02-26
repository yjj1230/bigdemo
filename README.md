# 🤖 智能客服Agent系统

基于DeepSeek大语言模型的智能客服Agent系统，支持17种意图类型和10个核心工具，提供自然语言交互体验。

## ✨ 特性

- 🎯 **混合意图识别**：关键词匹配 + LLM增强，准确率显著提升
- 🔄 **多轮对话**：支持上下文管理，智能槽位填充
- 🛠️ **工具路由**：10个核心工具，自动路由执行
- 🔄 **意图切换**：用户可随时切换话题，无需等待
- 💬 **智能回复**：集成DeepSeek大语言模型，生成个性化回复
- 📊 **性能监控**：集成性能监控，记录处理耗时
- 🎨 **美观前端**：Vue.js + Element UI，响应式设计

## 🛠️ 技术栈

### 后端
- **框架**：Spring Boot 3.2.0
- **LLM**：DeepSeek (Spring AI)
- **数据库**：MySQL 8.0
- **缓存**：Redis
- **ORM**：MyBatis
- **构建工具**：Maven

### 前端
- **框架**：Vue.js 3
- **UI库**：Element Plus
- **HTTP客户端**：Axios
- **构建工具**：Vite

## 🎯 支持的功能

| 功能 | 描述 |
|------|------|
| 订单查询 | 查询订单列表和详情 |
| 物流查询 | 查询订单物流信息 |
| 优惠券管理 | 领取和查看优惠券 |
| 积分查询 | 查询积分余额和记录 |
| 商品搜索 | 搜索商品 |
| 商品推荐 | 智能推荐商品 |
| 商品对比 | 对比两个商品 |
| 购物车查询 | 查询购物车 |
| 收藏查询 | 查询收藏列表 |
| 退款查询 | 查询退款进度 |
| 智能客服 | 回答用户问题 |

## 📦 快速开始

### 环境要求
- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+
- Node.js 16+

### 后端启动
```bash
# 1. 克隆项目
git clone https://github.com/your-username/shopdemo-agent-system.git

# 2. 进入后端目录
cd shopdemo-agent-system

# 3. 配置数据库
# 复制 application-example.yml 为 application.yml 并修改配置

# 4. 启动后端
mvn spring-boot:run
```

### 前端启动
```bash
# 1. 进入前端目录
cd shopdemo-frontend

# 2. 安装依赖
npm install

# 3. 启动前端
npm run dev
```

### 配置说明
1. 复制 `application-example.yml` 为 `application.yml`
2. 修改数据库连接信息
3. 配置DeepSeek API Key
4. 启动项目

## 🏗️ 项目架构

```
┌─────────────┐
│  前端界面  │
│  (Vue.js)  │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  Agent API  │
│  (Spring)   │
└──────┬──────┘
       │
       ├──────────────┬──────────────┐
       │              │              │
       ▼              ▼              ▼
┌──────────┐  ┌──────────┐  ┌──────────┐
│ 意图识别 │  │  工具路由 │  │  LLM回复 │
└──────┬───┘  └──────┬───┘  └──────┬───┘
       │              │              │
       ▼              ▼              ▼
┌──────────────────────────────────────────┐
│         业务服务层                │
│  (订单、物流、商品、用户...)        │
└──────────────────┬───────────────────┘
                 │
                 ▼
┌──────────────────┐
│   数据库层      │
│  (MySQL + Redis) │
└──────────────────┘
```

## 📚 文档

- [智能Agent实现总结](智能Agent实现总结.md)
- [API文档](docs/api.md)
- [数据库设计](docs/database.md)

## 🤝 贡献

欢迎提交Issue和Pull Request！

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启Pull Request

## 📄 开源协议

本项目采用 [MIT License](LICENSE) 开源。

## 👨‍💻 作者

**您的名字** - [GitHub](https://github.com/your-username)

## 🙏 致谢

- [DeepSeek](https://www.deepseek.com/) - 提供大语言模型API
- [Spring AI](https://spring.io/projects/spring-ai) - LLM集成框架
- [Element Plus](https://element-plus.org/) - UI组件库

## 📊 项目亮点

- ⭐ 架构设计优秀，模块化清晰
- ⭐ 混合意图识别，准确率高
- ⭐ 支持多轮对话，用户体验好
- ⭐ 工具自动注册，扩展性强
- ⭐ 代码质量高，注释详细
- ⭐ 技术栈先进，集成LLM

## 📞 联系方式

如有问题，请提交Issue或联系作者。

---

**⭐ 如果这个项目对您有帮助，请给一个Star！⭐**
