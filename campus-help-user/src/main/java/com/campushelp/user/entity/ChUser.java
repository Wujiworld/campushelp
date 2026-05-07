package com.campushelp.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 对应表 {@code ch_user}：统一账号（手机号登录）。
 */
@Data
@TableName("ch_user")
public class ChUser {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String phone;

    /** BCrypt 哈希；短信登录场景可为空 */
    private String passwordHash;

    private String nickname;
    private String avatarUrl;
    private Integer status;
    private Long campusId;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
