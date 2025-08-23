package com.qiapi.project.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户密钥信息视图对象
 *
 * @author zhexueqi
 */
@Data
public class UserKeyVO implements Serializable {

    /**
     * 访问密钥
     */
    private String accessKey;

    /**
     * 秘密密钥（可能被掩码处理）
     */
    private String secretKey;

    /**
     * 是否已生成密钥
     */
    private Boolean hasKeys;

    /**
     * 密钥生成时间
     */
    private String generateTime;

    /**
     * 密钥使用说明
     */
    private String usage;

    private static final long serialVersionUID = 1L;

    public UserKeyVO() {
        this.usage = "请妥善保管您的密钥信息，不要泄露给他人。AccessKey用于身份识别，SecretKey用于签名验证。";
        this.generateTime = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}