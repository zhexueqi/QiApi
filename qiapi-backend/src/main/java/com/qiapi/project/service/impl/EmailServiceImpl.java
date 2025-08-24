package com.qiapi.project.service.impl;

import com.qiapi.project.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * 邮件服务实现类
 */
@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

  @Autowired
  private JavaMailSender mailSender;

  @Value("${spring.mail.username}")
  private String fromEmail;

  @Override
  public boolean sendVerificationCode(String toEmail, String code) {
    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setFrom(fromEmail);
      message.setTo(toEmail);
      message.setSubject("QiAPI平台验证码");
      message.setText("您的验证码是: " + code + "，5分钟内有效。请勿泄露给他人。");

      mailSender.send(message);
      log.info("验证码邮件发送成功，收件人: {}", toEmail);
      return true;
    } catch (Exception e) {
      log.error("验证码邮件发送失败，收件人: {}", toEmail, e);
      return false;
    }
  }
}