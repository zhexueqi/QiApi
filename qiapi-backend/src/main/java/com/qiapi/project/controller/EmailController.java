package com.qiapi.project.controller;

import com.qiapi.project.common.BaseResponse;
import com.qiapi.project.common.ErrorCode;
import com.qiapi.project.common.ResultUtils;
import com.qiapi.project.exception.BusinessException;
import com.qiapi.project.model.dto.email.SendEmailCodeRequest;
import com.qiapi.project.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 邮箱相关接口
 */
@RestController
@RequestMapping("/email")
@Slf4j
public class EmailController {

  @Autowired
  private EmailService emailService;

  @Resource
  private StringRedisTemplate stringRedisTemplate;

  /**
   * 发送邮箱验证码
   *
   * @param sendEmailCodeRequest 发送验证码请求
   * @return 是否发送成功
   */
  @PostMapping("/send/code")
  public BaseResponse<Boolean> sendVerificationCode(@RequestBody @Validated SendEmailCodeRequest sendEmailCodeRequest) {
    if (sendEmailCodeRequest == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }

    String email = sendEmailCodeRequest.getEmail();
    if (StringUtils.isBlank(email) || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式不正确");
    }

    // 生成6位随机验证码
    String code = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));

    // 发送邮件
    boolean result = emailService.sendVerificationCode(email, code);
    if (!result) {
      throw new BusinessException(ErrorCode.SYSTEM_ERROR, "验证码发送失败");
    }

    // 将验证码存入Redis，设置5分钟过期时间
    stringRedisTemplate.opsForValue().set("email:code:" + email, code, 5, TimeUnit.MINUTES);

    return ResultUtils.success(true);
  }
}