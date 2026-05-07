package com.campushelp.order.hooks;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.campushelp.common.api.ApiResult;
import com.campushelp.order.client.ProductServiceClient;
import com.campushelp.order.entity.ChOrder;
import com.campushelp.order.entity.ChOrderItem;
import com.campushelp.order.mapper.ChOrderItemMapper;
import com.campushelp.order.service.OrderService;
import com.campushelp.order.spi.OrderUnpaidClosedSideEffect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(20)
public class TakeoutStockOrderHooks implements OrderUnpaidClosedSideEffect {
    private final ChOrderItemMapper orderItemMapper;
    private final ProductServiceClient productServiceClient;

    public TakeoutStockOrderHooks(ChOrderItemMapper orderItemMapper, ProductServiceClient productServiceClient) {
        this.orderItemMapper = orderItemMapper;
        this.productServiceClient = productServiceClient;
    }

    @Override
    public void onOrderUnpaidClosed(ChOrder order) {
        if (!OrderService.ORDER_TYPE_TAKEOUT.equals(order.getOrderType())) return;
        for (ChOrderItem line : orderItemMapper.selectList(new QueryWrapper<ChOrderItem>().eq("order_id", order.getId()))) {
            if (!"SKU".equals(line.getItemType()) || line.getRefId() == null || line.getQuantity() == null) continue;
            ApiResult<Boolean> result = productServiceClient.restoreSkuStock(line.getRefId(), line.getQuantity());
            if (result == null || !result.isSuccess()) {
                // ignore; timeout-close retry and reconciliation can recover
            }
        }
    }
}
