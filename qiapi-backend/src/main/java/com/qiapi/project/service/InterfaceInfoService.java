package com.qiapi.project.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qiapi.project.model.dto.interfaceinfo.InterfaceInfoQueryRequest;
import com.qiapi.project.model.vo.InterfaceInfoVO;
import com.qiapi.qiapicommon.model.entity.InterfaceInfo;

import javax.servlet.http.HttpServletRequest;

/**
* @author zhexueqi
*/
public interface InterfaceInfoService extends IService<InterfaceInfo> {

    void validInterfaceInfo(InterfaceInfo interfaceinfo, boolean add);

    /**
     * 获取接口信息封装
     * @param interfaceinfo
     * @param request
     * @return
     */
    InterfaceInfoVO getInterfaceInfoVO(InterfaceInfo interfaceinfo, HttpServletRequest request);

    /**
     * 获取查询包装类
     * @param interfaceInfoQueryRequest
     * @return
     */
    QueryWrapper<InterfaceInfo> getQueryWrapper(InterfaceInfoQueryRequest interfaceInfoQueryRequest);

    /**
     * 分页获取接口封装
     * @param interfaceInfoPage
     * @param request
     * @return
     */
    Page<InterfaceInfoVO> getInterfaceInfoVOPage(Page<InterfaceInfo> interfaceInfoPage, HttpServletRequest request);

    /**
     * 下线接口
     * @param id
     * @param request
     * @return
     */
    boolean offOnline(long id, HttpServletRequest request);

    /**
     * 上线接口
     * @param id
     * @param request
     * @return
     */
    boolean onlineInterfaceInfo(long id, HttpServletRequest request);
}
