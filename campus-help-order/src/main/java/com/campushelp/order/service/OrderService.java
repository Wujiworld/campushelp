package com.campushelp.order.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.campushelp.common.api.ApiResult;
import com.campushelp.order.dto.OrderCreateRequest;
import com.campushelp.order.dto.OrderItemRequest;
import com.campushelp.order.client.ProductServiceClient;
import com.campushelp.order.client.dto.ProductDTO;
import com.campushelp.order.client.dto.ProductSkuDTO;
import com.campushelp.order.client.dto.StoreDTO;
import com.campushelp.order.entity.ChActivity;
import com.campushelp.order.entity.ChActivityEnroll;
import com.campushelp.order.entity.ChPaymentNotify;
import com.campushelp.order.entity.ChOrder;
import com.campushelp.order.entity.ChOrderErrandExt;
import com.campushelp.order.entity.ChOrderItem;
import com.campushelp.order.entity.ChOrderSecondhandExt;
import com.campushelp.order.entity.ChOrderTicketExt;
import com.campushelp.order.entity.ChAgentItem;
import com.campushelp.order.entity.ChSecondhandItem;
import com.campushelp.order.entity.ChTicketType;
import com.campushelp.order.exception.BadRequestException;
import com.campushelp.order.exception.OrderNotFoundException;
import com.campushelp.order.mapper.ChActivityMapper;
import com.campushelp.order.mapper.ChActivityEnrollMapper;
import com.campushelp.order.mapper.ChAgentItemMapper;
import com.campushelp.order.mapper.ChPaymentNotifyMapper;
import com.campushelp.order.mapper.ChOrderErrandExtMapper;
import com.campushelp.order.mapper.ChOrderItemMapper;
import com.campushelp.order.mapper.ChOrderMapper;
import com.campushelp.order.mapper.ChOrderSecondhandExtMapper;
import com.campushelp.order.mapper.ChOrderTicketExtMapper;
import com.campushelp.order.mapper.ChSecondhandItemMapper;
import com.campushelp.order.mapper.ChTicketTypeMapper;
import com.campushelp.order.spi.OrderDelayNotifier;
import com.campushelp.order.spi.OrderPaidSideEffect;
import com.campushelp.order.spi.OrderUnpaidClosedSideEffect;
import com.campushelp.common.event.DomainEvent;
import com.campushelp.common.event.DomainEventPublisher;
import com.campushelp.common.event.NotificationEventType;
import cn.hutool.json.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    public static final String STATUS_CREATED = "CREATED";
    public static final String STATUS_PAID = "PAID";
    public static final String STATUS_CANCELLED = "CANCELLED";

    public static final String PAY_UNPAID = "UNPAID";
    public static final String PAY_PAID = "PAID";

    public static final String STATUS_MERCHANT_CONFIRMED = "MERCHANT_CONFIRMED";
    public static final String STATUS_RIDER_TAKEN = "RIDER_TAKEN";
    public static final String STATUS_DELIVERING = "DELIVERING";
    public static final String STATUS_COMPLETED = "COMPLETED";

    public static final String ORDER_TYPE_TAKEOUT = "TAKEOUT";
    public static final String ORDER_TYPE_ERRAND = "ERRAND";
    public static final String ORDER_TYPE_SECONDHAND = "SECONDHAND";
    public static final String ORDER_TYPE_TICKET = "TICKET";

    public static final String SECONDHAND_ITEM_ON_SALE = "ON_SALE";
    public static final String SECONDHAND_ITEM_PENDING_PAY = "PENDING_PAY";
    public static final String SECONDHAND_ITEM_SOLD = "SOLD";

    public static final String DELIVERY_MEETUP = "MEETUP";
    public static final String DELIVERY_DELIVERY = "DELIVERY";

    private final ChOrderMapper orderMapper;
    private final ChOrderItemMapper orderItemMapper;
    private final ProductServiceClient productServiceClient;
    private final ChPaymentNotifyMapper paymentNotifyMapper;
    private final ChSecondhandItemMapper secondhandItemMapper;
    private final ChOrderSecondhandExtMapper orderSecondhandExtMapper;
    private final ChOrderErrandExtMapper orderErrandExtMapper;
    private final ChOrderTicketExtMapper orderTicketExtMapper;
    private final ChAgentItemMapper agentItemMapper;
    private final ChTicketTypeMapper ticketTypeMapper;
    private final ChActivityMapper activityMapper;
    private final ChActivityEnrollMapper activityEnrollMapper;

    private final List<OrderPaidSideEffect> paidSideEffects;
    private final List<OrderUnpaidClosedSideEffect> unpaidClosedSideEffects;
    private final List<OrderDelayNotifier> orderDelayNotifiers;
    private final DomainEventPublisher domainEventPublisher;
    private final TradeOpsService tradeOpsService;

    @Value("${campus.order.pay-timeout-minutes:15}")
    private int payTimeoutMinutes;

    @Value("${campus.payment.callback-lock-ttl-seconds:0}")
    private int payCallbackLockTtlSeconds;

    private final StringRedisTemplate stringRedisTemplate;

    public OrderService(ChOrderMapper orderMapper,
                        ChOrderItemMapper orderItemMapper,
                        ProductServiceClient productServiceClient,
                        ChPaymentNotifyMapper paymentNotifyMapper,
                        ChSecondhandItemMapper secondhandItemMapper,
                        ChOrderSecondhandExtMapper orderSecondhandExtMapper,
                        ChOrderErrandExtMapper orderErrandExtMapper,
                        ChOrderTicketExtMapper orderTicketExtMapper,
                        ChAgentItemMapper agentItemMapper,
                        ChTicketTypeMapper ticketTypeMapper,
                        ChActivityMapper activityMapper,
                        ChActivityEnrollMapper activityEnrollMapper,
                        List<OrderPaidSideEffect> paidSideEffects,
                        List<OrderUnpaidClosedSideEffect> unpaidClosedSideEffects,
                        @Autowired(required = false) List<OrderDelayNotifier> orderDelayNotifiers,
                        @Autowired(required = false) DomainEventPublisher domainEventPublisher,
                        @Autowired(required = false) StringRedisTemplate stringRedisTemplate,
                        @Autowired(required = false) TradeOpsService tradeOpsService) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.productServiceClient = productServiceClient;
        this.paymentNotifyMapper = paymentNotifyMapper;
        this.secondhandItemMapper = secondhandItemMapper;
        this.orderSecondhandExtMapper = orderSecondhandExtMapper;
        this.orderErrandExtMapper = orderErrandExtMapper;
        this.orderTicketExtMapper = orderTicketExtMapper;
        this.agentItemMapper = agentItemMapper;
        this.ticketTypeMapper = ticketTypeMapper;
        this.activityMapper = activityMapper;
        this.activityEnrollMapper = activityEnrollMapper;
        this.paidSideEffects = paidSideEffects == null ? Collections.emptyList() : paidSideEffects;
        this.unpaidClosedSideEffects = unpaidClosedSideEffects == null ? Collections.emptyList() : unpaidClosedSideEffects;
        this.orderDelayNotifiers = orderDelayNotifiers == null ? Collections.emptyList() : orderDelayNotifiers;
        this.domainEventPublisher = domainEventPublisher;
        this.stringRedisTemplate = stringRedisTemplate;
        this.tradeOpsService = tradeOpsService;
    }

    @Transactional(rollbackFor = Exception.class)
    public ChOrder create(OrderCreateRequest req) {
        String type = req.getOrderType() == null ? "" : req.getOrderType().trim().toUpperCase();
        if (ORDER_TYPE_TAKEOUT.equals(type)) {
            return createTakeout(req);
        }
        throw new BadRequestException("不支持的订单类型，请使用业务接口创建：ERRAND / SECONDHAND / TICKET");
    }

    private ChOrder createTakeout(OrderCreateRequest req) {
        LocalDateTime now = LocalDateTime.now();
        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new BadRequestException("订单至少需要 1 个明细项");
        }
        if (req.getStoreId() == null) {
            throw new BadRequestException("外卖订单需要门店");
        }
        StoreDTO store = requireData(productServiceClient.getStore(req.getStoreId()), "门店不可下单");
        if (store == null || store.getStatus() == null || store.getStatus() != 1) {
            throw new BadRequestException("门店不可下单");
        }
        if (!store.getCampusId().equals(req.getCampusId())) {
            throw new BadRequestException("门店与校区不匹配");
        }

        int itemsAmount = 0;
        for (OrderItemRequest it : req.getItems()) {
            itemsAmount += (it.getUnitPriceCent() * it.getQuantity());
            deductTakeoutSkuStock(req.getStoreId(), it);
        }
        int totalAmountCent = itemsAmount + req.getDeliveryFeeCent();

        ChOrder o = newBaseOrder(req, ORDER_TYPE_TAKEOUT, now, totalAmountCent);
        o.setStoreId(req.getStoreId());
        o.setAddressId(req.getAddressId());
        o.setMerchantUserId(store.getMerchantUserId());
        orderMapper.insert(o);

        for (OrderItemRequest it : req.getItems()) {
            insertSkuLine(o.getId(), it, now);
        }
        notifyUnpaidOrderCreated(o);
        publishOrderEvent(NotificationEventType.ORDER_CREATED, o, null);
        return o;
    }

    private void notifyUnpaidOrderCreated(ChOrder o) {
        for (OrderDelayNotifier n : orderDelayNotifiers) {
            n.onUnpaidOrderCreated(o);
        }
    }

    private void deductTakeoutSkuStock(Long storeId, OrderItemRequest it) {
        ProductSkuDTO sku = requireData(productServiceClient.getSku(it.getSkuId()), "SKU 不可售");
        if (sku == null || sku.getStatus() == null || sku.getStatus() != 1) {
            throw new BadRequestException("SKU 不可售");
        }
        ProductDTO product = requireData(productServiceClient.getProduct(sku.getProductId()), "商品已下架");
        if (product == null || product.getStatus() == null || product.getStatus() != 1) {
            throw new BadRequestException("商品已下架");
        }
        if (!product.getStoreId().equals(storeId)) {
            throw new BadRequestException("SKU 不属于该门店");
        }
        if (sku.getPriceCent() == null || !sku.getPriceCent().equals(it.getUnitPriceCent())) {
            throw new BadRequestException("单价与当前售价不一致");
        }
        Boolean deducted = requireData(productServiceClient.deductSkuStock(sku.getId(), it.getQuantity()), "库存不足");
        if (!Boolean.TRUE.equals(deducted)) {
            throw new BadRequestException("库存不足");
        }
    }

    /**
     * 跑腿单：单条 SERVICE 明细表示赏金/服务费。
     */
    @Transactional(rollbackFor = Exception.class)
    public ChOrder createErrandOrder(long userId,
                                     long campusId,
                                     Long addressId,
                                     String errandType,
                                     String pickupAddress,
                                     String pickupCode,
                                     String listText,
                                     int feeCent,
                                     String remark) {
        LocalDateTime now = LocalDateTime.now();
        if (feeCent <= 0) {
            throw new BadRequestException("跑腿赏金须大于 0");
        }
        ChOrder o = newBaseOrderShell(userId, campusId, ORDER_TYPE_ERRAND, now, feeCent, remark);
        o.setStoreId(null);
        o.setAddressId(addressId);
        orderMapper.insert(o);

        ChOrderItem oi = new ChOrderItem();
        oi.setOrderId(o.getId());
        oi.setItemType("SERVICE");
        oi.setRefId(null);
        oi.setTitle("跑腿服务费");
        oi.setUnitPriceCent(feeCent);
        oi.setQuantity(1);
        oi.setAmountCent(feeCent);
        oi.setSnapshotJson("{}");
        oi.setCreatedAt(now);
        orderItemMapper.insert(oi);

        ChOrderErrandExt ext = new ChOrderErrandExt();
        ext.setOrderId(o.getId());
        ext.setErrandType(errandType == null ? "OTHER" : errandType.trim().toUpperCase());
        ext.setPickupAddress(pickupAddress);
        ext.setPickupCode(pickupCode);
        ext.setListText(listText);
        ext.setCreatedAt(now);
        orderErrandExtMapper.insert(ext);
        notifyUnpaidOrderCreated(o);
        return o;
    }

    /**
     * 代购条目下单：本质是跑腿 BUY 单，但会写入一条 AGENT 明细，且未支付取消/超时可恢复条目上架。
     */
    @Transactional(rollbackFor = Exception.class)
    public ChOrder createAgentPurchase(long userId, long campusId, long agentItemId, long addressId, String remark) {
        ChAgentItem it = agentItemMapper.selectById(agentItemId);
        if (it == null) {
            throw new BadRequestException("代购条目不存在");
        }
        if (!campusIdEquals(it.getCampusId(), campusId)) {
            throw new BadRequestException("条目不属于该校区");
        }
        if (!"ON_SALE".equals(it.getStatus())) {
            throw new BadRequestException("条目不可下单");
        }
        if (it.getSellerUserId() != null && it.getSellerUserId().equals(userId)) {
            throw new BadRequestException("不能下单自己的条目");
        }
        if (addressId <= 0) {
            throw new BadRequestException("请选择配送地址");
        }

        // 先下架，避免并发重复下单（简化为 OFFLINE；取消/超时再恢复）
        agentItemMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<ChAgentItem>()
                .eq("id", agentItemId)
                .eq("status", "ON_SALE")
                .set("status", "OFFLINE")
                .set("updated_at", LocalDateTime.now()));

        LocalDateTime now = LocalDateTime.now();
        int feeCent = it.getPriceCent() == null ? 1 : it.getPriceCent();
        ChOrder o = newBaseOrderShell(userId, campusId, ORDER_TYPE_ERRAND, now, feeCent, remark);
        o.setStoreId(null);
        o.setAddressId(addressId);
        orderMapper.insert(o);

        ChOrderItem oi = new ChOrderItem();
        oi.setOrderId(o.getId());
        oi.setItemType("AGENT");
        oi.setRefId(agentItemId);
        oi.setTitle(it.getTitle());
        oi.setUnitPriceCent(feeCent);
        oi.setQuantity(1);
        oi.setAmountCent(feeCent);
        oi.setSnapshotJson(JSONUtil.toJsonStr(it));
        oi.setCreatedAt(now);
        orderItemMapper.insert(oi);

        ChOrderErrandExt ext = new ChOrderErrandExt();
        ext.setOrderId(o.getId());
        ext.setErrandType("BUY");
        ext.setPickupAddress(null);
        ext.setPickupCode(null);
        ext.setListText(it.getTitle());
        ext.setCreatedAt(now);
        orderErrandExtMapper.insert(ext);

        notifyUnpaidOrderCreated(o);
        publishOrderEvent(NotificationEventType.ORDER_CREATED, o, java.util.Map.of("agentItemId", agentItemId));
        return o;
    }

    private static boolean campusIdEquals(Long a, long b) {
        return a != null && a.longValue() == b;
    }

    @Transactional(rollbackFor = Exception.class)
    public ChOrder createSecondhandPurchase(long userId,
                                            long campusId,
                                            long itemId,
                                            String deliveryMode,
                                            Long addressId) {
        String mode = deliveryMode == null ? DELIVERY_MEETUP : deliveryMode.trim().toUpperCase();
        if (!DELIVERY_MEETUP.equals(mode) && !DELIVERY_DELIVERY.equals(mode)) {
            throw new BadRequestException("deliveryMode 须为 MEETUP 或 DELIVERY");
        }
        if (DELIVERY_DELIVERY.equals(mode) && addressId == null) {
            throw new BadRequestException("配送需填写收货地址");
        }

        ChSecondhandItem item = secondhandItemMapper.selectById(itemId);
        if (item == null) {
            throw new BadRequestException("商品不存在");
        }
        if (!item.getCampusId().equals(campusId)) {
            throw new BadRequestException("商品不属于该校区");
        }
        if (!SECONDHAND_ITEM_ON_SALE.equals(item.getStatus())) {
            throw new BadRequestException("商品不可购买");
        }
        if (item.getSellerUserId().equals(userId)) {
            throw new BadRequestException("不能购买自己的商品");
        }

        UpdateWrapper<ChSecondhandItem> lock = new UpdateWrapper<>();
        lock.eq("id", itemId).eq("status", SECONDHAND_ITEM_ON_SALE).set("status", SECONDHAND_ITEM_PENDING_PAY).set("updated_at", LocalDateTime.now());
        if (secondhandItemMapper.update(null, lock) != 1) {
            throw new BadRequestException("商品已被他人锁定或已售出，请刷新");
        }

        LocalDateTime now = LocalDateTime.now();
        int total = item.getPriceCent();
        ChOrder o = newBaseOrderShell(userId, campusId, ORDER_TYPE_SECONDHAND, now, total, null);
        o.setStoreId(null);
        o.setAddressId(addressId);
        o.setDeliveryFeeCent(0);
        orderMapper.insert(o);

        ChOrderItem oi = new ChOrderItem();
        oi.setOrderId(o.getId());
        oi.setItemType("SECONDHAND");
        oi.setRefId(itemId);
        oi.setTitle(item.getTitle());
        oi.setUnitPriceCent(item.getPriceCent());
        oi.setQuantity(1);
        oi.setAmountCent(item.getPriceCent());
        oi.setSnapshotJson(JSONUtil.toJsonStr(item));
        oi.setCreatedAt(now);
        orderItemMapper.insert(oi);

        ChOrderSecondhandExt ext = new ChOrderSecondhandExt();
        ext.setOrderId(o.getId());
        ext.setSecondhandItemId(itemId);
        ext.setDeliveryMode(mode);
        ext.setCreatedAt(now);
        orderSecondhandExtMapper.insert(ext);
        notifyUnpaidOrderCreated(o);
        publishOrderEvent(NotificationEventType.ORDER_CREATED, o, null);
        return o;
    }

    @Transactional(rollbackFor = Exception.class)
    public ChOrder createTicketOrder(long userId, long campusId, long ticketTypeId) {
        ChTicketType tt = ticketTypeMapper.selectById(ticketTypeId);
        if (tt == null || !"ON".equals(tt.getStatus())) {
            throw new BadRequestException("票种不可售");
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(tt.getSaleStartTime()) || now.isAfter(tt.getSaleEndTime())) {
            throw new BadRequestException("不在售票时间");
        }
        ChActivity act = activityMapper.selectById(tt.getActivityId());
        if (act == null || !"PUBLISHED".equals(act.getStatus())) {
            throw new BadRequestException("活动不可报名");
        }
        if (!act.getCampusId().equals(campusId)) {
            throw new BadRequestException("活动不属于该校区");
        }

        int limit = tt.getPerUserLimit() == null ? 1 : tt.getPerUserLimit();
        long enrolled = activityEnrollMapper.selectCount(
                new QueryWrapper<ChActivityEnroll>()
                        .eq("user_id", userId)
                        .eq("ticket_type_id", ticketTypeId)
                        .eq("status", "SUCCESS"));
        int pending = countPendingTicketOrders(userId, ticketTypeId);
        if (enrolled + pending >= limit) {
            throw new BadRequestException("超过单用户限购");
        }

        if (ticketTypeMapper.tryReserveStock(ticketTypeId, 1) != 1) {
            throw new BadRequestException("库存不足");
        }

        int total = tt.getPriceCent();
        ChOrder o = newBaseOrderShell(userId, campusId, ORDER_TYPE_TICKET, now, total, null);
        o.setStoreId(null);
        o.setAddressId(null);
        o.setDeliveryFeeCent(0);
        orderMapper.insert(o);

        ChOrderItem oi = new ChOrderItem();
        oi.setOrderId(o.getId());
        oi.setItemType("TICKET");
        oi.setRefId(ticketTypeId);
        oi.setTitle(act.getTitle() + " · " + tt.getName());
        oi.setUnitPriceCent(tt.getPriceCent());
        oi.setQuantity(1);
        oi.setAmountCent(tt.getPriceCent());
        oi.setSnapshotJson(JSONUtil.toJsonStr(tt));
        oi.setCreatedAt(now);
        orderItemMapper.insert(oi);

        ChOrderTicketExt ext = new ChOrderTicketExt();
        ext.setOrderId(o.getId());
        ext.setActivityId(act.getId());
        ext.setTicketTypeId(ticketTypeId);
        ext.setCreatedAt(now);
        orderTicketExtMapper.insert(ext);
        notifyUnpaidOrderCreated(o);
        publishOrderEvent(NotificationEventType.ORDER_CREATED, o, null);
        return o;
    }

    private int countPendingTicketOrders(long userId, long ticketTypeId) {
        List<ChOrderTicketExt> exts = orderTicketExtMapper.selectList(
                new QueryWrapper<ChOrderTicketExt>().eq("ticket_type_id", ticketTypeId));
        int n = 0;
        for (ChOrderTicketExt ext : exts) {
            ChOrder o = orderMapper.selectById(ext.getOrderId());
            if (o != null
                    && o.getUserId().equals(userId)
                    && ORDER_TYPE_TICKET.equals(o.getOrderType())
                    && STATUS_CREATED.equals(o.getStatus())
                    && PAY_UNPAID.equals(o.getPayStatus())) {
                n++;
            }
        }
        return n;
    }

    private ChOrder newBaseOrder(OrderCreateRequest req, String orderType, LocalDateTime now, int totalAmountCent) {
        ChOrder o = new ChOrder();
        o.setOrderNo(OrderNoGenerator.next());
        o.setOrderType(orderType);
        o.setUserId(req.getUserId());
        o.setCampusId(req.getCampusId());
        o.setStatus(STATUS_CREATED);
        o.setPayStatus(PAY_UNPAID);
        o.setTotalAmountCent(totalAmountCent);
        o.setPayAmountCent(0);
        o.setDeliveryFeeCent(req.getDeliveryFeeCent());
        o.setRemark(req.getRemark());
        o.setExpireAt(now.plusMinutes(payTimeoutMinutes));
        o.setCreatedAt(now);
        o.setUpdatedAt(now);
        return o;
    }

    private ChOrder newBaseOrderShell(long userId, long campusId, String orderType, LocalDateTime now,
                                      int totalAmountCent, String remark) {
        ChOrder o = new ChOrder();
        o.setOrderNo(OrderNoGenerator.next());
        o.setOrderType(orderType);
        o.setUserId(userId);
        o.setCampusId(campusId);
        o.setStatus(STATUS_CREATED);
        o.setPayStatus(PAY_UNPAID);
        o.setTotalAmountCent(totalAmountCent);
        o.setPayAmountCent(0);
        o.setDeliveryFeeCent(0);
        o.setRemark(remark);
        o.setExpireAt(now.plusMinutes(payTimeoutMinutes));
        o.setCreatedAt(now);
        o.setUpdatedAt(now);
        return o;
    }

    private void insertSkuLine(Long orderId, OrderItemRequest it, LocalDateTime now) {
        ChOrderItem oi = new ChOrderItem();
        oi.setOrderId(orderId);
        oi.setItemType("SKU");
        oi.setRefId(it.getSkuId());
        oi.setTitle(it.getTitle());
        oi.setUnitPriceCent(it.getUnitPriceCent());
        oi.setQuantity(it.getQuantity());
        oi.setAmountCent(it.getUnitPriceCent() * it.getQuantity());
        oi.setSnapshotJson(JSONUtil.toJsonStr(it));
        oi.setCreatedAt(now);
        orderItemMapper.insert(oi);
    }

    public ChOrder getByIdOrThrow(Long id) {
        ChOrder o = orderMapper.selectById(id);
        if (o == null) {
            throw new OrderNotFoundException("订单不存在: " + id);
        }
        return o;
    }

    public ChOrder getByIdForUser(Long id, Long userId) {
        ChOrder o = getByIdOrThrow(id);
        if (!o.getUserId().equals(userId)) {
            throw new OrderNotFoundException("订单不存在");
        }
        return o;
    }

    public List<ChOrder> listMine(Long userId, int limit) {
        int safe = Math.min(Math.max(limit, 1), 100);
        return orderMapper.selectList(
                new QueryWrapper<ChOrder>()
                        .eq("user_id", userId)
                        .orderByDesc("created_at")
                        .last("LIMIT " + safe)
        );
    }

    public List<ChOrder> listMerchantPending(Long merchantUserId, int limit) {
        int safe = Math.min(Math.max(limit, 1), 100);
        List<StoreDTO> stores = requireData(productServiceClient.storesByMerchant(merchantUserId), "查询门店失败");
        if (stores.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> storeIds = stores.stream().map(StoreDTO::getId).collect(Collectors.toList());
        return orderMapper.selectList(
                new QueryWrapper<ChOrder>()
                        .in("store_id", storeIds)
                        .eq("order_type", ORDER_TYPE_TAKEOUT)
                        .eq("status", STATUS_PAID)
                        .orderByDesc("created_at")
                        .last("LIMIT " + safe)
        );
    }

    public List<ChOrder> listRiderPool(int limit) {
        int safe = Math.min(Math.max(limit, 1), 100);
        return orderMapper.selectRiderPool(safe);
    }

    @Transactional(rollbackFor = Exception.class)
    public ChOrder pay(Long orderId, Long operatorUserId) {
        ChOrder o = getByIdOrThrow(orderId);
        assertOrderPayableByUser(o, operatorUserId);
        applyPaySuccess(o, LocalDateTime.now());
        return getByIdOrThrow(orderId);
    }

    /**
     * 模拟支付回调：幂等键为 {@code payNo}；成功时与 {@link #pay(Long, Long)} 等价（不校验操作人）。
     */
    @Transactional(rollbackFor = Exception.class)
    public ChOrder confirmPaidFromMock(Long orderId, String payNo) {
        if (payNo == null || payNo.isBlank()) {
            throw new BadRequestException("payNo 不能为空");
        }
        String pn = payNo.trim();
        if (paymentNotifyMapper.selectCount(new QueryWrapper<ChPaymentNotify>().eq("pay_no", pn)) > 0) {
            return getByIdOrThrow(orderId);
        }
        tryAcquirePayCallbackLock(pn);
        ChOrder o = getByIdOrThrow(orderId);
        if (PAY_PAID.equals(o.getPayStatus())) {
            recordPaymentNotify(orderId, pn, "SUCCESS");
            return o;
        }
        assertOrderPayable(o);
        applyPaySuccess(o, LocalDateTime.now());
        recordPaymentNotify(orderId, pn, "SUCCESS");
        return getByIdOrThrow(orderId);
    }

    private void tryAcquirePayCallbackLock(String payNo) {
        if (payCallbackLockTtlSeconds <= 0 || stringRedisTemplate == null) {
            return;
        }
        Boolean ok = stringRedisTemplate.opsForValue()
                .setIfAbsent("pay:cb:" + payNo, "1", Duration.ofSeconds(payCallbackLockTtlSeconds));
        if (Boolean.FALSE.equals(ok)) {
            throw new BadRequestException("该支付单号正在处理，请稍后重试");
        }
    }

    private void recordPaymentNotify(long orderId, String payNo, String status) {
        ChPaymentNotify n = new ChPaymentNotify();
        n.setOrderId(orderId);
        n.setPayNo(payNo);
        n.setStatus(status);
        n.setCreatedAt(LocalDateTime.now());
        try {
            paymentNotifyMapper.insert(n);
        } catch (Exception ex) {
            // 并发重复 pay_no：幂等
            log.warn("Payment notify insert skipped, orderId={}, payNo={}, reason={}", orderId, payNo, ex.getMessage());
        }
    }

    private void assertOrderPayableByUser(ChOrder o, Long operatorUserId) {
        if (!o.getUserId().equals(operatorUserId)) {
            throw new BadRequestException("无权支付该订单");
        }
        assertOrderPayable(o);
    }

    private void assertOrderPayable(ChOrder o) {
        if (!STATUS_CREATED.equals(o.getStatus()) || !PAY_UNPAID.equals(o.getPayStatus())) {
            throw new BadRequestException("当前状态不可支付");
        }
        LocalDateTime now = LocalDateTime.now();
        if (o.getExpireAt() != null && now.isAfter(o.getExpireAt())) {
            throw new BadRequestException("订单已超时，请重新下单");
        }
    }

    /**
     * 支付成功：条件更新（CREATED+UNPAID）保证并发下只入账一次，再触发 SPI 与领域事件。
     */
    private void applyPaySuccess(ChOrder o, LocalDateTime now) {
        UpdateWrapper<ChOrder> payCas = new UpdateWrapper<>();
        payCas.eq("id", o.getId())
                .eq("status", STATUS_CREATED)
                .eq("pay_status", PAY_UNPAID);
        ChOrder paidPatch = new ChOrder();
        paidPatch.setPayStatus(PAY_PAID);
        paidPatch.setStatus(STATUS_PAID);
        paidPatch.setPayAmountCent(o.getTotalAmountCent());
        paidPatch.setPaidAt(now);
        paidPatch.setUpdatedAt(now);
        int updated = orderMapper.update(paidPatch, payCas);
        if (updated == 0) {
            ChOrder cur = orderMapper.selectById(o.getId());
            if (cur != null && PAY_PAID.equals(cur.getPayStatus())) {
                return;
            }
            throw new BadRequestException("当前状态不可支付");
        }

        ChOrder paid = getByIdOrThrow(o.getId());
        for (OrderPaidSideEffect effect : paidSideEffects) {
            effect.onOrderPaid(paid);
        }
        publishOrderEvent(NotificationEventType.ORDER_PAID, paid, null);

        if (ORDER_TYPE_TICKET.equals(paid.getOrderType())) {
            UpdateWrapper<ChOrder> completeCas = new UpdateWrapper<>();
            completeCas.eq("id", paid.getId())
                    .eq("order_type", ORDER_TYPE_TICKET)
                    .eq("status", STATUS_PAID)
                    .eq("pay_status", PAY_PAID);
            ChOrder donePatch = new ChOrder();
            donePatch.setStatus(STATUS_COMPLETED);
            donePatch.setCompletedAt(now);
            donePatch.setUpdatedAt(now);
            if (orderMapper.update(donePatch, completeCas) > 0) {
                ChOrder completed = getByIdOrThrow(paid.getId());
                publishOrderEvent(NotificationEventType.ORDER_COMPLETED, completed, null);
                if (tradeOpsService != null) {
                    tradeOpsService.createIncomeLedgerForCompletedOrder(completed);
                }
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public ChOrder cancel(Long orderId, Long operatorUserId) {
        ChOrder o = getByIdOrThrow(orderId);
        if (!o.getUserId().equals(operatorUserId)) {
            throw new BadRequestException("无权取消该订单");
        }
        if (!STATUS_CREATED.equals(o.getStatus()) || !PAY_UNPAID.equals(o.getPayStatus())) {
            throw new BadRequestException("当前状态不可取消");
        }
        LocalDateTime now = LocalDateTime.now();
        o.setStatus(STATUS_CANCELLED);
        o.setCancelledAt(now);
        o.setUpdatedAt(now);
        orderMapper.updateById(o);

        for (OrderUnpaidClosedSideEffect effect : unpaidClosedSideEffects) {
            effect.onOrderUnpaidClosed(o);
        }
        publishOrderEvent(NotificationEventType.ORDER_CANCELLED, o, null);
        return o;
    }

    @Transactional(rollbackFor = Exception.class)
    public ChOrder merchantConfirm(Long orderId, Long merchantUserId, Long operatorUserId) {
        ChOrder o = getByIdOrThrow(orderId);
        if (!ORDER_TYPE_TAKEOUT.equals(o.getOrderType())) {
            throw new BadRequestException("仅外卖订单需要商家确认");
        }
        if (o.getStoreId() == null) {
            throw new BadRequestException("订单无门店，无法商家确认");
        }
        if (!STATUS_PAID.equals(o.getStatus()) || !PAY_PAID.equals(o.getPayStatus())) {
            throw new BadRequestException("当前状态不可商家确认");
        }
        StoreDTO store = requireData(productServiceClient.getStore(o.getStoreId()), "门店不存在");
        if (store == null) {
            throw new BadRequestException("门店不存在");
        }
        if (!store.getMerchantUserId().equals(merchantUserId)) {
            throw new BadRequestException("非本店订单");
        }
        if (o.getMerchantUserId() == null) {
            o.setMerchantUserId(merchantUserId);
        } else if (!o.getMerchantUserId().equals(merchantUserId)) {
            throw new BadRequestException("非本商家订单");
        }
        o.setStatus(STATUS_MERCHANT_CONFIRMED);
        o.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(o);
        publishOrderEvent(NotificationEventType.ORDER_MERCHANT_CONFIRMED, o, null);
        return o;
    }

    @Transactional(rollbackFor = Exception.class)
    public ChOrder riderTake(Long orderId, Long riderUserId, Long operatorUserId) {
        ChOrder o = getByIdOrThrow(orderId);
        if (o.getRiderUserId() != null && !o.getRiderUserId().equals(riderUserId)) {
            throw new BadRequestException("该订单已被其他骑手接单");
        }

        boolean okTakeout = ORDER_TYPE_TAKEOUT.equals(o.getOrderType())
                && STATUS_MERCHANT_CONFIRMED.equals(o.getStatus());
        boolean okErrand = ORDER_TYPE_ERRAND.equals(o.getOrderType()) && STATUS_PAID.equals(o.getStatus());
        boolean okShDelivery = false;
        if (ORDER_TYPE_SECONDHAND.equals(o.getOrderType()) && STATUS_PAID.equals(o.getStatus())) {
            ChOrderSecondhandExt ext = orderSecondhandExtMapper.selectOne(
                    new QueryWrapper<ChOrderSecondhandExt>().eq("order_id", orderId));
            okShDelivery = ext != null && DELIVERY_DELIVERY.equals(ext.getDeliveryMode());
        }

        if (!okTakeout && !okErrand && !okShDelivery) {
            throw new BadRequestException("当前状态不可骑手接单");
        }

        o.setRiderUserId(riderUserId);
        o.setStatus(STATUS_RIDER_TAKEN);
        o.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(o);
        publishOrderEvent(NotificationEventType.ORDER_RIDER_TAKEN, o, null);
        return o;
    }

    @Transactional(rollbackFor = Exception.class)
    public ChOrder riderPickup(Long orderId, Long riderUserId, Long operatorUserId) {
        ChOrder o = getByIdOrThrow(orderId);
        if (!STATUS_RIDER_TAKEN.equals(o.getStatus())) {
            throw new BadRequestException("当前状态不可开始配送");
        }
        if (o.getRiderUserId() == null || !o.getRiderUserId().equals(riderUserId)) {
            throw new BadRequestException("非本骑手订单");
        }
        o.setStatus(STATUS_DELIVERING);
        o.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(o);
        publishOrderEvent(NotificationEventType.ORDER_RIDER_PICKUP, o, null);
        return o;
    }

    @Transactional(rollbackFor = Exception.class)
    public ChOrder complete(Long orderId, Long operatorUserId) {
        ChOrder o = getByIdOrThrow(orderId);
        if (!o.getUserId().equals(operatorUserId)) {
            throw new BadRequestException("无权完成该订单");
        }
        LocalDateTime now = LocalDateTime.now();

        if (STATUS_DELIVERING.equals(o.getStatus())) {
            o.setStatus(STATUS_COMPLETED);
            o.setCompletedAt(now);
            o.setUpdatedAt(now);
            orderMapper.updateById(o);
            publishOrderEvent(NotificationEventType.ORDER_COMPLETED, o, null);
            if (tradeOpsService != null) {
                tradeOpsService.createIncomeLedgerForCompletedOrder(o);
            }
            return o;
        }

        if (ORDER_TYPE_SECONDHAND.equals(o.getOrderType())
                && STATUS_PAID.equals(o.getStatus())
                && PAY_PAID.equals(o.getPayStatus())) {
            ChOrderSecondhandExt ext = orderSecondhandExtMapper.selectOne(
                    new QueryWrapper<ChOrderSecondhandExt>().eq("order_id", orderId));
            if (ext != null && DELIVERY_MEETUP.equals(ext.getDeliveryMode())) {
                o.setStatus(STATUS_COMPLETED);
                o.setCompletedAt(now);
                o.setUpdatedAt(now);
                orderMapper.updateById(o);
                publishOrderEvent(NotificationEventType.ORDER_COMPLETED, o, null);
                if (tradeOpsService != null) {
                    tradeOpsService.createIncomeLedgerForCompletedOrder(o);
                }
                return o;
            }
        }

        throw new BadRequestException("当前状态不可完成");
    }

    @Transactional(rollbackFor = Exception.class)
    public int closeExpiredUnpaid() {
        LocalDateTime now = LocalDateTime.now();
        QueryWrapper<ChOrder> q = new QueryWrapper<>();
        q.eq("status", STATUS_CREATED)
                .eq("pay_status", PAY_UNPAID)
                .isNotNull("expire_at")
                .le("expire_at", now);
        List<ChOrder> list = orderMapper.selectList(q);
        int n = 0;
        for (ChOrder o : list) {
            if (tryCloseSingleUnpaidOrder(o.getId(), now)) {
                n++;
            }
        }
        return n;
    }

    /**
     * 延迟关单 / 管理用：若订单仍为待支付则关单并触发库存回滚等钩子。
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean tryCloseUnpaidOrder(long orderId) {
        return tryCloseSingleUnpaidOrder(orderId, LocalDateTime.now());
    }

    private boolean tryCloseSingleUnpaidOrder(long orderId, LocalDateTime now) {
        com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<ChOrder> u = new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<>();
        u.eq("id", orderId)
                .eq("status", STATUS_CREATED)
                .eq("pay_status", PAY_UNPAID);
        ChOrder update = new ChOrder();
        update.setStatus(STATUS_CANCELLED);
        update.setCancelledAt(now);
        update.setUpdatedAt(now);
        if (orderMapper.update(update, u) <= 0) {
            return false;
        }
        ChOrder closed = getByIdOrThrow(orderId);
        for (OrderUnpaidClosedSideEffect effect : unpaidClosedSideEffects) {
            effect.onOrderUnpaidClosed(closed);
        }
        publishOrderEvent(NotificationEventType.ORDER_UNPAID_CLOSED, closed, null);
        return true;
    }

    private void publishOrderEvent(NotificationEventType type, ChOrder o, Map<String, Object> extraPayload) {
        if (domainEventPublisher == null || o == null) {
            return;
        }
        Long[] recipients = recipientsForOrder(o);
        Map<String, Object> payload = new HashMap<>();
        payload.put("orderId", o.getId());
        payload.put("orderNo", o.getOrderNo());
        payload.put("orderType", o.getOrderType());
        payload.put("status", o.getStatus());
        payload.put("payStatus", o.getPayStatus());
        payload.put("campusId", o.getCampusId());
        payload.put("storeId", o.getStoreId());
        payload.put("userId", o.getUserId());
        payload.put("merchantUserId", o.getMerchantUserId());
        payload.put("riderUserId", o.getRiderUserId());
        payload.put("totalAmountCent", o.getTotalAmountCent());
        if (extraPayload != null) {
            payload.putAll(extraPayload);
        }
        DomainEvent ev = new DomainEvent(
                UUID.randomUUID().toString(),
                type,
                String.valueOf(o.getId()),
                Instant.now(),
                recipients,
                payload
        );
        domainEventPublisher.publishAfterCommit(ev);
    }

    private static Long[] recipientsForOrder(ChOrder o) {
        // 去重与空值剔除，保持简单（本期不做“通知骑手池”之类群发）
        java.util.LinkedHashSet<Long> set = new java.util.LinkedHashSet<>();
        if (o.getUserId() != null) set.add(o.getUserId());
        if (o.getMerchantUserId() != null) set.add(o.getMerchantUserId());
        if (o.getRiderUserId() != null) set.add(o.getRiderUserId());
        return set.toArray(new Long[0]);
    }

    private static <T> T requireData(ApiResult<T> result, String fallbackMessage) {
        if (result == null || !result.isSuccess()) {
            throw new BadRequestException(fallbackMessage);
        }
        return result.getData();
    }
}
