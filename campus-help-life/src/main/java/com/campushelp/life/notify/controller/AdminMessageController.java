package com.campushelp.life.notify.controller;

import com.campushelp.common.event.DomainEvent;
import com.campushelp.common.event.DomainEventPublisher;
import com.campushelp.common.event.NotificationEventType;
import com.campushelp.common.security.RequireRole;
import com.campushelp.common.security.RoleEnum;
import com.campushelp.life.notify.dto.SystemBroadcastRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Validated
@RestController
public class AdminMessageController {

    private final DomainEventPublisher domainEventPublisher;

    public AdminMessageController(@Autowired(required = false) DomainEventPublisher domainEventPublisher) {
        this.domainEventPublisher = domainEventPublisher;
    }

    @RequireRole(RoleEnum.ADMIN)
    @PostMapping("/api/v3/admin/messages/broadcast")
    public Map<String, Object> broadcast(@Valid @RequestBody SystemBroadcastRequest req) {
        if (domainEventPublisher == null) {
            throw new IllegalStateException("通知系统未启用");
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("title", req.getTitle());
        payload.put("content", req.getContent());
        if (req.getPayload() != null) {
            payload.putAll(req.getPayload());
        }
        String eventId = UUID.randomUUID().toString();
        DomainEvent ev = new DomainEvent(
                eventId,
                NotificationEventType.SYSTEM_ANNOUNCEMENT,
                "0",
                Instant.now(),
                new Long[0],
                payload
        );
        domainEventPublisher.publishAfterCommit(ev);
        Map<String, Object> resp = new HashMap<>();
        resp.put("eventId", eventId);
        return resp;
    }
}

