package com.qiapi.project.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiapi.project.annotation.AuthCheck;
import com.qiapi.project.common.BaseResponse;
import com.qiapi.project.common.DeleteRequest;
import com.qiapi.project.common.ErrorCode;
import com.qiapi.project.common.ResultUtils;
import com.qiapi.project.config.WxOpenConfig;
import com.qiapi.project.constant.UserConstant;
import com.qiapi.project.exception.BusinessException;
import com.qiapi.project.exception.ThrowUtils;
import com.qiapi.project.model.dto.user.*;
import com.qiapi.qiapicommon.model.entity.User;
import com.qiapi.project.model.vo.LoginUserVO;
import com.qiapi.project.model.vo.UserVO;
import com.qiapi.project.model.vo.UserKeyVO;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import me.chanjar.weixin.common.bean.oauth2.WxOAuth2AccessToken;
import me.chanjar.weixin.mp.api.WxMpService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.UUID;
import java.security.SecureRandom;

import static com.qiapi.project.service.impl.UserServiceImpl.SALT;


/**
 * 用户接口
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Resource
    private com.qiapi.service.UserService userService;

    @Resource
    private WxOpenConfig wxOpenConfig;

    // region 登录相关

    /**
     * 用户注册
     *
     * @param userRegisterRequest
     * @return
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            return null;
        }
        
        // 注册用户并自动生成API密钥
        long result = userService.userRegister(userAccount, userPassword, checkPassword);
        
        // 注册成功后，为用户生成API密钥
        if (result > 0) {
            User user = userService.getById(result);
            if (user != null) {
                String accessKey = generateAccessKey();
                String secretKey = generateSecretKey();
                
                User updateUser = new User();
                updateUser.setId(result);
                updateUser.setAccessKey(accessKey);
                updateUser.setSecretKey(secretKey);
                
                userService.updateById(updateUser);
                log.info("新用户 {} 注册成功，已自动生成API密钥", userAccount);
            }
        }
        
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest
     * @param request
     * @return
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 用户登录（微信开放平台）
     */
    @GetMapping("/login/wx_open")
    public BaseResponse<LoginUserVO> userLoginByWxOpen(HttpServletRequest request, HttpServletResponse response,
                                                       @RequestParam("code") String code) {
        WxOAuth2AccessToken accessToken;
        try {
            WxMpService wxService = wxOpenConfig.getWxMpService();
            accessToken = wxService.getOAuth2Service().getAccessToken(code);
            WxOAuth2UserInfo userInfo = wxService.getOAuth2Service().getUserInfo(accessToken, code);
            String unionId = userInfo.getUnionId();
            String mpOpenId = userInfo.getOpenid();
            if (StringUtils.isAnyBlank(unionId, mpOpenId)) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "登录失败，系统错误");
            }
            return ResultUtils.success(userService.userLoginByMpOpen(userInfo, request));
        } catch (Exception e) {
            log.error("userLoginByWxOpen error", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "登录失败，系统错误");
        }
    }

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginUserVO(user));
    }

    // endregion

    // region 增删改查

    /**
     * 创建用户
     *
     * @param userAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest, HttpServletRequest request) {
        if (userAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userAddRequest, user);
        // 默认密码 12345678
        String defaultPassword = "12345678";
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + defaultPassword).getBytes());
        user.setUserPassword(encryptPassword);
        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(user.getId());
    }

    /**
     * 删除用户
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }

    /**
     * 更新用户
     *
     * @param userUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest,
                                            HttpServletRequest request) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取用户（仅管理员）
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }

    /**
     * 根据 id 获取包装类
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(long id, HttpServletRequest request) {
        BaseResponse<User> response = getUserById(id, request);
        User user = response.getData();
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 分页获取用户列表（仅管理员）
     *
     * @param userQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<User>> listUserByPage(@RequestBody UserQueryRequest userQueryRequest,
                                                   HttpServletRequest request) {
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(new Page<>(current, size),
                userService.getQueryWrapper(userQueryRequest));
        return ResultUtils.success(userPage);
    }

    /**
     * 分页获取用户封装列表
     *
     * @param userQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest,
                                                       HttpServletRequest request) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<User> userPage = userService.page(new Page<>(current, size),
                userService.getQueryWrapper(userQueryRequest));
        Page<UserVO> userVOPage = new Page<>(current, size, userPage.getTotal());
        List<UserVO> userVO = userService.getUserVO(userPage.getRecords());
        userVOPage.setRecords(userVO);
        return ResultUtils.success(userVOPage);
    }

    // endregion

    /**
     * 更新个人信息
     *
     * @param userUpdateMyRequest
     * @param request
     * @return
     */
    @PostMapping("/update/my")
    public BaseResponse<Boolean> updateMyUser(@RequestBody UserUpdateMyRequest userUpdateMyRequest,
                                              HttpServletRequest request) {
        if (userUpdateMyRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        User user = new User();
        BeanUtils.copyProperties(userUpdateMyRequest, user);
        user.setId(loginUser.getId());
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }
    /**
     * 更新个人信息
     *
     * @param userUpdateMyRequest
     * @param request
     * @return
     */
    @PostMapping("/update/password")
    public BaseResponse<Boolean> updatePassword(@RequestBody UserPasswordRequest userPasswordRequest,
                                              HttpServletRequest request) {
        if (userPasswordRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Boolean isSuccess = userService.updatePassword(userPasswordRequest.getOldPassword(), userPasswordRequest.getNewPassword(), request);
        return ResultUtils.success(isSuccess);
    }
    
    // region 密钥管理
    
    /**
     * 生成用户API密钥
     * 只有在用户首次生成或重新生成时调用
     *
     * @param request
     * @return
     */
    @PostMapping("/generate/keys")
    public BaseResponse<UserKeyVO> generateApiKeys(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        
        // 生成新的AccessKey和SecretKey
        String accessKey = generateAccessKey();
        String secretKey = generateSecretKey();
        
        // 更新用户信息
        User updateUser = new User();
        updateUser.setId(loginUser.getId());
        updateUser.setAccessKey(accessKey);
        updateUser.setSecretKey(secretKey);
        
        boolean result = userService.updateById(updateUser);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        
        // 返回新生成的密钥
        UserKeyVO userKeyVO = new UserKeyVO();
        userKeyVO.setAccessKey(accessKey);
        userKeyVO.setSecretKey(secretKey);
        
        log.info("用户 {} 生成了新的API密钥", loginUser.getId());
        return ResultUtils.success(userKeyVO);
    }
    
    /**
     * 重新生成用户API密钥
     * 会使旧密钥失效
     *
     * @param request
     * @return
     */
    @PostMapping("/regenerate/keys")
    public BaseResponse<UserKeyVO> regenerateApiKeys(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        
        // 生成新的AccessKey和SecretKey
        String accessKey = generateAccessKey();
        String secretKey = generateSecretKey();
        
        // 更新用户信息
        User updateUser = new User();
        updateUser.setId(loginUser.getId());
        updateUser.setAccessKey(accessKey);
        updateUser.setSecretKey(secretKey);
        
        boolean result = userService.updateById(updateUser);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        
        // 返回新生成的密钥
        UserKeyVO userKeyVO = new UserKeyVO();
        userKeyVO.setAccessKey(accessKey);
        userKeyVO.setSecretKey(secretKey);
        
        log.info("用户 {} 重新生成了API密钥", loginUser.getId());
        return ResultUtils.success(userKeyVO);
    }
    
    /**
     * 获取用户当前的API密钥信息
     * 不返回完整的SecretKey，只返回部分字符用于显示
     *
     * @param request
     * @return
     */
    @GetMapping("/get/keys")
    public BaseResponse<UserKeyVO> getUserKeys(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        
        UserKeyVO userKeyVO = new UserKeyVO();
        userKeyVO.setAccessKey(loginUser.getAccessKey());
        
        // 只返回SecretKey的前4位和后4位，中间用*号替代，用于安全显示
        String secretKey = loginUser.getSecretKey();
        if (secretKey != null && secretKey.length() > 8) {
            String maskedSecretKey = secretKey.substring(0, 4) + 
                    "*".repeat(secretKey.length() - 8) + 
                    secretKey.substring(secretKey.length() - 4);
            userKeyVO.setSecretKey(maskedSecretKey);
        } else {
            userKeyVO.setSecretKey("未生成");
        }
        
        // 设置是否已生成密钥的标志
        userKeyVO.setHasKeys(loginUser.getAccessKey() != null && loginUser.getSecretKey() != null);
        
        return ResultUtils.success(userKeyVO);
    }
    
    /**
     * 生成AccessKey
     * 格式: qiapi_ + 时间戳 + 随机字符串
     */
    private String generateAccessKey() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String randomStr = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return "qiapi_" + timestamp + "_" + randomStr;
    }
    
    /**
     * 生成SecretKey
     * 使用安全随机数生成64位字符串
     */
    private String generateSecretKey() {
        SecureRandom secureRandom = new SecureRandom();
        StringBuilder secretKey = new StringBuilder();
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        
        for (int i = 0; i < 64; i++) {
            secretKey.append(chars.charAt(secureRandom.nextInt(chars.length())));
        }
        
        return secretKey.toString();
    }
    
    // endregion
}
