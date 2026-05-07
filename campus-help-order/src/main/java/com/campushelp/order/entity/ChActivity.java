package com.campushelp.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ch_activity")
public class ChActivity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long campusId;
    private String title;
    private String description;
    private String place;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;

    private Integer likeCount;

    private Long createdBy;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
