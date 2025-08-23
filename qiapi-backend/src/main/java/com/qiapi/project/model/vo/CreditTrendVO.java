package com.qiapi.project.model.vo;

import lombok.Data;

import java.util.Date;

/**
 * 额度操作趋势统计VO
 * 
 * @author zhexueqi
 */
@Data
public class CreditTrendVO {

  /**
   * 日期
   */
  private Date date;

  /**
   * 当日总消费额度
   */
  private Long dailyConsumed;

  /**
   * 当日充值额度
   */
  private Long dailyRecharged;

  /**
   * 当日活跃用户数
   */
  private Long activeUsers;

  /**
   * 当日新增用户数
   */
  private Long newUsers;

  /**
   * 操作类型分布(JSON格式，如：{"CONSUME":100,"RECHARGE":50})
   */
  private String operationDistribution;
}