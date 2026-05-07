package com.campushelp.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ch_message_recipient")
public class ChMessageRecipient {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long messageId;
    private Long userId;

    private LocalDateTime readAt;
    private LocalDateTime deletedAt;
    private LocalDateTime createdAt;
}

