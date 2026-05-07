package com.campushelp.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ch_outbox")
public class ChOutbox {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String eventId;
    private String payloadJson;
    private LocalDateTime createdAt;
    private LocalDateTime publishedAt;
}
