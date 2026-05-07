package com.campushelp.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 对应表 {@code ch_user_role}：用户与角色多对多。
 */
@Data
@TableName("ch_user_role")
public class ChUserRole {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;
    private Long roleId;
    private LocalDateTime createdAt;
}
