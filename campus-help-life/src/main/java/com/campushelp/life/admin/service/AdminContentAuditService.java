package com.campushelp.life.admin.service;

import com.campushelp.common.api.ResultCode;
import com.campushelp.common.exception.BusinessException;
import com.campushelp.life.client.LifeCatalogClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class AdminContentAuditService {

    private final LifeCatalogClient lifeCatalogClient;

    public AdminContentAuditService(LifeCatalogClient lifeCatalogClient) {
        this.lifeCatalogClient = lifeCatalogClient;
    }

    public Map<String, Object> pending(int limit) {
        int safe = Math.min(Math.max(limit, 1), 200);
        Map<String, Object> r = new HashMap<>();
        r.put("secondhand", lifeCatalogClient.listSecondhandItems(null, "AUDITING", null, null, null, "NEWEST"));
        r.put("agent", lifeCatalogClient.listAgentItems(null, "AUDITING"));
        r.put("comment", lifeCatalogClient.listComments("SECONDHAND_ITEM", 0L, "AUDITING", 0, safe));
        return r;
    }

    @Transactional(rollbackFor = Exception.class)
    public void approve(String contentType, long id) {
        updateStatus(contentType, id, "ON_SALE", "VISIBLE");
    }

    @Transactional(rollbackFor = Exception.class)
    public void reject(String contentType, long id) {
        updateStatus(contentType, id, "REJECTED", "HIDDEN");
    }

    private void updateStatus(String contentType, long id, String itemStatus, String commentStatus) {
        String t = contentType == null ? "" : contentType.trim().toUpperCase();
        if ("SECONDHAND".equals(t)) {
            if (lifeCatalogClient.getSecondhandItem(id) == null) throw new BusinessException(ResultCode.BIZ_RULE, "内容不存在");
            lifeCatalogClient.updateSecondhandStatus(id, "AUDITING", itemStatus);
            return;
        }
        if ("AGENT".equals(t)) {
            if (lifeCatalogClient.getAgentItem(id) == null) throw new BusinessException(ResultCode.BIZ_RULE, "内容不存在");
            if ("ON_SALE".equals(itemStatus)) {
                lifeCatalogClient.restoreAgentItem(id);
            } else {
                lifeCatalogClient.offlineAgentItem(id);
            }
            return;
        }
        if ("COMMENT".equals(t)) {
            if (lifeCatalogClient.getComment(id) == null) throw new BusinessException(ResultCode.BIZ_RULE, "内容不存在");
            lifeCatalogClient.updateCommentStatus(id, commentStatus);
            return;
        }
        throw new BusinessException(ResultCode.BIZ_RULE, "不支持的 contentType");
    }
}
