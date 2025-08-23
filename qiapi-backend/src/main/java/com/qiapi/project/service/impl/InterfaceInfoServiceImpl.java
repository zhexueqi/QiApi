package com.qiapi.project.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qiapi.client.QiApiClient;
import com.qiapi.project.common.ErrorCode;
import com.qiapi.project.constant.CommonConstant;
import com.qiapi.project.exception.BusinessException;
import com.qiapi.project.exception.ThrowUtils;
import com.qiapi.project.model.dto.interfaceinfo.InterfaceInfoQueryRequest;
import com.qiapi.project.model.enums.InterfaceInfoStatusEnum;
import com.qiapi.project.model.vo.InterfaceInfoVO;
import com.qiapi.project.service.InterfaceInfoService;
import com.qiapi.qiapicommon.model.entity.InterfaceInfo;
import com.qiapi.project.mapper.InterfaceInfoMapper;
import com.qiapi.utils.SqlUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author zhexueqi
* @description 针对表【interface_info(接口信息表)】的数据库操作Service实现
* @createDate 2024-07-27 11:27:05
*/
@Service
public class InterfaceInfoServiceImpl extends ServiceImpl<InterfaceInfoMapper, InterfaceInfo>
    implements InterfaceInfoService {

    @Resource
    private QiApiClient qiApiClient;


    @Override
    public void validInterfaceInfo(InterfaceInfo interfaceinfo, boolean add) {
        if (interfaceinfo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String name = interfaceinfo.getName();
        String description = interfaceinfo.getDescription();
        String url = interfaceinfo.getUrl();
        String requestHeader = interfaceinfo.getRequestHeader();
        String responseHeader = interfaceinfo.getResponseHeader();
        String method = interfaceinfo.getMethod();
        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isAnyBlank(name,url , requestHeader,responseHeader,method), ErrorCode.PARAMS_ERROR);
        }
        if (StringUtils.isNotBlank(name) && name.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "名称过长");
        }
        // 有参数则校验
        if (StringUtils.isNotBlank(description) && description.length() > 128) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "描述过长");
        }
    }

    /**
     * 获取接口信息VO
     * @param interfaceinfo
     * @param request
     * @return
     */
    @Override
    public InterfaceInfoVO getInterfaceInfoVO(InterfaceInfo interfaceinfo, HttpServletRequest request) {
        InterfaceInfoVO interfaceInfoVO = new InterfaceInfoVO();
        long interfaceinfoId = interfaceinfo.getId();

        InterfaceInfo interfaceInfo = this.getById(interfaceinfoId);
        BeanUtils.copyProperties(interfaceInfo, interfaceInfoVO);
        return interfaceInfoVO;
    }


    /**
     * 获取查询包装类
     *
     * @param interfaceInfoQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<InterfaceInfo> getQueryWrapper(InterfaceInfoQueryRequest interfaceInfoQueryRequest) {


        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>();
        if (interfaceInfoQueryRequest == null) {
            return queryWrapper;
        }
        String name = interfaceInfoQueryRequest.getName();
        String description = interfaceInfoQueryRequest.getDescription();
        String url = interfaceInfoQueryRequest.getUrl();
        String requestHeader = interfaceInfoQueryRequest.getRequestHeader();
        String responseHeader = interfaceInfoQueryRequest.getResponseHeader();
        Integer status = interfaceInfoQueryRequest.getStatus();
        String method = interfaceInfoQueryRequest.getMethod();
        String sortField = interfaceInfoQueryRequest.getSortField();
        String sortOrder = interfaceInfoQueryRequest.getSortOrder();
        Long id = interfaceInfoQueryRequest.getId();
        Long userId = interfaceInfoQueryRequest.getUserId();
        // 拼接查询条件
        if (StringUtils.isNotBlank(name)) {
            queryWrapper.and(qw -> qw.like("name", name).or().like("description", description));
        }
        queryWrapper.like(StringUtils.isNotBlank(url), "url", url);
        queryWrapper.like(StringUtils.isNotBlank(requestHeader), "requestHeader", requestHeader);
        queryWrapper.like(StringUtils.isNotBlank(responseHeader), "responseHeader", responseHeader);
        queryWrapper.eq(ObjectUtils.isNotEmpty(status), "status", status);
        queryWrapper.eq(StringUtils.isNotBlank(method), "method", method);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }


    /**
     * 分页获取列表（封装类）
     * @param interfaceInfoPage
     * @param request
     * @return
     */
    @Override
    public Page<InterfaceInfoVO> getInterfaceInfoVOPage(Page<InterfaceInfo> interfaceInfoPage, HttpServletRequest request) {
        List<InterfaceInfo> interfaceInfoList = interfaceInfoPage.getRecords();
        Page<InterfaceInfoVO> interfaceInfoVOPage = new Page<>(interfaceInfoPage.getCurrent(), interfaceInfoPage.getSize(), interfaceInfoPage.getTotal());
        if (CollUtil.isEmpty(interfaceInfoList)) {
            return interfaceInfoVOPage;
        }

        // 填充信息
        List<InterfaceInfoVO> interfaceInfoVOList = interfaceInfoList.stream().map(interfaceInfo -> {
            InterfaceInfoVO interfaceInfoVO = new InterfaceInfoVO();
            BeanUtils.copyProperties(interfaceInfo,interfaceInfoVO);

            return interfaceInfoVO;
        }).collect(Collectors.toList());
        interfaceInfoVOPage.setRecords(interfaceInfoVOList);
        return interfaceInfoVOPage;
    }

    @Override
    public boolean offOnline(long id, HttpServletRequest request) {
        // 判断是否存在
        InterfaceInfo oldInterfaceInfo = this.getById(id);
        ThrowUtils.throwIf(oldInterfaceInfo == null, ErrorCode.NOT_FOUND_ERROR);
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        interfaceInfo.setId(id);
        interfaceInfo.setStatus(InterfaceInfoStatusEnum.OFFLINE.getValue());
        return updateById(interfaceInfo);
    }

    @Override
    public boolean onlineInterfaceInfo(long id, HttpServletRequest request) {
        // 判断是否存在
        InterfaceInfo oldInterfaceInfo = this.getById(id);
        ThrowUtils.throwIf(oldInterfaceInfo == null, ErrorCode.NOT_FOUND_ERROR);
        // 判断该接口是否可以调用
        com.qiapi.model.User user = new com.qiapi.model.User();
        user.setName("哲学七");
        String result = qiApiClient.getNameByRestful(user);
        if (com.alibaba.excel.util.StringUtils.isBlank(result)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "接口验证失败");
        }
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        interfaceInfo.setId(id);
        interfaceInfo.setStatus(InterfaceInfoStatusEnum.ONLINE.getValue());
        return this.updateById(interfaceInfo);
    }
}




