package com.campushelp.product.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.campushelp.common.cache.CacheFacade;
import com.campushelp.product.entity.ChCampus;
import com.campushelp.product.mapper.ChCampusMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 校区列表（公开读，供首页选择校区）。
 */
@RestController
public class CampusController {

    private final ChCampusMapper campusMapper;
    private final CacheFacade cacheFacade;
    private final ObjectMapper objectMapper;

    public CampusController(ChCampusMapper campusMapper, CacheFacade cacheFacade, ObjectMapper objectMapper) {
        this.campusMapper = campusMapper;
        this.cacheFacade = cacheFacade;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/api/v3/campuses")
    public List<ChCampus> campuses() {
        String json = cacheFacade.getOrLoadJson("campuses", () -> {
            List<ChCampus> list = campusMapper.selectList(
                    new QueryWrapper<ChCampus>().eq("status", 1).orderByAsc("id"));
            try {
                return objectMapper.writeValueAsString(list);
            } catch (Exception e) {
                throw new IllegalStateException("serialize campuses failed", e);
            }
        });
        if (json == null) return List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<List<ChCampus>>() {});
        } catch (Exception e) {
            throw new IllegalStateException("deserialize campuses failed", e);
        }
    }
}
