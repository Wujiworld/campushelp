package com.campushelp.order.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;

/**
 * 业务订单号：前缀 + 时间戳 + 随机数，保证可读性与冲突概率可控。
 * 替代方案：雪花 ID 直接转字符串、Redis 自增序列、号段服务。
 */
public final class OrderNoGenerator {

    private OrderNoGenerator() {
    }

    public static String next() {
        return "CH" + DateUtil.format(DateUtil.date(), "yyyyMMddHHmmss") + RandomUtil.randomNumbers(6);
    }
}
