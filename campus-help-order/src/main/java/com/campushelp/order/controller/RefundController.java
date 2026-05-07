package com.campushelp.order.controller;

import com.campushelp.common.security.RequireRole;
import com.campushelp.common.security.RoleEnum;
import com.campushelp.common.security.SecurityContextUtils;
import com.campushelp.order.dto.RefundApplyRequest;
import com.campushelp.order.dto.RefundAuditRequest;
import com.campushelp.order.entity.ChOrderRefund;
import com.campushelp.order.service.AuditLogService;
import com.campushelp.order.service.TradeOpsService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.List;

@Validated
@RestController
public class RefundController {

    private final TradeOpsService tradeOpsService;
    private final AuditLogService auditLogService;

    public RefundController(TradeOpsService tradeOpsService, AuditLogService auditLogService) {
        this.tradeOpsService = tradeOpsService;
        this.auditLogService = auditLogService;
    }

    @PostMapping("/api/v3/orders/{id}/refunds")
    public ChOrderRefund apply(@PathVariable("id") Long orderId, @Valid @RequestBody RefundApplyRequest req) {
        Long uid = SecurityContextUtils.requireUserId();
        ChOrderRefund r = tradeOpsService.applyRefund(uid, orderId, req);
        auditLogService.log(uid, "USER", "REFUND_APPLY", "ORDER", String.valueOf(orderId), req.getReason());
        return r;
    }

    @GetMapping("/api/v3/orders/{id}/refunds")
    public List<ChOrderRefund> listByOrder(@PathVariable("id") Long orderId) {
        long uid = SecurityContextUtils.requireUserId();
        boolean admin = SecurityContextUtils.hasRole("ADMIN");
        return tradeOpsService.listRefundByOrderForUser(orderId, uid, admin);
    }

    @RequireRole(RoleEnum.MERCHANT)
    @PostMapping("/api/v3/merchant/refunds/{id}/approve")
    public ChOrderRefund merchantApprove(@PathVariable("id") Long refundId,
                                         @Valid @RequestBody RefundAuditRequest req) {
        Long uid = SecurityContextUtils.requireUserId();
        ChOrderRefund r = tradeOpsService.approveRefundByMerchant(uid, refundId, req.getRemark());
        auditLogService.log(uid, "MERCHANT", "REFUND_APPROVE", "REFUND", String.valueOf(refundId), req.getRemark());
        return r;
    }

    @RequireRole(RoleEnum.MERCHANT)
    @PostMapping("/api/v3/merchant/refunds/{id}/reject")
    public ChOrderRefund merchantReject(@PathVariable("id") Long refundId,
                                        @Valid @RequestBody RefundAuditRequest req) {
        Long uid = SecurityContextUtils.requireUserId();
        ChOrderRefund r = tradeOpsService.rejectRefundByMerchant(uid, refundId, req.getRemark());
        auditLogService.log(uid, "MERCHANT", "REFUND_REJECT", "REFUND", String.valueOf(refundId), req.getRemark());
        return r;
    }

    @RequireRole(RoleEnum.ADMIN)
    @GetMapping("/api/v3/admin/refunds/pending")
    public List<ChOrderRefund> adminPending(@RequestParam(defaultValue = "50") @Min(1) @Max(200) int limit) {
        return tradeOpsService.listRefundPending(limit);
    }

    @RequireRole(RoleEnum.ADMIN)
    @PostMapping("/api/v3/admin/refunds/{id}/approve")
    public ChOrderRefund adminApprove(@PathVariable("id") Long refundId,
                                      @Valid @RequestBody RefundAuditRequest req) {
        Long uid = SecurityContextUtils.requireUserId();
        ChOrderRefund r = tradeOpsService.approveRefundByAdmin(uid, refundId, req.getRemark());
        auditLogService.log(uid, "ADMIN", "REFUND_APPROVE", "REFUND", String.valueOf(refundId), req.getRemark());
        return r;
    }

    @RequireRole(RoleEnum.ADMIN)
    @PostMapping("/api/v3/admin/refunds/{id}/reject")
    public ChOrderRefund adminReject(@PathVariable("id") Long refundId,
                                     @Valid @RequestBody RefundAuditRequest req) {
        Long uid = SecurityContextUtils.requireUserId();
        ChOrderRefund r = tradeOpsService.rejectRefundByAdmin(uid, refundId, req.getRemark());
        auditLogService.log(uid, "ADMIN", "REFUND_REJECT", "REFUND", String.valueOf(refundId), req.getRemark());
        return r;
    }
}
