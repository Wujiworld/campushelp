package com.campushelp.order.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.campushelp.order.entity.ChAuditLog;
import com.campushelp.order.mapper.ChAuditLogMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditLogService {

    private final ChAuditLogMapper auditLogMapper;

    public AuditLogService(ChAuditLogMapper auditLogMapper) {
        this.auditLogMapper = auditLogMapper;
    }

    public void log(Long operatorUserId, String operatorRole, String action, String targetType, String targetId, String detail) {
        ChAuditLog l = new ChAuditLog();
        l.setOperatorUserId(operatorUserId);
        l.setOperatorRole(operatorRole);
        l.setAction(action);
        l.setTargetType(targetType);
        l.setTargetId(targetId);
        l.setDetail(detail);
        l.setCreatedAt(LocalDateTime.now());
        auditLogMapper.insert(l);
    }

    public List<ChAuditLog> list(int limit) {
        int safe = Math.min(Math.max(limit, 1), 500);
        return auditLogMapper.selectList(new QueryWrapper<ChAuditLog>()
                .orderByDesc("created_at")
                .last("LIMIT " + safe));
    }
}
