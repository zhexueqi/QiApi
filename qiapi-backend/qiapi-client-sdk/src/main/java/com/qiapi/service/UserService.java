package com.qiapi.service;

import com.qiapi.client.QiApiClient;
import com.qiapi.model.ApiResponse;
import com.qiapi.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 用户服务
 * 提供面向用户业务的便捷API调用方法
 * 
 * @author zhexueqi
 */
//@Service
public class UserService {
    
//    @Autowired
    private QiApiClient qiApiClient;
    
    /**
     * 根据ID获取用户信息
     * 
     * @param id 用户ID
     * @return 用户信息
     */
    public User getUserById(Long id) {
        ApiResponse<User> response = qiApiClient.call("user.info", User.class)
            .param("id", id)
            .execute();
        return response.isSuccess() ? response.getData() : null;
    }
    
    /**
     * 获取用户列表
     * 
     * @param keyword 搜索关键词
     * @param page 页码
     * @param size 每页大小
     * @return 用户列表
     */
    public List<User> listUsers(String keyword, int page, int size) {
        ApiResponse<List> response = qiApiClient.call("user.list", List.class)
            .param("keyword", keyword)
            .param("page", page)
            .param("size", size)
            .execute();
        return response.isSuccess() ? response.getData() : Collections.emptyList();
    }
    
    /**
     * 创建用户
     * 
     * @param user 用户信息
     * @return 创建结果
     */
    public boolean createUser(User user) {
        ApiResponse<Boolean> response = qiApiClient.call("user.create", Boolean.class)
            .body(user)
            .execute();
        return response.isSuccess() && response.getData();
    }
    
    /**
     * 异步获取用户信息
     * 
     * @param id 用户ID
     * @return CompletableFuture<User>
     */
    public CompletableFuture<User> getUserByIdAsync(Long id) {
        return qiApiClient.call("user.info", User.class)
            .param("id", id)
            .async()
            .thenApply(response -> response.isSuccess() ? response.getData() : null);
    }
    
    /**
     * 更新用户信息
     * 
     * @param user 用户信息
     * @return 更新结果
     */
    public boolean updateUser(User user) {
        ApiResponse<Boolean> response = qiApiClient.call("user.update", Boolean.class)
            .body(user)
            .execute();
        return response.isSuccess() && response.getData();
    }
    
    /**
     * 删除用户
     * 
     * @param id 用户ID
     * @return 删除结果
     */
    public boolean deleteUser(Long id) {
        ApiResponse<Boolean> response = qiApiClient.call("user.delete", Boolean.class)
            .param("id", id)
            .execute();
        return response.isSuccess() && response.getData();
    }
}