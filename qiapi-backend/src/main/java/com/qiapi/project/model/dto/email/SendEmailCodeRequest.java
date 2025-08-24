package com.qiapi.project.model.dto.email;

import lombok.Data;

import javax.validation.constraints.Email;
import java.io.Serializable;

/**
 * 发送邮箱验证码请求
 */
@Data
public class SendEmailCodeRequest implements Serializable {

    /**
     * 邮箱地址
     */
    @Email
    private String email;

    private static final long serialVersionUID = 1L;
}