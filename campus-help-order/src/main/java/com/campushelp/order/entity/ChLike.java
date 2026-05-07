package com.campushelp.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ch_like")
public class ChLike {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;
    private String targetType;
    private Long targetId;
    private LocalDateTime createdAt;
}
