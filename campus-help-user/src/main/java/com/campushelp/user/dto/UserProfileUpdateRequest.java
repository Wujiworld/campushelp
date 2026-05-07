package com.campushelp.user.dto;

import lombok.Data;

@Data
public class UserProfileUpdateRequest {

    private String nickname;
    private String avatarUrl;
    private Long campusId;
}
