package com.campushelp.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ch_comment")
public class ChComment {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;
    private String targetType;
    private Long targetId;
    private String content;
    private String status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
