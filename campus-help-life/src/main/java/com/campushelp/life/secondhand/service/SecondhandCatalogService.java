package com.campushelp.life.secondhand.service;

import com.campushelp.common.exception.ValidationException;
import com.campushelp.life.client.LifeCatalogClient;
import com.campushelp.life.client.dto.SecondhandImageDto;
import com.campushelp.life.client.dto.SecondhandItemDto;
import com.campushelp.life.secondhand.dto.SecondhandItemCreateRequest;
import com.campushelp.common.safety.SensitiveWordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SecondhandCatalogService {

    private final LifeCatalogClient lifeCatalogClient;
    private final SensitiveWordService sensitiveWordService;

    public SecondhandCatalogService(LifeCatalogClient lifeCatalogClient,
                                    SensitiveWordService sensitiveWordService) {
        this.lifeCatalogClient = lifeCatalogClient;
        this.sensitiveWordService = sensitiveWordService;
    }

    public List<SecondhandItemDto> list(Long campusId,
                                        String status,
                                        String keyword,
                                        Integer minPriceCent,
                                        Integer maxPriceCent,
                                        String sort) {
        return lifeCatalogClient.listSecondhandItems(campusId, status, keyword, minPriceCent, maxPriceCent, sort);
    }

    public List<SecondhandItemDto> list(Long campusId, String status) {
        return list(campusId, status, null, null, null, "NEWEST");
    }

    public SecondhandItemDto getById(Long id) {
        return lifeCatalogClient.getSecondhandItem(id);
    }

    public List<SecondhandImageDto> listImages(Long itemId) {
        return lifeCatalogClient.listSecondhandImages(itemId);
    }

    @Transactional(rollbackFor = Exception.class)
    public SecondhandItemDto publish(long sellerUserId, long campusId, SecondhandItemCreateRequest req) {
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
        SecondhandItemDto item = lifeCatalogClient.createSecondhandItem(
                sellerUserId,
                campusId,
                title,
                desc,
                req.getPriceCent(),
                Boolean.FALSE.equals(req.getNegotiable()) ? 0 : 1);

        if (req.getImageUrls() != null) {
            int i = 0;
            for (String url : req.getImageUrls()) {
                if (url == null || url.isBlank()) {
                    continue;
                }
                lifeCatalogClient.addSecondhandImage(item.getId(), url.trim(), i++);
            }
        }
        return item;
    }

    @Transactional(rollbackFor = Exception.class)
    public void offline(long sellerUserId, long itemId) {
        SecondhandItemDto item = lifeCatalogClient.getSecondhandItem(itemId);
        if (item == null) {
            throw new ValidationException("商品不存在");
        }
        if (!item.getSellerUserId().equals(sellerUserId)) {
            throw new ValidationException("无权操作");
        }
        if (!"ON_SALE".equals(item.getStatus()) && !"PENDING_PAY".equals(item.getStatus())) {
            throw new ValidationException("当前状态不可下架");
        }
        if ("PENDING_PAY".equals(item.getStatus())) {
            throw new ValidationException("订单待支付中，暂不可下架");
        }
        lifeCatalogClient.offlineSecondhand(itemId);
    }

    public List<String> imageUrlsOf(Long itemId) {
        return listImages(itemId).stream().map(SecondhandImageDto::getUrl).collect(Collectors.toList());
    }

    public List<SecondhandItemDto> listBySeller(long sellerUserId) {
        return lifeCatalogClient.listSecondhandBySeller(sellerUserId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void recordView(long itemId) {
        lifeCatalogClient.incSecondhandView(itemId);
    }

    public Map<String, Object> toBriefMap(SecondhandItemDto it) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", it.getId());
        m.put("campusId", it.getCampusId());
        m.put("title", it.getTitle());
        m.put("priceCent", it.getPriceCent());
        m.put("negotiable", it.getNegotiable());
        m.put("status", it.getStatus());
        m.put("createdAt", it.getCreatedAt());
        m.put("likeCount", it.getLikeCount() == null ? 0 : it.getLikeCount());
        m.put("viewCount", it.getViewCount() == null ? 0 : it.getViewCount());
        List<String> urls = imageUrlsOf(it.getId());
        m.put("coverUrl", urls.isEmpty() ? null : urls.get(0));
        return m;
    }
}
