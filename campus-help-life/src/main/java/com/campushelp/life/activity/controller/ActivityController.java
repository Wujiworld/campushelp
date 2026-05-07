package com.campushelp.life.activity.controller;

import com.campushelp.common.security.SecurityContextUtils;
import com.campushelp.life.activity.dto.ActivityTicketOrderRequest;
import com.campushelp.life.activity.service.ActivityQueryService;
import com.campushelp.life.client.OrderServiceClient;
import com.campushelp.life.client.dto.ActivityDto;
import com.campushelp.life.client.dto.OrderSummaryDto;
import com.campushelp.life.client.dto.TicketTypeDto;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@Validated
@RestController
public class ActivityController {

    private final ActivityQueryService activityQueryService;
    private final OrderServiceClient orderServiceClient;

    public ActivityController(ActivityQueryService activityQueryService, OrderServiceClient orderServiceClient) {
        this.activityQueryService = activityQueryService;
        this.orderServiceClient = orderServiceClient;
    }

    @GetMapping("/api/v3/activities")
    public List<ActivityDto> list(@RequestParam(required = false) Long campusId) {
        return activityQueryService.listPublished(campusId);
    }

    @GetMapping("/api/v3/activities/{id}")
    public ActivityDto detail(@PathVariable Long id) {
        return activityQueryService.getPublished(id);
    }

    @GetMapping("/api/v3/activities/{id}/tickets")
    public List<TicketTypeDto> tickets(@PathVariable Long id) {
        return activityQueryService.listTickets(id);
    }

    @PostMapping("/api/v3/activities/orders")
    public OrderSummaryDto orderTicket(@Valid @RequestBody ActivityTicketOrderRequest req) {
        Long uid = SecurityContextUtils.requireUserId();
        return orderServiceClient.createTicketOrder(uid, req.getCampusId(), req.getTicketTypeId());
    }
}
