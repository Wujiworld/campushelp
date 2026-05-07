package com.campushelp.common.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 健康探测（负载均衡 / 运维探活）。
 */
@RestController
public class PingController {

    @GetMapping("/api/v3/ping")
    public Map<String, Object> ping() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("service", "campus-help");
        m.put("ok", true);
        return m;
    }
}
