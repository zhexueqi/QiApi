package com.qiapi.project.service;

/**
 * 邮件服务
 */
public interface EmailService {

  /**
   * 发送验证码邮件
   *
   * @param toEmail 接收邮箱
   * @param code    验证码
   * @return 是否发送成功
   */
  boolean sendVerificationCode(String toEmail, String code);
}