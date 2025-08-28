//package com.qiapi.example;
//
//import com.qiapi.client.QiApiClient;
//import com.qiapi.model.ApiConfig;
//import com.qiapi.model.ApiResponse;
//import com.qiapi.model.User;
//import com.qiapi.service.UserService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//import java.util.concurrent.CompletableFuture;
//
///**
// * 优化后的API使用示例
// * 展示如何使用新的QiApiClient调用各种不同的API
// *
// * @author zhexueqi
// */
//@Component
//public class OptimizedApiExample {
//
//    @Autowired
//    private QiApiClient qiApiClient;
//
//    @Autowired
//    private UserService userService;
//
//    /**
//     * 示例1: 使用UserService直接调用API（推荐方式）
//     */
//    public void example1_DirectApiCall() {
//        System.out.println("=== 示例1: 直接API调用 ===");
//
//        // 直接调用，无需构造参数Map
//        User user = userService.getUserById(123L);
//        if (user != null) {
//            System.out.println("用户信息: " + user.getName());
//        } else {
//            System.out.println("获取用户信息失败");
//        }
//    }
//
//    /**
//     * 示例2: 使用构建器模式调用API
//     */
//    public void example2_BuilderPattern() {
//        System.out.println("=== 示例2: 构建器模式调用 ===");
//
//        // 使用构建器模式调用API
//        ApiResponse<User> response = qiApiClient.call("user.info", User.class)
//            .param("id", 123L)
//            .param("name", "zhexueqi")
//            .header("Custom-Header", "CustomValue")
//            .timeout(5000)
//            .execute();
//
//        if (response.isSuccess()) {
//            User user = response.getData();
//            System.out.println("用户信息: " + user.getName());
//        } else {
//            System.out.println("API调用失败: " + response.getMessage());
//        }
//    }
//
//    /**
//     * 示例3: 异步调用API
//     */
//    public void example3_AsyncCall() {
//        System.out.println("=== 示例3: 异步调用 ===");
//
//        // 异步调用API
//        CompletableFuture<User> future = userService.getUserByIdAsync(123L);
//
//        future.thenAccept(user -> {
//            if (user != null) {
//                System.out.println("异步获取用户信息: " + user.getName());
//            } else {
//                System.out.println("异步获取用户信息失败");
//            }
//        }).join(); // 等待异步执行完成，仅用于示例
//    }
//
//    /**
//     * 示例4: 获取可用API列表
//     */
//    public void example4_ListAvailableApis() {
//        System.out.println("=== 示例4: 获取可用API列表 ===");
//
//        List<ApiConfig> apis = qiApiClient.getAvailableApis();
//
//        System.out.println("可用API列表:");
//        for (ApiConfig api : apis) {
//            System.out.printf("- %s: %s (%s %s)%n",
//                api.getApiId(),
//                api.getApiName(),
//                api.getMethod(),
//                api.getPath()
//            );
//        }
//    }
//
//    /**
//     * 示例5: 创建和更新用户
//     */
//    public void example5_CreateAndUpdateUser() {
//        System.out.println("=== 示例5: 创建和更新用户 ===");
//
//        // 创建用户
//        User newUser = new User();
//        newUser.setName("新用户");
//
//        boolean createResult = userService.createUser(newUser);
//        System.out.println("创建用户结果: " + (createResult ? "成功" : "失败"));
//
//        // 更新用户
//        newUser.setName("更新后的用户");
//        boolean updateResult = userService.updateUser(newUser);
//        System.out.println("更新用户结果: " + (updateResult ? "成功" : "失败"));
//    }
//
//    /**
//     * 运行所有示例
//     */
//    public void runAllExamples() {
//        example1_DirectApiCall();
//        System.out.println();
//
//        example2_BuilderPattern();
//        System.out.println();
//
//        example3_AsyncCall();
//        System.out.println();
//
//        example4_ListAvailableApis();
//        System.out.println();
//
//        example5_CreateAndUpdateUser();
//    }
//}