package com.qiapi.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qiapi.qiapicommon.model.entity.UserInterfaceInfo;

import java.util.List;

/**
 * 用户接口信息 Mapper
 *
 * @author zhexueqi
 */
public interface UserInterfaceInfoMapper extends BaseMapper<UserInterfaceInfo> {

    List<UserInterfaceInfo> listTopInvokeInterfaceInfo(int limit);
}




