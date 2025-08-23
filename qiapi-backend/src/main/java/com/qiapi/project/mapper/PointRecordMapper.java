package com.qiapi.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qiapi.qiapicommon.model.entity.PointRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 积分记录数据库操作
 */
@Mapper
public interface PointRecordMapper extends BaseMapper<PointRecord> {

}