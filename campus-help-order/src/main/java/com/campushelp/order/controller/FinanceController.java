package com.campushelp.order.controller;

import com.campushelp.common.security.RequireRole;
import com.campushelp.common.security.RoleEnum;
import com.campushelp.common.security.SecurityContextUtils;
import com.campushelp.order.dto.WithdrawApplyRequest;
import com.campushelp.order.dto.WithdrawAuditRequest;
import com.campushelp.order.entity.ChWithdrawApply;
import com.campushelp.order.service.AuditLogService;
import com.campushelp.order.service.TradeOpsService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Validated
@RestController
public class FinanceController {

    private final TradeOpsService tradeOpsService;
    private final AuditLogService auditLogService;

    public FinanceController(TradeOpsService tradeOpsService, AuditLogService auditLogService) {
        this.tradeOpsService = tradeOpsService;
        this.auditLogService = auditLogService;
    }

    @RequireRole({RoleEnum.MERCHANT, RoleEnum.RIDER})
    @GetMapping("/api/v3/finance/balance")
    public Map<String, Object> balance(@RequestParam String role) {
        long uid = SecurityContextUtils.requireUserId();
        Map<String, Object> resp = new HashMap<>();
        resp.put("userId", uid);
        resp.put("role", role);
        resp.put("availableAmountCent", tradeOpsService.availableBalance(uid, role));
        return resp;
    }

    @RequireRole({RoleEnum.MERCHANT, RoleEnum.RIDER})
    @PostMapping("/api/v3/finance/withdraws")
    public ChWithdrawApply applyWithdraw(@Valid @RequestBody WithdrawApplyRequest req) {
        Long uid = SecurityContextUtils.requireUserId();
        ChWithdrawApply a = tradeOpsService.applyWithdraw(uid, req);
        auditLogService.log(uid, req.getRole(), "WITHDRAW_APPLY", "WITHDRAW", String.valueOf(a.getId()), "amount=" + req.getAmountCent());
        return a;
    }

    @RequireRole({RoleEnum.MERCHANT, RoleEnum.RIDER})
    @GetMapping("/api/v3/finance/withdraws/mine")
    public List<ChWithdrawApply> myWithdraws() {
        return tradeOpsService.listMyWithdraw(SecurityContextUtils.requireUserId());
    }

    @RequireRole(RoleEnum.ADMIN)
    @GetMapping("/api/v3/admin/finance/withdraws/pending")
    public List<ChWithdrawApply> pending(@RequestParam(defaultValue = "50") @Min(1) @Max(200) int limit) {
        return tradeOpsService.listPendingWithdraw(limit);
    }

    @RequireRole(RoleEnum.ADMIN)
    @PostMapping("/api/v3/admin/finance/withdraws/{id}/approve")
    public ChWithdrawApply approve(@PathVariable("id") Long id,
                                   @Valid @RequestBody WithdrawAuditRequest req) {
        Long uid = SecurityContextUtils.requireUserId();
        ChWithdrawApply a = tradeOpsService.approveWithdraw(uid, id, req.getRemark());
        auditLogService.log(uid, "ADMIN", "WITHDRAW_APPROVE", "WITHDRAW", String.valueOf(id), req.getRemark());
        return a;
    }

    @RequireRole(RoleEnum.ADMIN)
    @PostMapping("/api/v3/admin/finance/withdraws/{id}/reject")
    public ChWithdrawApply reject(@PathVariable("id") Long id,
                                  @Valid @RequestBody WithdrawAuditRequest req) {
        Long uid = SecurityContextUtils.requireUserId();
        ChWithdrawApply a = tradeOpsService.rejectWithdraw(uid, id, req.getRemark());
        auditLogService.log(uid, "ADMIN", "WITHDRAW_REJECT", "WITHDRAW", String.valueOf(id), req.getRemark());
        return a;
    }
}
