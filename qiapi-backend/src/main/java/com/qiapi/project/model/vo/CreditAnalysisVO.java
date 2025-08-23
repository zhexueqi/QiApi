package com.qiapi.project.model.vo;

import lombok.Data;

/**
 * 额度分析统计VO
 * 
 * @author zhexueqi
 */
@Data
public class CreditAnalysisVO {

  /**
   * 接口ID
   */
  private Long interfaceId;

  /**
   * 接口名称
   */
  private String interfaceName;

  /**
   * 总额度消费量
   */
  private Long totalCreditConsumed;

  /**
   * 总剩余额度
   */
  private Long totalRemainingCredit;

  /**
   * 用户数量
   */
  private Long userCount;

  /**
   * 平均每用户消费额度
   */
  private Double avgCreditPerUser;
}