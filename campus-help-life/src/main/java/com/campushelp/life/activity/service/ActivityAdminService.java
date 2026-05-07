package com.campushelp.life.activity.service;

import com.campushelp.common.exception.ValidationException;
import com.campushelp.life.activity.dto.AdminActivityCreateRequest;
import com.campushelp.life.activity.dto.AdminTicketTypeCreateRequest;
import com.campushelp.life.client.LifeCatalogClient;
import com.campushelp.life.client.dto.ActivityDto;
import com.campushelp.life.client.dto.TicketTypeDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;

@Service
public class ActivityAdminService {

    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private final LifeCatalogClient lifeCatalogClient;

    public ActivityAdminService(LifeCatalogClient lifeCatalogClient) {
        this.lifeCatalogClient = lifeCatalogClient;
    }

    @Transactional(rollbackFor = Exception.class)
    public ActivityDto createActivity(long adminUserId, AdminActivityCreateRequest req) {
        if (req.getStartTime().isAfter(req.getEndTime()) || req.getStartTime().isEqual(req.getEndTime())) {
            throw new ValidationException("开始时间必须早于结束时间");
        }
        String status = (req.getStatus() == null || req.getStatus().trim().isEmpty())
                ? "PUBLISHED"
                : req.getStatus().trim().toUpperCase();
        if (!"DRAFT".equals(status) && !"PUBLISHED".equals(status) && !"OFFLINE".equals(status) && !"ENDED".equals(status)) {
            throw new ValidationException("status 不合法");
        }
        return lifeCatalogClient.createActivity(
                req.getCampusId(),
                req.getTitle().trim(),
                req.getDescription(),
                req.getPlace(),
                req.getStartTime().format(DATETIME_FMT),
                req.getEndTime().format(DATETIME_FMT),
                status,
                adminUserId
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public TicketTypeDto createTicketType(long activityId, AdminTicketTypeCreateRequest req) {
        ActivityDto act = lifeCatalogClient.getActivity(activityId);
        if (act == null) {
            throw new ValidationException("活动不存在");
        }
        if (req.getSaleStartTime().isAfter(req.getSaleEndTime()) || req.getSaleStartTime().isEqual(req.getSaleEndTime())) {
            throw new ValidationException("售卖开始时间必须早于售卖结束时间");
        }
        String status = (req.getStatus() == null || req.getStatus().trim().isEmpty())
                ? "ON"
                : req.getStatus().trim().toUpperCase();
        if (!"ON".equals(status) && !"OFF".equals(status)) {
            throw new ValidationException("status 不合法");
        }
        if (req.getStockTotal() <= 0) {
            throw new ValidationException("stockTotal 须大于 0");
        }
        if (req.getPerUserLimit() <= 0) {
            throw new ValidationException("perUserLimit 须大于 0");
        }
        return lifeCatalogClient.createTicketType(
                activityId,
                req.getName().trim(),
                req.getPriceCent(),
                req.getStockTotal(),
                req.getPerUserLimit(),
                req.getSaleStartTime().format(DATETIME_FMT),
                req.getSaleEndTime().format(DATETIME_FMT),
                status
        );
    }
}

