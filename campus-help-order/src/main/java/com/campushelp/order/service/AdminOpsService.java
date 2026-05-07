package com.campushelp.order.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.campushelp.order.entity.ChOrder;
import com.campushelp.order.entity.ChSystemConfig;
import com.campushelp.order.mapper.ChOrderMapper;
import com.campushelp.order.mapper.ChSystemConfigMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminOpsService {

    private final ChSystemConfigMapper systemConfigMapper;
    private final ChOrderMapper orderMapper;

    public AdminOpsService(ChSystemConfigMapper systemConfigMapper, ChOrderMapper orderMapper) {
        this.systemConfigMapper = systemConfigMapper;
        this.orderMapper = orderMapper;
    }

    public List<ChSystemConfig> listConfigs() {
        return systemConfigMapper.selectList(new QueryWrapper<ChSystemConfig>().orderByAsc("config_key"));
    }

    @Transactional(rollbackFor = Exception.class)
    public ChSystemConfig upsertConfig(String key, String value, long adminUserId) {
        ChSystemConfig c = systemConfigMapper.selectOne(new QueryWrapper<ChSystemConfig>().eq("config_key", key));
        LocalDateTime now = LocalDateTime.now();
        if (c == null) {
            c = new ChSystemConfig();
            c.setConfigKey(key);
            c.setConfigValue(value);
            c.setUpdatedBy(adminUserId);
            c.setCreatedAt(now);
            c.setUpdatedAt(now);
            systemConfigMapper.insert(c);
            return c;
        }
        c.setConfigValue(value);
        c.setUpdatedBy(adminUserId);
        c.setUpdatedAt(now);
        systemConfigMapper.updateById(c);
        return c;
    }

    public Map<String, Object> dashboard() {
        Map<String, Object> m = new HashMap<>();
        long totalOrders = orderMapper.selectCount(new QueryWrapper<>());
        long paidOrders = orderMapper.selectCount(new QueryWrapper<ChOrder>().eq("pay_status", "PAID"));
        long completedOrders = orderMapper.selectCount(new QueryWrapper<ChOrder>().eq("status", "COMPLETED"));
        long refundingOrders = orderMapper.selectCount(new QueryWrapper<ChOrder>().eq("status", "REFUNDING"));
        long refundedOrders = orderMapper.selectCount(new QueryWrapper<ChOrder>().eq("status", "REFUNDED"));
        long gmvPaidCent = orderMapper.sumPaidAmountCent();
        long activeUsers7d = orderMapper.countActiveOrderUsers7d();
        long ticketOrders24h = orderMapper.countTicketOrders24h();
        m.put("totalOrders", totalOrders);
        m.put("paidOrders", paidOrders);
        m.put("completedOrders", completedOrders);
        m.put("refundingOrders", refundingOrders);
        m.put("refundedOrders", refundedOrders);
        m.put("gmvPaidCent", gmvPaidCent);
        m.put("activeOrderUsers7d", activeUsers7d);
        m.put("ticketOrders24h", ticketOrders24h);
        return m;
    }
}
