package com.campushelp.product.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.campushelp.product.entity.ChCampus;
import com.campushelp.product.mapper.ChCampusMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 校区列表（公开读，供首页选择校区）。
 */
@RestController
public class CampusController {

    private final ChCampusMapper campusMapper;

    public CampusController(ChCampusMapper campusMapper) {
        this.campusMapper = campusMapper;
    }

    @GetMapping("/api/v3/campuses")
    public List<ChCampus> campuses() {
        return campusMapper.selectList(
                new QueryWrapper<ChCampus>().eq("status", 1).orderByAsc("id"));
    }
}
