package org.example.shopdemo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.shopdemo.annotation.RateLimiter;
import org.example.shopdemo.annotation.RequireAdmin;
import org.example.shopdemo.common.Result;
import org.example.shopdemo.dto.LoginRequest;
import org.example.shopdemo.dto.RegisterRequest;
import org.example.shopdemo.entity.User;
import org.example.shopdemo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 * 处理用户相关的HTTP请求
 */
@RestController
@RequestMapping("/api/user")
@Tag(name = "用户管理", description = "用户注册、登录、信息管理接口")
public class UserController {
    @Autowired
    private UserService userService;

    /**
     * 用户登录
     * @param request 登录请求对象
     * @return 包含token、用户信息和角色的对象
     */
    @PostMapping("/login")
    @RateLimiter(time = 60, count = 5, limitType = RateLimiter.LimitType.IP, message = "登录请求过于频繁，请稍后再试")
    @Operation(summary = "用户登录", description = "用户使用用户名和密码登录，成功后返回JWT token")
    public Result<java.util.Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        try {
            String token = userService.login(request);
            // 直接从数据库获取用户信息，避免Redis缓存问题
            User user = userService.getUserInfoFromDatabase(request.getUsername());
            user.setPassword(null);
            // 构建返回对象
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("token", token);
            result.put("user", user);
            result.put("role", user.getRole() == 1 ? "ADMIN" : "USER");
            return Result.success("登录成功", result);
        } catch (Exception e) {
            return Result.error(500, e.getMessage());
        }
    }

    /**
     * 用户注册
     * @param request 注册请求对象
     * @return 操作结果
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "新用户注册账号")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        userService.register(request);
        return Result.success("注册成功", null);
    }

    /**
     * 获取用户信息
     * @param token JWT token
     * @return 用户信息
     */
    @GetMapping("/info")
    @Operation(summary = "获取用户信息", description = "获取当前登录用户的详细信息")
    public Result<java.util.Map<String, Object>> getUserInfo(@Parameter(hidden = true) @RequestHeader("Authorization") String token) {
        String actualToken = org.example.shopdemo.utils.jwtutil.extractToken(token);
        String username = org.example.shopdemo.utils.jwtutil.getUsernameFromToken(actualToken);
        // 直接从数据库获取用户信息，避免Redis缓存问题
        User user = userService.getUserInfoFromDatabase(username);
        user.setPassword(null);
        
        // 构建返回对象，包含角色字符串
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("id", user.getId());
        result.put("username", user.getUsername());
        result.put("email", user.getEmail());
        result.put("phone", user.getPhone());
        result.put("nickname", user.getNickname());
        result.put("avatar", user.getAvatar());
        result.put("status", user.getStatus());
        result.put("createTime", user.getCreateTime());
        result.put("updateTime", user.getUpdateTime());
        result.put("role", user.getRole() == 1 ? "ADMIN" : "USER");
        
        return Result.success(result);
    }

    /**
     * 更新用户信息
     * @param token JWT token
     * @param user 用户对象
     * @return 操作结果
     */
    @PutMapping("/update")
    @Operation(summary = "更新用户信息", description = "更新当前登录用户的信息")
    public Result<Void> updateUserInfo(@Parameter(hidden = true) @RequestHeader("Authorization") String token, @RequestBody User user) {
        String actualToken = org.example.shopdemo.utils.jwtutil.extractToken(token);
        String username = org.example.shopdemo.utils.jwtutil.getUsernameFromToken(actualToken);
        User existUser = userService.getUserInfo(username);
        user.setId(existUser.getId());
        userService.updateUserInfo(user);
        return Result.success("更新成功", null);
    }

    /**
     * 修改密码
     * @param token JWT token
     * @param request 密码修改请求
     * @return 操作结果
     */
    @PutMapping("/password")
    @Operation(summary = "修改密码", description = "修改当前登录用户的密码")
    public Result<Void> changePassword(@Parameter(hidden = true) @RequestHeader("Authorization") String token, 
                                    @RequestBody java.util.Map<String, String> request) {
        String actualToken = org.example.shopdemo.utils.jwtutil.extractToken(token);
        String username = org.example.shopdemo.utils.jwtutil.getUsernameFromToken(actualToken);
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");
        userService.changePassword(username, oldPassword, newPassword);
        return Result.success("密码修改成功", null);
    }

    /**
     * 获取所有用户（管理员功能）
     * @return 用户列表
     */
    @GetMapping("/all")
    @RequireAdmin
    @Operation(summary = "获取所有用户", description = "获取系统中所有用户（管理员功能）")
    public Result<java.util.List<java.util.Map<String, Object>>> getAllUsers() {
        java.util.List<User> users = userService.getAllUsers();
        java.util.List<java.util.Map<String, Object>> resultList = new java.util.ArrayList<>();
        
        for (User user : users) {
            java.util.Map<String, Object> userMap = new java.util.HashMap<>();
            userMap.put("id", user.getId());
            userMap.put("username", user.getUsername());
            userMap.put("email", user.getEmail());
            userMap.put("phone", user.getPhone());
            userMap.put("nickname", user.getNickname());
            userMap.put("avatar", user.getAvatar());
            userMap.put("status", user.getStatus());
            userMap.put("createTime", user.getCreateTime());
            userMap.put("updateTime", user.getUpdateTime());
            userMap.put("role", user.getRole() == 1 ? "ADMIN" : "USER");
            resultList.add(userMap);
        }
        
        return Result.success(resultList);
    }

    /**
     * 更新用户角色（管理员功能）
     * @param userId 用户ID
     * @param request 角色更新请求
     * @return 操作结果
     */
    @PutMapping("/{userId}/role")
    @RequireAdmin
    @Operation(summary = "更新用户角色", description = "更新指定用户的角色（管理员功能）")
    public Result<Void> updateUserRole(@PathVariable Long userId, @RequestBody java.util.Map<String, String> request) {
        String roleStr = request.get("role");
        org.example.shopdemo.enums.UserRole role = org.example.shopdemo.enums.UserRole.valueOf(roleStr);
        userService.updateUserRole(userId, role);
        return Result.success("角色更新成功", null);
    }

    /**
     * 删除用户（管理员功能）
     * @param userId 用户ID
     * @return 操作结果
     */
    @DeleteMapping("/{userId}")
    @RequireAdmin
    @Operation(summary = "删除用户", description = "删除指定用户（管理员功能）")
    public Result<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return Result.success("删除成功", null);
    }

    /**
     * 更新用户信息（管理员功能）
     * @param userId 用户ID
     * @param user 用户信息
     * @return 操作结果
     */
    @PutMapping("/{userId}")
    @RequireAdmin
    @Operation(summary = "更新用户信息", description = "更新指定用户的信息（管理员功能）")
    public Result<Void> updateUserById(@PathVariable Long userId, @RequestBody User user) {
        userService.updateUserById(userId, user);
        return Result.success("更新成功", null);
    }
}
