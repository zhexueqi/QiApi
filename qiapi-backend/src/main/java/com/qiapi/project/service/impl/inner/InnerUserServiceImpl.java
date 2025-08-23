package com.qiapi.project.service.impl.inner;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.qiapi.project.common.ErrorCode;
import com.qiapi.project.exception.BusinessException;
import com.qiapi.project.mapper.UserMapper;
import com.qiapi.qiapicommon.model.entity.User;
import com.qiapi.qiapicommon.service.InnerUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;

import static com.qiapi.project.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 内部用户服务实现类
 *
 */
@DubboService
@Slf4j
public class InnerUserServiceImpl implements InnerUserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public User getInvokeUser(String accessKey) {
        if (StringUtils.isAnyBlank(accessKey)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("accessKey", accessKey);
        return userMapper.selectOne(queryWrapper);
    }

    @Override
    public User getUserBySessionId(String sessionId) {
        if (StringUtils.isBlank(sessionId)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "SessionID不能为空");
        }

        try {
            // 通过Redis查询Session信息
            // Spring Session默认的key格式为: spring:session:sessions:{sessionId}
            String sessionKey = "spring:session:sessions:" + sessionId;

            // 获取Session中的用户信息，使用项目标准的key：USER_LOGIN_STATE
            Object userObj = redisTemplate.opsForHash().get(sessionKey, "sessionAttr:" + USER_LOGIN_STATE);

            if (userObj == null) {
                log.warn("无法从Session中获取用户信息 - SessionID: {}", sessionId);
                return null;
            }

            // 按照项目标准方式处理用户对象
            User currentUser = (User) userObj;
            if (currentUser == null || currentUser.getId() == null) {
                log.warn("Session中的用户信息无效 - SessionID: {}", sessionId);
                return null;
            }

            // 从数据库查询最新的用户信息（保持与项目一致的做法）
            long userId = currentUser.getId();
            User latestUser = userMapper.selectById(userId);
            if (latestUser == null) {
                log.warn("数据库中未找到用户信息 - SessionID: {}, UserID: {}", sessionId, userId);
                return null;
            }

            log.info("成功通过Session获取用户信息 - SessionID: {}, UserID: {}, UserAccount: {}",
                    sessionId, latestUser.getId(), latestUser.getUserAccount());
            return latestUser;

        } catch (Exception e) {
            log.error("通过SessionID查询用户信息失败 - SessionID: {}", sessionId, e);
            return null;
        }
    }
}
