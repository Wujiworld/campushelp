package com.campushelp.life.profile.service;

import com.campushelp.life.client.OrderProfileClient;
import com.campushelp.life.profile.dto.ProfileStatsVO;
import org.springframework.stereotype.Service;

@Service
public class ProfileStatsService {

    private final OrderProfileClient orderProfileClient;

    public ProfileStatsService(OrderProfileClient orderProfileClient) {
        this.orderProfileClient = orderProfileClient;
    }

    public ProfileStatsVO stats(long userId) {
        return orderProfileClient.stats(userId);
    }
}
