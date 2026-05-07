package com.campushelp.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ch_withdraw_apply")
public class ChWithdrawApply {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;
    private String userRole;
    private Integer amountCent;
    private String status;
    private String accountNo;
    private String accountName;
    private String remark;
    private Long auditBy;
    private String auditRemark;
    private LocalDateTime auditedAt;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
