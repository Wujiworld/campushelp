package com.campushelp.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ch_message")
public class ChMessage {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String eventId;
    private String type;
    private String bizId;

    private String title;
    private String content;
    private String payloadJson;

    private LocalDateTime createdAt;
}

