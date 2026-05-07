package com.campushelp.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ch_settlement_ledger")
public class ChSettlementLedger {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long orderId;
    private Long userId;
    private String userRole;
    private Integer amountCent;
    private String status;
    private String bizType;
    private Long bizId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
