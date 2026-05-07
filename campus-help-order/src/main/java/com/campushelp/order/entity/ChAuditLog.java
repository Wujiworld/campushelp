package com.campushelp.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ch_audit_log")
public class ChAuditLog {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long operatorUserId;
    private String operatorRole;
    private String action;
    private String targetType;
    private String targetId;
    private String detail;
    private LocalDateTime createdAt;
}
