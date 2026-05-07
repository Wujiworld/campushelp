package com.campushelp.life.seckill.runner;

import com.campushelp.life.client.LifeCatalogClient;
import com.campushelp.life.client.dto.TicketTypeDto;
import com.campushelp.life.seckill.service.SeckillTicketService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(50)
@ConditionalOnBean(SeckillTicketService.class)
public class SeckillStockWarmupRunner implements ApplicationRunner {

    private final LifeCatalogClient lifeCatalogClient;
    private final SeckillTicketService seckillTicketService;

    public SeckillStockWarmupRunner(LifeCatalogClient lifeCatalogClient, SeckillTicketService seckillTicketService) {
        this.lifeCatalogClient = lifeCatalogClient;
        this.seckillTicketService = seckillTicketService;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<TicketTypeDto> list = lifeCatalogClient.listTicketsByStatus("ON", 500);
        for (TicketTypeDto tt : list) {
            seckillTicketService.warmupStock(tt.getId());
        }
    }
}
