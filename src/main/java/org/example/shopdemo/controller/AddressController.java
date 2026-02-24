package org.example.shopdemo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.shopdemo.common.Result;
import org.example.shopdemo.entity.Address;
import org.example.shopdemo.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户地址控制器
 * 处理用户地址相关的HTTP请求
 */
@RestController
@RequestMapping("/api/address")
@Tag(name = "地址管理", description = "用户地址的添加、查询、更新、删除接口")
public class AddressController {
    @Autowired
    private AddressService addressService;

    /**
     * 获取用户的所有地址
     * @param token JWT token
     * @return 地址列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取用户地址列表", description = "获取当前登录用户的所有地址")
    public Result<List<Address>> getUserAddresses(@Parameter(hidden = true) @RequestHeader("Authorization") String token) {
        String actualToken = org.example.shopdemo.utils.jwtutil.extractToken(token);
        String username = org.example.shopdemo.utils.jwtutil.getUsernameFromToken(actualToken);
        Long userId = org.example.shopdemo.utils.jwtutil.getUserIdFromToken(actualToken);
        return Result.success(addressService.getUserAddresses(userId));
    }

    /**
     * 根据ID获取地址
     * @param id 地址ID
     * @return 地址对象
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取地址", description = "根据地址ID获取地址详细信息")
    public Result<Address> getAddressById(@PathVariable Long id) {
        return Result.success(addressService.getAddressById(id));
    }

    /**
     * 添加地址
     * @param token JWT token
     * @param address 地址对象
     * @return 操作结果
     */
    @PostMapping("/add")
    @Operation(summary = "添加地址", description = "为当前用户添加新地址")
    public Result<Void> addAddress(@Parameter(hidden = true) @RequestHeader("Authorization") String token, @RequestBody Address address) {
        String actualToken = org.example.shopdemo.utils.jwtutil.extractToken(token);
        Long userId = org.example.shopdemo.utils.jwtutil.getUserIdFromToken(actualToken);
        address.setUserId(userId);
        addressService.addAddress(address);
        return Result.success("添加成功", null);
    }

    /**
     * 更新地址
     * @param address 地址对象
     * @return 操作结果
     */
    @PutMapping("/update")
    @Operation(summary = "更新地址", description = "更新地址信息")
    public Result<Void> updateAddress(@RequestBody Address address) {
        addressService.updateAddress(address);
        return Result.success("更新成功", null);
    }

    /**
     * 删除地址
     * @param id 地址ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除地址", description = "根据ID删除地址")
    public Result<Void> deleteAddress(@PathVariable Long id) {
        addressService.deleteAddress(id);
        return Result.success("删除成功", null);
    }

    /**
     * 设置默认地址
     * @param token JWT token
     * @param addressId 地址ID
     * @return 操作结果
     */
    @PutMapping("/default/{addressId}")
    @Operation(summary = "设置默认地址", description = "将指定地址设置为默认地址")
    public Result<Void> setDefaultAddress(@Parameter(hidden = true) @RequestHeader("Authorization") String token, @PathVariable Long addressId) {
        String actualToken = org.example.shopdemo.utils.jwtutil.extractToken(token);
        String username = org.example.shopdemo.utils.jwtutil.getUsernameFromToken(actualToken);
        Long userId = org.example.shopdemo.utils.jwtutil.getUserIdFromToken(actualToken);
        addressService.setDefaultAddress(userId, addressId);
        return Result.success("设置成功", null);
    }
}
