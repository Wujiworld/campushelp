package com.campushelp.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ch_address")
public class ChAddress {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;
    private Long campusId;
    private Long buildingId;
    private String contactName;
    private String contactPhone;
    private String detail;
    private String label;
    private Integer isDefault;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
