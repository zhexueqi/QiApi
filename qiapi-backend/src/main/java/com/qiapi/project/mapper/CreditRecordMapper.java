package com.qiapi.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qiapi.qiapicommon.model.entity.CreditRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 额度记录数据库操作
 */
@Mapper
public interface CreditRecordMapper extends BaseMapper<CreditRecord> {

}