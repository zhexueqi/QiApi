package com.qiapi.project.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qiapi.project.common.ErrorCode;
import com.qiapi.project.constant.CommonConstant;
import com.qiapi.project.exception.BusinessException;
import com.qiapi.project.mapper.UserMapper;
import com.qiapi.project.model.dto.user.EmailLoginRequest;
import com.qiapi.project.model.dto.user.EmailRegisterRequest;
import com.qiapi.project.model.dto.user.UserQueryRequest;
import com.qiapi.project.service.EmailService;
import com.qiapi.project.service.PointService;
import com.qiapi.project.service.UserService;
import com.qiapi.qiapicommon.model.entity.User;
import com.qiapi.project.model.enums.UserRoleEnum;
import com.qiapi.project.model.vo.LoginUserVO;
import com.qiapi.project.model.vo.UserVO;
import com.qiapi.project.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.qiapi.project.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户服务实现
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /**
     * 盐值，混淆密码
     */
    public static final String SALT = "yupi";

    @Resource
    private PointService pointService;

    @Resource
    private EmailService emailService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        synchronized (userAccount.intern()) {
            // 账户不能重复
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount", userAccount);
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
            }
            // 2. 加密
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
            // 3. 生成accessKey 和 secureKey
            String accessKey = DigestUtil.sha256Hex(SALT + userAccount + RandomUtil.randomNumbers(5));
            String secureKey = DigestUtil.sha256Hex(SALT + userAccount + RandomUtil.randomNumbers(8));
            // 4. 插入数据
            User user = new User();
            user.setUserAccount(userAccount);
            user.setUserPassword(encryptPassword);
            user.setAccessKey(accessKey);
            user.setSecretKey(secureKey);
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }

            // 初始化用户积分账户
            try {
                pointService.initUserPoints(user.getId());
                log.info("用户{}积分账户初始化成功", user.getId());
            } catch (Exception e) {
                log.error("用户{}积分账户初始化失败", user.getId(), e);
                // 积分初始化失败不影响注册流程
            }

            return user.getId();
        }
    }

    @Override
    public long userRegisterWithEmail(String userAccount, String userEmail, String userPassword, String checkPassword) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码参数为空");
        }

        // 用户名和邮箱至少提供一个
        if (StringUtils.isAllBlank(userAccount, userEmail)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名和邮箱至少提供一个");
        }

        // 校验用户名格式
        if (StringUtils.isNotBlank(userAccount) && userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }

        // 校验邮箱格式
        if (StringUtils.isNotBlank(userEmail)
                && !userEmail.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式不正确");
        }

        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }

        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }

        // 使用邮箱或用户名作为同步锁的key
        String lockKey = StringUtils.isNotBlank(userEmail) ? userEmail : userAccount;
        synchronized (lockKey.intern()) {
            // 检查用户名和邮箱是否已存在
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            if (StringUtils.isNotBlank(userAccount)) {
                queryWrapper.eq("userAccount", userAccount);
            }
            if (StringUtils.isNotBlank(userEmail)) {
                if (StringUtils.isNotBlank(userAccount)) {
                    queryWrapper.or();
                }
                queryWrapper.eq("userEmail", userEmail);
            }

            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名或邮箱已存在");
            }

            // 2. 加密
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

            // 3. 生成accessKey 和 secretKey
            String keyBase = StringUtils.isNotBlank(userAccount) ? userAccount : userEmail;
            String accessKey = DigestUtil.sha256Hex(SALT + keyBase + RandomUtil.randomNumbers(5));
            String secretKey = DigestUtil.sha256Hex(SALT + keyBase + RandomUtil.randomNumbers(8));

            // 4. 插入数据
            User user = new User();
            if (StringUtils.isNotBlank(userAccount)) {
                user.setUserAccount(userAccount);
            }
            if (StringUtils.isNotBlank(userEmail)) {
                user.setUserEmail(userEmail);
            }
            user.setUserPassword(encryptPassword);
            user.setAccessKey(accessKey);
            user.setSecretKey(secretKey);

            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }

            // 初始化用户积分账户
            try {
                pointService.initUserPoints(user.getId());
                log.info("用户{}积分账户初始化成功", user.getId());
            } catch (Exception e) {
                log.error("用户{}积分账户初始化失败", user.getId(), e);
                // 积分初始化失败不影响注册流程
            }

            return user.getId();
        }
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        // 3. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        return this.getLoginUserVO(user);
    }

    @Override
    public LoginUserVO userLoginWithEmail(String userAccount, String userEmail, String userPassword,
            HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isBlank(userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码为空");
        }

        // 用户名和邮箱至少提供一个
        if (StringUtils.isAllBlank(userAccount, userEmail)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名和邮箱至少提供一个");
        }

        // 校验用户名格式
        if (StringUtils.isNotBlank(userAccount) && userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }

        // 校验邮箱格式
        if (StringUtils.isNotBlank(userEmail)
                && !userEmail.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式不正确");
        }

        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }

        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        // 构造查询条件：用户名或邮箱匹配，且密码匹配
        queryWrapper.and(wrapper -> {
            if (StringUtils.isNotBlank(userAccount)) {
                wrapper.eq("userAccount", userAccount);
            }
            if (StringUtils.isNotBlank(userEmail)) {
                if (StringUtils.isNotBlank(userAccount)) {
                    wrapper.or();
                }
                wrapper.eq("userEmail", userEmail);
            }
        });
        queryWrapper.eq("userPassword", encryptPassword);

        User user = this.baseMapper.selectOne(queryWrapper);

        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount/userEmail cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }

        // 3. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        return this.getLoginUserVO(user);
    }

    @Override
    public LoginUserVO userLoginByMpOpen(WxOAuth2UserInfo wxOAuth2UserInfo, HttpServletRequest request) {
        String unionId = wxOAuth2UserInfo.getUnionId();
        String mpOpenId = wxOAuth2UserInfo.getOpenid();
        // 单机锁
        synchronized (unionId.intern()) {
            // 查询用户是否已存在
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("unionId", unionId);
            User user = this.getOne(queryWrapper);
            // 被封号，禁止登录
            if (user != null && UserRoleEnum.BAN.getValue().equals(user.getUserRole())) {
                throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "该用户已被封，禁止登录");
            }
            // 用户不存在则创建
            if (user == null) {
                user = new User();
                user.setUserAvatar(wxOAuth2UserInfo.getHeadImgUrl());
                user.setUserName(wxOAuth2UserInfo.getNickname());
                boolean result = this.save(user);
                if (!result) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "登录失败");
                }
            }
            // 记录用户的登录态
            request.getSession().setAttribute(USER_LOGIN_STATE, user);
            return getLoginUserVO(user);
        }
    }

    @Override
    public long userRegisterByEmailCode(EmailRegisterRequest emailRegisterRequest) {
        // 校验参数
        if (emailRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        String email = emailRegisterRequest.getEmail();
        String code = emailRegisterRequest.getCode();
        String userAccount = emailRegisterRequest.getUserAccount();
        String userPassword = emailRegisterRequest.getUserPassword();
        String checkPassword = emailRegisterRequest.getCheckPassword();

        // 校验邮箱格式
        if (StringUtils.isBlank(email) || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式不正确");
        }

        // 校验验证码
        if (StringUtils.isBlank(code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码不能为空");
        }

        // 校验用户名格式
        if (StringUtils.isNotBlank(userAccount) && userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名至少4位");
        }

        // 校验密码
        if (StringUtils.isBlank(userPassword) || userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码至少8位");
        }

        // 校验确认密码
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }

        // 验证验证码（从Redis中获取验证码进行比对）
        String storedCode = stringRedisTemplate.opsForValue().get("email:code:" + email);
        if (StringUtils.isBlank(storedCode) || !storedCode.equals(code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码错误或已过期");
        }

        // 验证通过后删除验证码
        stringRedisTemplate.delete("email:code:" + email);

        // 检查邮箱是否已注册
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userEmail", email);
        long count = this.baseMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该邮箱已注册");
        }

        // 如果提供了用户名，检查用户名是否已存在
        if (StringUtils.isNotBlank(userAccount)) {
            queryWrapper.clear();
            queryWrapper.eq("userAccount", userAccount);
            count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名已存在");
            }
        }

        // 加密密码
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        // 生成accessKey 和 secretKey
        String keyBase = StringUtils.isNotBlank(userAccount) ? userAccount : email;
        String accessKey = DigestUtil.sha256Hex(SALT + keyBase + RandomUtil.randomNumbers(5));
        String secretKey = DigestUtil.sha256Hex(SALT + keyBase + RandomUtil.randomNumbers(8));

        // 插入数据
        User user = new User();
        user.setUserEmail(email);
        if (StringUtils.isNotBlank(userAccount)) {
            user.setUserAccount(userAccount);
        }
        user.setUserPassword(encryptPassword);
        user.setAccessKey(accessKey);
        user.setSecretKey(secretKey);

        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
        }

        // 初始化用户积分账户
        try {
            pointService.initUserPoints(user.getId());
            log.info("用户{}积分账户初始化成功", user.getId());
        } catch (Exception e) {
            log.error("用户{}积分账户初始化失败", user.getId(), e);
            // 积分初始化失败不影响注册流程
        }

        return user.getId();
    }

    @Override
    public LoginUserVO userLoginByEmailCode(EmailLoginRequest emailLoginRequest, HttpServletRequest request) {
        // 校验参数
        if (emailLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        String email = emailLoginRequest.getEmail();
        String code = emailLoginRequest.getCode();

        // 校验邮箱格式
        if (StringUtils.isBlank(email) || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式不正确");
        }

        // 校验验证码
        if (StringUtils.isBlank(code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码不能为空");
        }

        // 验证验证码（从Redis中获取验证码进行比对）
        String storedCode = stringRedisTemplate.opsForValue().get("email:code:" + email);
        if (StringUtils.isBlank(storedCode) || !storedCode.equals(code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码错误或已过期");
        }

        // 验证通过后删除验证码
        stringRedisTemplate.delete("email:code:" + email);

        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userEmail", email);
        User user = this.baseMapper.selectOne(queryWrapper);

        // 用户不存在
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在，请先注册");
        }

        // 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        return this.getLoginUserVO(user);
    }

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    /**
     * 获取当前登录用户（允许未登录）
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUserPermitNull(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            return null;
        }
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        long userId = currentUser.getId();
        return this.getById(userId);
    }

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return isAdmin(user);
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

    /**
     * 用户注销
     *
     * @param request
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        if (request.getSession().getAttribute(USER_LOGIN_STATE) == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVO(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String unionId = userQueryRequest.getUnionId();
        String mpOpenId = userQueryRequest.getMpOpenId();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.eq(StringUtils.isNotBlank(unionId), "unionId", unionId);
        queryWrapper.eq(StringUtils.isNotBlank(mpOpenId), "mpOpenId", mpOpenId);
        queryWrapper.eq(StringUtils.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.like(StringUtils.isNotBlank(userProfile), "userProfile", userProfile);
        queryWrapper.like(StringUtils.isNotBlank(userName), "userName", userName);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public Boolean updatePassword(String oldPassword, String newPassword, HttpServletRequest request) {
        User loginUser = this.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        if (StringUtils.isAnyBlank(oldPassword, newPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + newPassword).getBytes());

        loginUser.setUserPassword(encryptPassword);
        return this.updateById(loginUser);
    }
}