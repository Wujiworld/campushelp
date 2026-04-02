package com.campushelp.product.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ch_store")
public class ChStore {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long merchantUserId;
    private Long campusId;
    private String name;
    private Integer type;
    private Integer status;
    private String openTime;
    private String closeTime;
    private String notice;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

