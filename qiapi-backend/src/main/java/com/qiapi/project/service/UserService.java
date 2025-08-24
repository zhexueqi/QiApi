package com.qiapi.project.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qiapi.project.model.dto.user.EmailLoginRequest;
import com.qiapi.project.model.dto.user.EmailRegisterRequest;
import com.qiapi.project.model.dto.user.UserQueryRequest;
import com.qiapi.qiapicommon.model.entity.User;
import com.qiapi.project.model.vo.LoginUserVO;
import com.qiapi.project.model.vo.UserVO;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 用户服务
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户注册（支持邮箱和用户名）
     *
     * @param userAccount   用户账户
     * @param userEmail     用户邮箱
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegisterWithEmail(String userAccount, String userEmail, String userPassword, String checkPassword);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户登录（支持邮箱和用户名）
     *
     * @param userAccount  用户账户
     * @param userEmail    用户邮箱
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLoginWithEmail(String userAccount, String userEmail, String userPassword,
            HttpServletRequest request);

    /**
     * 用户登录（微信开放平台）
     *
     * @param wxOAuth2UserInfo 从微信获取的用户信息
     * @param request
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLoginByMpOpen(WxOAuth2UserInfo wxOAuth2UserInfo, HttpServletRequest request);

    /**
     * 邮箱验证码注册
     *
     * @param emailRegisterRequest 邮箱注册请求
     * @return 新用户 id
     */
    long userRegisterByEmailCode(EmailRegisterRequest emailRegisterRequest);

    /**
     * 邮箱验证码登录
     *
     * @param emailLoginRequest 邮箱登录请求
     * @param request
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLoginByEmailCode(EmailLoginRequest emailLoginRequest, HttpServletRequest request);

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 获取当前登录用户（允许未登录）
     *
     * @param request
     * @return
     */
    User getLoginUserPermitNull(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param user
     * @return
     */
    boolean isAdmin(User user);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 获取脱敏的已登录用户信息
     *
     * @return
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 获取脱敏的用户信息
     *
     * @param user
     * @return
     */
    UserVO getUserVO(User user);

    /**
     * 获取脱敏的用户信息
     *
     * @param userList
     * @return
     */
    List<UserVO> getUserVO(List<User> userList);

    /**
     * 获取查询条件
     *
     * @param userQueryRequest
     * @return
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    Boolean updatePassword(String oldPassword, String newPassword, HttpServletRequest request);
}
