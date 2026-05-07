package com.campushelp.life.agent.service;

import com.campushelp.life.agent.dto.AgentItemCreateRequest;
import com.campushelp.life.client.LifeCatalogClient;
import com.campushelp.life.client.dto.AgentItemDto;
import com.campushelp.common.exception.ValidationException;
import com.campushelp.common.safety.SensitiveWordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AgentCatalogService {

    private final LifeCatalogClient lifeCatalogClient;
    private final SensitiveWordService sensitiveWordService;

    public AgentCatalogService(LifeCatalogClient lifeCatalogClient, SensitiveWordService sensitiveWordService) {
        this.lifeCatalogClient = lifeCatalogClient;
        this.sensitiveWordService = sensitiveWordService;
    }

    public List<AgentItemDto> list(Long campusId, String status) {
        return lifeCatalogClient.listAgentItems(campusId, status);
    }

    public AgentItemDto getById(long id) {
        return lifeCatalogClient.getAgentItem(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public AgentItemDto publish(long sellerUserId, long campusId, AgentItemCreateRequest req) {
        String title = req.getTitle() == null ? "" : req.getTitle().trim();
        String desc = req.getDescription();
        if (sensitiveWordService != null) {
            if (sensitiveWordService.containsSensitive(title) || sensitiveWordService.containsSensitive(desc)) {
                if (sensitiveWordService.mode() == SensitiveWordService.Mode.REJECT) {
                    throw new ValidationException("内容包含敏感词");
                }
                title = sensitiveWordService.mask(title);
                desc = sensitiveWordService.mask(desc);
            }
        }
        return lifeCatalogClient.createAgentItem(sellerUserId, campusId, title, desc, req.getPriceCent());
    }

    @Transactional(rollbackFor = Exception.class)
    public void offline(long sellerUserId, long itemId) {
        AgentItemDto it = lifeCatalogClient.getAgentItem(itemId);
        if (it == null) {
            throw new ValidationException("条目不存在");
        }
        if (!it.getSellerUserId().equals(sellerUserId)) {
            throw new ValidationException("无权操作");
        }
        lifeCatalogClient.offlineAgentItem(itemId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void adminOffline(long itemId) {
        AgentItemDto it = lifeCatalogClient.getAgentItem(itemId);
        if (it == null) {
            throw new ValidationException("条目不存在");
        }
        lifeCatalogClient.offlineAgentItem(itemId);
    }
}
