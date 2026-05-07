package com.campushelp.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 当前登录用户展示信息（个人中心 / 顶栏）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileVO {

    private Long userId;
    private String phone;
    private String nickname;
    private List<String> roles;
    private String avatarUrl;
    private Long campusId;
    private LocalDateTime createdAt;
}
