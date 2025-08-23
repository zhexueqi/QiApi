package com.qiapi.project.model.vo;

import lombok.Data;

/**
 * 用户额度统计VO
 * 
 * @author zhexueqi
 */
@Data
public class UserCreditStatsVO {

  /**
   * 用户ID
   */
  private Long userId;

  /**
   * 用户名
   */
  private String userName;

  /**
   * 总消费额度
   */
  private Long totalConsumedCredit;

  /**
   * 总剩余额度
   */
  private Long totalRemainingCredit;

  /**
   * 活跃接口数量
   */
  private Long activeInterfaceCount;

  /**
   * 平均每接口消费额度
   */
  private Double avgCreditPerInterface;
}