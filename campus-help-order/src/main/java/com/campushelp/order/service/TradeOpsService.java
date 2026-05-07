package com.campushelp.order.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.campushelp.order.dto.RefundApplyRequest;
import com.campushelp.order.dto.WithdrawApplyRequest;
import com.campushelp.order.entity.ChOrder;
import com.campushelp.order.entity.ChOrderRefund;
import com.campushelp.order.entity.ChSettlementLedger;
import com.campushelp.order.entity.ChWithdrawApply;
import com.campushelp.order.exception.BadRequestException;
import com.campushelp.order.mapper.ChOrderMapper;
import com.campushelp.order.mapper.ChOrderRefundMapper;
import com.campushelp.order.mapper.ChSettlementLedgerMapper;
import com.campushelp.order.mapper.ChWithdrawApplyMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TradeOpsService {

    private final ChOrderMapper orderMapper;
    private final ChOrderRefundMapper refundMapper;
    private final ChSettlementLedgerMapper settlementLedgerMapper;
    private final ChWithdrawApplyMapper withdrawApplyMapper;

    public TradeOpsService(ChOrderMapper orderMapper,
                          ChOrderRefundMapper refundMapper,
                          ChSettlementLedgerMapper settlementLedgerMapper,
                          ChWithdrawApplyMapper withdrawApplyMapper) {
        this.orderMapper = orderMapper;
        this.refundMapper = refundMapper;
        this.settlementLedgerMapper = settlementLedgerMapper;
        this.withdrawApplyMapper = withdrawApplyMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    public ChOrderRefund applyRefund(long userId, long orderId, RefundApplyRequest req) {
        ChOrder order = requireOrder(orderId);
        if (!order.getUserId().equals(userId)) {
            throw new BadRequestException("无权申请该订单退款");
        }
        if (!"PAID".equals(order.getPayStatus()) && !"COMPLETED".equals(order.getStatus())) {
            throw new BadRequestException("当前订单不可退款");
        }
        if (req.getAmountCent() > order.getPayAmountCent()) {
            throw new BadRequestException("退款金额不能大于实付金额");
        }
        ChOrderRefund exists = refundMapper.selectOne(new QueryWrapper<ChOrderRefund>()
                .eq("order_id", orderId)
                .eq("user_id", userId));
        if (exists != null) {
            return exists;
        }

        LocalDateTime now = LocalDateTime.now();
        ChOrderRefund refund = new ChOrderRefund();
        refund.setOrderId(orderId);
        refund.setUserId(userId);
        refund.setMerchantUserId(order.getMerchantUserId());
        refund.setApplyReason(req.getReason());
        refund.setApplyAmountCent(req.getAmountCent());
        refund.setStatus("PENDING");
        refund.setCreatedAt(now);
        refund.setUpdatedAt(now);
        refundMapper.insert(refund);

        updateOrderRefunding(orderId, now);
        return refund;
    }

    public List<ChOrderRefund> listRefundByOrder(long orderId) {
        return refundMapper.selectList(new QueryWrapper<ChOrderRefund>()
                .eq("order_id", orderId)
                .orderByDesc("created_at"));
    }

    public List<ChOrderRefund> listRefundByOrderForUser(long orderId, long userId, boolean admin) {
        ChOrder order = requireOrder(orderId);
        boolean canRead = admin
                || (order.getUserId() != null && order.getUserId().equals(userId))
                || (order.getMerchantUserId() != null && order.getMerchantUserId().equals(userId))
                || (order.getRiderUserId() != null && order.getRiderUserId().equals(userId));
        if (!canRead) {
            throw new BadRequestException("无权查看该订单退款");
        }
        return listRefundByOrder(orderId);
    }

    public List<ChOrderRefund> listRefundPending(int limit) {
        int safe = Math.min(Math.max(limit, 1), 200);
        return refundMapper.selectList(new QueryWrapper<ChOrderRefund>()
                .eq("status", "PENDING")
                .orderByAsc("created_at")
                .last("LIMIT " + safe));
    }

    @Transactional(rollbackFor = Exception.class)
    public ChOrderRefund approveRefundByMerchant(long merchantUserId, long refundId, String remark) {
        ChOrderRefund refund = requireRefund(refundId);
        ChOrder order = requireOrder(refund.getOrderId());
        if (order.getMerchantUserId() == null || !order.getMerchantUserId().equals(merchantUserId)) {
            throw new BadRequestException("无权审核该退款");
        }
        return approveRefund(refund, merchantUserId, "MERCHANT", remark);
    }

    @Transactional(rollbackFor = Exception.class)
    public ChOrderRefund approveRefundByAdmin(long adminUserId, long refundId, String remark) {
        ChOrderRefund refund = requireRefund(refundId);
        return approveRefund(refund, adminUserId, "ADMIN", remark);
    }

    @Transactional(rollbackFor = Exception.class)
    public ChOrderRefund rejectRefundByAdmin(long adminUserId, long refundId, String remark) {
        ChOrderRefund refund = requireRefund(refundId);
        return rejectRefund(refund, adminUserId, "ADMIN", remark);
    }

    @Transactional(rollbackFor = Exception.class)
    public ChOrderRefund rejectRefundByMerchant(long merchantUserId, long refundId, String remark) {
        ChOrderRefund refund = requireRefund(refundId);
        ChOrder order = requireOrder(refund.getOrderId());
        if (order.getMerchantUserId() == null || !order.getMerchantUserId().equals(merchantUserId)) {
            throw new BadRequestException("无权审核该退款");
        }
        return rejectRefund(refund, merchantUserId, "MERCHANT", remark);
    }

    @Transactional(rollbackFor = Exception.class)
    public void createIncomeLedgerForCompletedOrder(ChOrder order) {
        if (order == null || !"COMPLETED".equals(order.getStatus()) || !"PAID".equals(order.getPayStatus())) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        int deliveryFee = order.getDeliveryFeeCent() == null ? 0 : order.getDeliveryFeeCent();
        int payAmount = order.getPayAmountCent() == null ? 0 : order.getPayAmountCent();
        int merchantIncome = Math.max(payAmount - deliveryFee, 0);
        if (order.getMerchantUserId() != null && merchantIncome > 0) {
            createLedgerIfMissing(order.getId(), order.getMerchantUserId(), "MERCHANT", merchantIncome, "ORDER_INCOME", null, now);
        }
        if (order.getRiderUserId() != null && deliveryFee > 0) {
            createLedgerIfMissing(order.getId(), order.getRiderUserId(), "RIDER", deliveryFee, "ORDER_INCOME", null, now);
        }
    }

    private void createLedgerIfMissing(long orderId, long userId, String role, int amount, String bizType, Long bizId, LocalDateTime now) {
        long exists = settlementLedgerMapper.selectCount(new QueryWrapper<ChSettlementLedger>()
                .eq("order_id", orderId)
                .eq("user_id", userId)
                .eq("user_role", role)
                .eq("biz_type", bizType));
        if (exists > 0) {
            return;
        }
        ChSettlementLedger ledger = new ChSettlementLedger();
        ledger.setOrderId(orderId);
        ledger.setUserId(userId);
        ledger.setUserRole(role);
        ledger.setAmountCent(amount);
        ledger.setStatus("AVAILABLE");
        ledger.setBizType(bizType);
        ledger.setBizId(bizId);
        ledger.setCreatedAt(now);
        ledger.setUpdatedAt(now);
        settlementLedgerMapper.insert(ledger);
    }

    @Transactional(rollbackFor = Exception.class)
    public ChWithdrawApply applyWithdraw(long userId, WithdrawApplyRequest req) {
        Integer available = settlementLedgerMapper.sumAvailable(userId, req.getRole());
        int canUse = available == null ? 0 : available;
        if (req.getAmountCent() > canUse) {
            throw new BadRequestException("可提现余额不足");
        }
        LocalDateTime now = LocalDateTime.now();
        ChWithdrawApply apply = new ChWithdrawApply();
        apply.setUserId(userId);
        apply.setUserRole(req.getRole());
        apply.setAmountCent(req.getAmountCent());
        apply.setStatus("PENDING");
        apply.setAccountNo(req.getAccountNo());
        apply.setAccountName(req.getAccountName());
        apply.setRemark(req.getRemark());
        apply.setCreatedAt(now);
        apply.setUpdatedAt(now);
        withdrawApplyMapper.insert(apply);
        return apply;
    }

    public List<ChWithdrawApply> listMyWithdraw(long userId) {
        return withdrawApplyMapper.selectList(new QueryWrapper<ChWithdrawApply>()
                .eq("user_id", userId)
                .orderByDesc("created_at"));
    }

    public List<ChWithdrawApply> listPendingWithdraw(int limit) {
        int safe = Math.min(Math.max(limit, 1), 200);
        return withdrawApplyMapper.selectList(new QueryWrapper<ChWithdrawApply>()
                .eq("status", "PENDING")
                .orderByAsc("created_at")
                .last("LIMIT " + safe));
    }

    @Transactional(rollbackFor = Exception.class)
    public ChWithdrawApply approveWithdraw(long adminUserId, long applyId, String remark) {
        ChWithdrawApply apply = requireWithdraw(applyId);
        if (!"PENDING".equals(apply.getStatus())) {
            throw new BadRequestException("仅待审核申请可通过");
        }
        LocalDateTime now = LocalDateTime.now();
        Integer available = settlementLedgerMapper.sumAvailable(apply.getUserId(), apply.getUserRole());
        int canUse = available == null ? 0 : available;
        if (apply.getAmountCent() > canUse) {
            throw new BadRequestException("余额不足，无法通过");
        }

        ChSettlementLedger ledger = new ChSettlementLedger();
        ledger.setOrderId(0L);
        ledger.setUserId(apply.getUserId());
        ledger.setUserRole(apply.getUserRole());
        ledger.setAmountCent(-apply.getAmountCent());
        ledger.setStatus("SETTLED");
        ledger.setBizType("WITHDRAW");
        ledger.setBizId(apply.getId());
        ledger.setCreatedAt(now);
        ledger.setUpdatedAt(now);
        settlementLedgerMapper.insert(ledger);

        apply.setStatus("APPROVED");
        apply.setAuditBy(adminUserId);
        apply.setAuditRemark(remark);
        apply.setAuditedAt(now);
        apply.setUpdatedAt(now);
        withdrawApplyMapper.updateById(apply);
        return apply;
    }

    @Transactional(rollbackFor = Exception.class)
    public ChWithdrawApply rejectWithdraw(long adminUserId, long applyId, String remark) {
        ChWithdrawApply apply = requireWithdraw(applyId);
        if (!"PENDING".equals(apply.getStatus())) {
            throw new BadRequestException("仅待审核申请可驳回");
        }
        apply.setStatus("REJECTED");
        apply.setAuditBy(adminUserId);
        apply.setAuditRemark(remark);
        apply.setAuditedAt(LocalDateTime.now());
        apply.setUpdatedAt(LocalDateTime.now());
        withdrawApplyMapper.updateById(apply);
        return apply;
    }

    public Integer availableBalance(long userId, String role) {
        Integer v = settlementLedgerMapper.sumAvailable(userId, role);
        return v == null ? 0 : v;
    }

    private ChOrderRefund approveRefund(ChOrderRefund refund, long by, String byRole, String remark) {
        if (!"PENDING".equals(refund.getStatus())) {
            throw new BadRequestException("仅待审核退款可通过");
        }
        LocalDateTime now = LocalDateTime.now();
        refund.setStatus("APPROVED");
        refund.setAuditBy(by);
        refund.setAuditRole(byRole);
        refund.setAuditRemark(remark);
        refund.setAuditedAt(now);
        refund.setUpdatedAt(now);
        refundMapper.updateById(refund);

        ChOrder order = requireOrder(refund.getOrderId());
        UpdateWrapper<ChOrder> u = new UpdateWrapper<ChOrder>()
                .eq("id", order.getId())
                .set("status", "REFUNDED")
                .set("pay_status", "REFUNDED")
                .set("updated_at", now);
        orderMapper.update(null, u);

        rollbackIncomeLedger(order.getId(), refund.getId(), now);
        return refund;
    }

    private ChOrderRefund rejectRefund(ChOrderRefund refund, long by, String byRole, String remark) {
        if (!"PENDING".equals(refund.getStatus())) {
            throw new BadRequestException("仅待审核退款可驳回");
        }
        LocalDateTime now = LocalDateTime.now();
        refund.setStatus("REJECTED");
        refund.setAuditBy(by);
        refund.setAuditRole(byRole);
        refund.setAuditRemark(remark);
        refund.setAuditedAt(now);
        refund.setUpdatedAt(now);
        refundMapper.updateById(refund);

        UpdateWrapper<ChOrder> u = new UpdateWrapper<ChOrder>()
                .eq("id", refund.getOrderId())
                .set("status", "PAID")
                .set("pay_status", "PAID")
                .set("updated_at", now);
        orderMapper.update(null, u);
        return refund;
    }

    private void rollbackIncomeLedger(long orderId, long refundId, LocalDateTime now) {
        List<ChSettlementLedger> incomes = settlementLedgerMapper.selectList(new QueryWrapper<ChSettlementLedger>()
                .eq("order_id", orderId)
                .eq("biz_type", "ORDER_INCOME"));
        for (ChSettlementLedger income : incomes) {
            ChSettlementLedger rollback = new ChSettlementLedger();
            rollback.setOrderId(orderId);
            rollback.setUserId(income.getUserId());
            rollback.setUserRole(income.getUserRole());
            rollback.setAmountCent(-Math.abs(income.getAmountCent()));
            rollback.setStatus("SETTLED");
            rollback.setBizType("REFUND_ROLLBACK");
            rollback.setBizId(refundId);
            rollback.setCreatedAt(now);
            rollback.setUpdatedAt(now);
            settlementLedgerMapper.insert(rollback);
        }
    }

    private void updateOrderRefunding(long orderId, LocalDateTime now) {
        UpdateWrapper<ChOrder> u = new UpdateWrapper<ChOrder>()
                .eq("id", orderId)
                .set("status", "REFUNDING")
                .set("pay_status", "REFUNDING")
                .set("updated_at", now);
        orderMapper.update(null, u);
    }

    private ChOrderRefund requireRefund(long refundId) {
        ChOrderRefund refund = refundMapper.selectById(refundId);
        if (refund == null) {
            throw new BadRequestException("退款申请不存在");
        }
        return refund;
    }

    private ChWithdrawApply requireWithdraw(long applyId) {
        ChWithdrawApply apply = withdrawApplyMapper.selectById(applyId);
        if (apply == null) {
            throw new BadRequestException("提现申请不存在");
        }
        return apply;
    }

    private ChOrder requireOrder(long orderId) {
        ChOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BadRequestException("订单不存在");
        }
        return order;
    }
}
