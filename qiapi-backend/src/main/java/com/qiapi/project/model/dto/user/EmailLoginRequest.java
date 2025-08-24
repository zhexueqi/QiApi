package com.qiapi.project.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 邮箱登录请求
 */
@Data
public class EmailLoginRequest implements Serializable {

  /**
   * 邮箱地址
   */
  private String email;

  /**
   * 验证码
   */
  private String code;

  private static final long serialVersionUID = 1L;
}