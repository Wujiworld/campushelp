package com.campushelp.life.seckill.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.campushelp.common.security.SecurityContextUtils;
import com.campushelp.life.seckill.dto.SeckillTicketRequest;
import com.campushelp.life.seckill.service.SeckillTicketService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Map;

@RestController
@Validated
@ConditionalOnBean(SeckillTicketService.class)
public class SeckillTicketController {

    private final SeckillTicketService seckillTicketService;

    public SeckillTicketController(SeckillTicketService seckillTicketService) {
        this.seckillTicketService = seckillTicketService;
    }

    @PostMapping("/api/v3/seckill/ticket-orders")
    @SentinelResource(value = "seckillTicketSubmit", blockHandler = "submitBlocked")
    public ResponseEntity<Map<String, Object>> submit(@Valid @RequestBody SeckillTicketRequest req) {
        long uid = SecurityContextUtils.requireUserId();
        Map<String, Object> body = seckillTicketService.submit(uid, req.getCampusId(), req.getTicketTypeId());
        return ResponseEntity.accepted().body(body);
    }

    @SuppressWarnings("unused")
    public ResponseEntity<Map<String, Object>> submitBlocked(SeckillTicketRequest req, BlockException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(Map.of("message", "系统繁忙，请稍后重试"));
    }
}
