package org.example.shopdemo.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.example.shopdemo.dto.LoginRequest;
import org.example.shopdemo.dto.RegisterRequest;
import org.example.shopdemo.entity.User;
import org.example.shopdemo.enums.UserRole;
import org.example.shopdemo.enums.UserStatus;
import org.example.shopdemo.mapper.UserMapper;
import org.example.shopdemo.utils.RedisCacheService;
import org.example.shopdemo.utils.jwtutil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户服务类
 * 处理用户相关的业务逻辑
 */
@Slf4j
@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    @Autowired
    private RedisCacheService redisCacheService;

    private static final String USER_CACHE_PREFIX = "user";
    private static final String USER_TOKEN_PREFIX = "user_token";

    /**
     * 初始化默认管理员账号
     */
    @PostConstruct
    public void initAdmin() {
        try {
            User admin = userMapper.findByUsername("admin");
            if (admin == null) {
                log.info("初始化默认管理员账号...");
                User newAdmin = new User();
                newAdmin.setUsername("admin");
                newAdmin.setPassword(passwordEncoder.encode("admin123"));
                newAdmin.setEmail("admin@shopdemo.com");
                newAdmin.setPhone("13800138000");
                newAdmin.setNickname("系统管理员");
                newAdmin.setRole(UserRole.ADMIN.getCode());
                newAdmin.setStatus(UserStatus.NORMAL.getCode());
                userMapper.insert(newAdmin);
                log.info("默认管理员账号创建成功，用户名: admin, 密码: admin123");
            }
        } catch (Exception e) {
            log.error("初始化管理员账号失败", e);
        }
    }

    /**
     * 用户登录
     * @param request 登录请求对象
     * @return JWT token
     */
    public String login(LoginRequest request) {
        log.info("用户登录请求: username={}", request.getUsername());
        User user = userMapper.findByUsername(request.getUsername());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        if (user.getStatus() == UserStatus.DISABLED.getCode()) {
            throw new RuntimeException("账号已被禁用");
        }
        String token = jwtutil.generateToken(user.getId(), user.getUsername(), user.getRole());
        log.info("生成token成功: userId={}, username={}, token={}", user.getId(), user.getUsername(), token);
        String cacheKey = RedisCacheService.generateKey(USER_TOKEN_PREFIX, user.getId().toString());
        redisCacheService.set(cacheKey, token, 24, java.util.concurrent.TimeUnit.HOURS);
        return token;
    }

    /**
     * 用户注册
     * @param request 注册请求对象
     */
    public void register(RegisterRequest request) {
        User existUser = userMapper.findByUsername(request.getUsername());
        if (existUser != null) {
            throw new RuntimeException("用户名已存在");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setNickname(request.getNickname());
        user.setRole(UserRole.USER.getCode());
        user.setStatus(UserStatus.NORMAL.getCode());
        userMapper.insert(user);
    }

    /**
     * 获取用户信息（带缓存）
     * @param username 用户名
     * @return 用户对象
     */
    public User getUserInfo(String username) {
        String cacheKey = RedisCacheService.generateKey(USER_CACHE_PREFIX, username);
        Object cachedUser = redisCacheService.get(cacheKey);
        
        if (cachedUser != null) {
            try {
                return (User) cachedUser;
            } catch (Exception e) {
                // 缓存数据格式不兼容，从数据库重新获取
                redisCacheService.delete(cacheKey);
            }
        }
        
        User user = userMapper.findByUsername(username);
        if (user != null) {
            redisCacheService.set(cacheKey, user, 30, java.util.concurrent.TimeUnit.MINUTES);
        }
        
        return user;
    }

    /**
     * 直接从数据库获取用户信息（不使用缓存）
     * @param username 用户名
     * @return 用户对象
     */
    public User getUserInfoFromDatabase(String username) {
        return userMapper.findByUsername(username);
    }

    /**
     * 根据ID获取用户信息（带缓存）
     * @param userId 用户ID
     * @return 用户对象
     */
    public User getUserInfoById(Long userId) {
        String cacheKey = RedisCacheService.generateKey(USER_CACHE_PREFIX, userId.toString());
        Object cachedUser = redisCacheService.get(cacheKey);
        
        if (cachedUser != null) {
            try {
                return (User) cachedUser;
            } catch (Exception e) {
                // 缓存数据格式不兼容，从数据库重新获取
                redisCacheService.delete(cacheKey);
            }
        }
        
        User user = userMapper.findById(userId);
        if (user != null) {
            redisCacheService.set(cacheKey, user, 30, java.util.concurrent.TimeUnit.MINUTES);
        }
        
        return user;
    }

    /**
     * 更新用户信息
     * @param user 用户对象
     */
    public void updateUserInfo(User user) {
        userMapper.update(user);
        String cacheKey = RedisCacheService.generateKey(USER_CACHE_PREFIX, user.getUsername());
        redisCacheService.delete(cacheKey);
    }

    /**
     * 修改密码
     * @param username 用户名
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    public void changePassword(String username, String oldPassword, String newPassword) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("旧密码错误");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.update(user);
        String cacheKey = RedisCacheService.generateKey(USER_CACHE_PREFIX, username);
        redisCacheService.delete(cacheKey);
    }

    /**
     * 获取所有用户（管理员功能）
     * @return 用户列表
     */
    public java.util.List<User> getAllUsers() {
        return userMapper.findAll();
    }

    /**
     * 更新用户角色（管理员功能）
     * @param userId 用户ID
     * @param role 新角色
     */
    public void updateUserRole(Long userId, UserRole role) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setRole(role.getCode());
        userMapper.update(user);
        String cacheKey = RedisCacheService.generateKey(USER_CACHE_PREFIX, user.getUsername());
        redisCacheService.delete(cacheKey);
    }
}
