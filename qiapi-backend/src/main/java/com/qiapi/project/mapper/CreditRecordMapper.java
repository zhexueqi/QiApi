package com.qiapi.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qiapi.project.model.vo.CreditTrendVO;
import com.qiapi.qiapicommon.model.entity.CreditRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 额度记录数据库操作
 */
@Mapper
public interface CreditRecordMapper extends BaseMapper<CreditRecord> {

  /**
   * 获取指定日期范围内的额度操作趋势
   * 
   * @param startDate 开始日期
   * @param endDate   结束日期
   * @return 趋势统计列表
   */
  List<CreditTrendVO> getCreditTrendByDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

  /**
   * 获取最近N天的额度操作趋势
   * 
   * @param days 天数
   * @return 趋势统计列表
   */
  List<CreditTrendVO> getRecentCreditTrend(@Param("days") int days);

}