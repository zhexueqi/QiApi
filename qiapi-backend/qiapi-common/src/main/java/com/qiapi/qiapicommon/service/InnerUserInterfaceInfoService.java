package com.qiapi.qiapicommon.service;

import com.qiapi.qiapicommon.model.entity.UserInterfaceInfo;

/**
 * 内部用户接口信息服务
 *
 */
public interface InnerUserInterfaceInfoService {

    /**
     * 调用接口统计
     * @param interfaceInfoId
     * @param userId
     * @return
     */
    boolean invokeCount(long interfaceInfoId, long userId);

    /**
     * 获取用户调用接口信息
     */
    UserInterfaceInfo getUserInterfaceInfo(long interfaceInfoId, long userId);
}
