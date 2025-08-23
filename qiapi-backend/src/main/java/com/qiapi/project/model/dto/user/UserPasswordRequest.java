package com.qiapi.project.model.dto.user;


import lombok.Data;

/**
 * @author zhexueqi
 * @ClassName UserPasswordRequest
 * @since 2025-08-24    1:15
 */
@Data
public class UserPasswordRequest {
    private String oldPassword;
    private String newPassword;
}
