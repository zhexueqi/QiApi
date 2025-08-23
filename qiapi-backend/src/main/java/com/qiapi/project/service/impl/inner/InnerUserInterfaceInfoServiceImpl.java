package com.qiapi.project.service.impl.inner;

import com.qiapi.qiapicommon.model.entity.UserInterfaceInfo;
import com.qiapi.qiapicommon.service.InnerUserInterfaceInfoService;
import org.apache.dubbo.config.annotation.DubboService;
import com.qiapi.service.UserInterfaceInfoService;
import javax.annotation.Resource;

/**
 * 内部用户接口信息服务实现类
 *
 */
@DubboService
public class InnerUserInterfaceInfoServiceImpl implements InnerUserInterfaceInfoService {

    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;

    @Override
    public boolean invokeCount(long interfaceInfoId, long userId) {
        return userInterfaceInfoService.invokeCount(interfaceInfoId, userId);
    }

    @Override
    public UserInterfaceInfo getUserInterfaceInfo(long interfaceInfoId, long userId) {
        return userInterfaceInfoService.getUserInterfaceInfo(interfaceInfoId, userId);
    }
}
