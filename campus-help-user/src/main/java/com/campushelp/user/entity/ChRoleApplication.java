package com.campushelp.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ch_role_application")
public class ChRoleApplication {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;
    private String roleCode;
    private String docType;
    private String docNo;
    private String docImagesJson;
    private String status;
    private String submitRemark;
    private String auditRemark;
    private Long auditedBy;
    private LocalDateTime auditedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
