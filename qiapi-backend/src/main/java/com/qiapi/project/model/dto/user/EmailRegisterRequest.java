package com.qiapi.project.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 邮箱注册请求
 */
@Data
public class EmailRegisterRequest implements Serializable {

  /**
   * 邮箱地址
   */
  private String email;

  /**
   * 验证码
   */
  private String code;

  /**
   * 用户名（可选）
   */
  private String userAccount;

  /**
   * 密码
   */
  private String userPassword;

  /**
   * 确认密码
   */
  private String checkPassword;

  private static final long serialVersionUID = 1L;
}