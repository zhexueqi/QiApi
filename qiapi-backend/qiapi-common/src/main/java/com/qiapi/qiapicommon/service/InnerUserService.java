package com.qiapi.qiapicommon.service;

import com.qiapi.qiapicommon.model.entity.User;

/**
 * 内部用户服务
 *
 */
public interface InnerUserService {

    /**
     * 数据库中查是否已分配给用户秘钥（accessKey）
     * 
     * @param accessKey
     * @return
     */
    User getInvokeUser(String accessKey);

    /**
     * 通过SessionID查询用户信息（用于平台内部调试）
     * 
     * @param sessionId Session ID
     * @return 用户信息
     */
    User getUserBySessionId(String sessionId);
}
