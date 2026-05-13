package com.campushelp.life.activity.service;

import com.campushelp.common.cache.CacheFacade;
import com.campushelp.common.exception.ValidationException;
import com.campushelp.life.client.LifeCatalogClient;
import com.campushelp.life.client.dto.ActivityDto;
import com.campushelp.life.client.dto.TicketTypeDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActivityQueryService {

    private final LifeCatalogClient lifeCatalogClient;
    private final CacheFacade cacheFacade;

    public ActivityQueryService(LifeCatalogClient lifeCatalogClient, CacheFacade cacheFacade) {
        this.lifeCatalogClient = lifeCatalogClient;
        this.cacheFacade = cacheFacade;
    }

    public List<ActivityDto> listPublished(Long campusId) {
        return lifeCatalogClient.listActivities(campusId, "PUBLISHED", 200);
    }

    public List<TicketTypeDto> listTickets(Long activityId) {
        return lifeCatalogClient.listTickets(activityId, "ON");
    }

    public ActivityDto getPublished(long id) {
        ActivityDto a = cacheFacade.getOrLoad("activity:" + id, ActivityDto.class,
                () -> lifeCatalogClient.getActivity(id));
        if (a == null || !"PUBLISHED".equals(a.getStatus())) {
            throw new ValidationException("活动不存在或未发布");
        }
        return a;
    }
}
