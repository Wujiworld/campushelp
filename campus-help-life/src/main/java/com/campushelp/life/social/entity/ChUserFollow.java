package com.campushelp.life.social.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ch_user_follow")
public class ChUserFollow {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long followerUserId;
    private Long followeeUserId;
    private LocalDateTime createdAt;
}
