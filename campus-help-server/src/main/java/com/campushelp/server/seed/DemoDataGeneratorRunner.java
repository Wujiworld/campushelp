package com.campushelp.server.seed;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.campushelp.common.security.RoleEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 小规模演示数据生成器：用于本地/答辩快速制造“真实校园场景数据”。\n
 *
 * 使用：设置环境变量 CAMPUS_SEED_ENABLED=true（或配置 campus.seed.enabled=true）再启动 server。
 * 生成完成后会自动退出进程（避免每次启动重复生成）。
 */
@Component
public class DemoDataGeneratorRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataGeneratorRunner.class);

    private final JdbcTemplate jdbc;
    private final PasswordEncoder passwordEncoder;

    @Value("${campus.seed.enabled:false}")
    private boolean enabled;

    @Value("${campus.seed.scale:small}")
    private String scale;

    @Value("${campus.seed.reset:false}")
    private boolean reset;

    public DemoDataGeneratorRunner(JdbcTemplate jdbc, PasswordEncoder passwordEncoder) {
        this.jdbc = jdbc;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (!enabled) {
            return;
        }

        log.info("[seed] start generating demo data, scale={}", scale);

        // 基础依赖：校区必须存在（建议已执行 001/002/005）
        List<Long> campusIds = jdbc.queryForList("SELECT id FROM ch_campus WHERE status=1 ORDER BY id LIMIT 20", Long.class);
        if (campusIds.isEmpty()) {
            throw new IllegalStateException("ch_campus 为空，请先执行 001/005 种子脚本");
        }

        int userTotal = 200;
        int merchantCount = 20;
        int riderCount = 20;
        int storeCount = 20;
        int productsPerStore = 10;
        int skusPerProduct = 2;
        int secondhandCount = 500;
        int agentCount = 300;
        int takeoutOrders = 1000;

        String sc = scale == null ? "small" : scale.trim().toLowerCase();
        if ("medium".equals(sc)) {
            userTotal = 2000;
            merchantCount = 80;
            riderCount = 120;
            storeCount = 80;
            productsPerStore = 15;
            skusPerProduct = 3;
            secondhandCount = 2000;
            agentCount = 1500;
            takeoutOrders = 8000;
        } else if ("small".equals(sc)) {
            // defaults
        } else {
            log.warn("[seed] unknown scale={}, fallback to small", sc);
        }

        String pwdHash = passwordEncoder.encode("123456");
        LocalDateTime now = LocalDateTime.now();

        if (reset) {
            log.warn("[seed] reset=true, cleaning tables before seeding...");
            resetBusinessTables();
        }

        // 1) 生成用户
        List<Long> merchantUids = new ArrayList<>();
        List<Long> riderUids = new ArrayList<>();
        List<Long> studentUids = new ArrayList<>();
        long phoneBase = 13900010000L + RandomUtil.randomInt(0, 1000);
        List<Object[]> userRows = new ArrayList<>(userTotal);
        List<Object[]> addrRows = new ArrayList<>(userTotal);
        List<Object[]> roleRows = new ArrayList<>(userTotal);
        for (int i = 0; i < userTotal; i++) {
            long uid = nextId();
            long campusId = campusIds.get(i % campusIds.size());
            String phone = String.valueOf(phoneBase + i);
            String nickname = (i < merchantCount ? "商家" : i < merchantCount + riderCount ? "骑手" : "同学") + (i + 1);
            userRows.add(new Object[]{uid, phone, pwdHash, nickname, null, campusId, ts(now), ts(now)});
            if (i < merchantCount) {
                merchantUids.add(uid);
                roleRows.add(new Object[]{nextId(), uid, 1003L, ts(now)});
            } else if (i < merchantCount + riderCount) {
                riderUids.add(uid);
                roleRows.add(new Object[]{nextId(), uid, 1002L, ts(now)});
            } else {
                studentUids.add(uid);
                roleRows.add(new Object[]{nextId(), uid, 1001L, ts(now)});
            }

            // address
            long addrId = nextId();
            addrRows.add(new Object[]{
                    addrId, uid, campusId, null, nickname, phone,
                    "宿舍区 A-" + (RandomUtil.randomInt(1, 20)) + "-" + RandomUtil.randomInt(101, 699),
                    "宿舍", ts(now), ts(now)
            });
        }
        jdbc.batchUpdate(
                "INSERT INTO ch_user(id, phone, password_hash, nickname, avatar_url, status, campus_id, created_at, updated_at) " +
                        "VALUES(?,?,?,?,?,1,?,?,?)",
                userRows
        );
        jdbc.batchUpdate(
                "INSERT INTO ch_user_role(id, user_id, role_id, created_at) VALUES(?,?,?,?)",
                roleRows
        );
        jdbc.batchUpdate(
                "INSERT INTO ch_address(id, user_id, campus_id, building_id, contact_name, contact_phone, detail, label, is_default, created_at, updated_at) " +
                        "VALUES(?,?,?,?,?,?,?,?,1,?,?)",
                addrRows
        );

        // 2) 门店 / 商品 / SKU
        List<Long> storeIds = new ArrayList<>();
        List<Long> skuIds = new ArrayList<>();
        Map<Long, Integer> skuPrice = new HashMap<>();
        List<Object[]> storeRows = new ArrayList<>();
        List<Object[]> productRows = new ArrayList<>();
        List<Object[]> skuRows = new ArrayList<>();
        for (int i = 0; i < storeCount; i++) {
            long storeId = nextId();
            long merchantUid = merchantUids.get(i % merchantUids.size());
            long campusId = campusIds.get(i % campusIds.size());
            storeRows.add(new Object[]{storeId, merchantUid, campusId, "食堂档口-" + (i + 1), "欢迎光临", ts(now), ts(now)});
            storeIds.add(storeId);

            for (int p = 0; p < productsPerStore; p++) {
                long pid = nextId();
                String pname = randomFoodName() + " " + (p + 1);
                productRows.add(new Object[]{pid, storeId, pname, null, "主食", ts(now), ts(now)});
                for (int s = 0; s < skusPerProduct; s++) {
                    long skuId = nextId();
                    int price = RandomUtil.randomInt(800, 3500);
                    int stock = RandomUtil.randomInt(30, 300);
                    skuRows.add(new Object[]{skuId, pid, s == 0 ? "标准" : s == 1 ? "大份" : "加料", price, stock, ts(now), ts(now)});
                    skuIds.add(skuId);
                    skuPrice.put(skuId, price);
                }
            }
        }
        jdbc.batchUpdate(
                "INSERT INTO ch_store(id, merchant_user_id, campus_id, name, type, status, open_time, close_time, notice, created_at, updated_at) " +
                        "VALUES(?,?,?,?,1,1,'09:00','22:00',?, ?, ?)",
                storeRows
        );
        jdbc.batchUpdate(
                "INSERT INTO ch_product(id, store_id, name, cover_url, category, status, created_at, updated_at) " +
                        "VALUES(?,?,?,?,?,1,?,?)",
                productRows
        );
        jdbc.batchUpdate(
                "INSERT INTO ch_product_sku(id, product_id, sku_name, price_cent, stock, sold_count, status, created_at, updated_at) " +
                        "VALUES(?,?,?,?,?,0,1,?,?)",
                skuRows
        );

        // 3) 二手
        List<Object[]> shRows = new ArrayList<>(secondhandCount);
        List<Object[]> shImgRows = new ArrayList<>();
        for (int i = 0; i < secondhandCount; i++) {
            long itemId = nextId();
            long sellerUid = studentUids.get(i % studentUids.size());
            long campusId = campusIds.get(i % campusIds.size());
            int price = RandomUtil.randomInt(500, 50000);
            String title = randomSecondhandTitle();
            shRows.add(new Object[]{
                    itemId, sellerUid, campusId, title, "成色良好，支持当面/配送。", price, RandomUtil.randomInt(0, 2),
                    ts(now.minusDays(RandomUtil.randomInt(0, 30))), ts(now)
            });
            int imgN = RandomUtil.randomInt(0, 4);
            for (int k = 0; k < imgN; k++) {
                shImgRows.add(new Object[]{
                        nextId(), itemId, "https://picsum.photos/seed/sh" + itemId + "_" + k + "/600/600", k, ts(now)
                });
            }
        }
        jdbc.batchUpdate(
                "INSERT INTO ch_secondhand_item(id, seller_user_id, campus_id, title, description, price_cent, negotiable, status, created_at, updated_at) " +
                        "VALUES(?,?,?,?,?,?,?, 'ON_SALE', ?, ?)",
                shRows
        );
        if (!shImgRows.isEmpty()) {
            jdbc.batchUpdate(
                    "INSERT INTO ch_secondhand_image(id, item_id, url, sort_no, created_at) VALUES(?,?,?,?,?)",
                    shImgRows
            );
        }

        // 4) 代购条目（需要已执行 007）
        Integer hasAgent = jdbc.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name='ch_agent_item'",
                Integer.class
        );
        if (hasAgent != null && hasAgent > 0) {
            List<Object[]> agentRows = new ArrayList<>(agentCount);
            for (int i = 0; i < agentCount; i++) {
                long id = nextId();
                long sellerUid = (i % 2 == 0 ? studentUids : merchantUids).get(i % (i % 2 == 0 ? studentUids.size() : merchantUids.size()));
                long campusId = campusIds.get(i % campusIds.size());
                int price = RandomUtil.randomInt(300, 20000);
                agentRows.add(new Object[]{id, sellerUid, campusId, "代购-" + randomAgentTitle(), "跑腿代购，时效快。", price, ts(now), ts(now)});
            }
            jdbc.batchUpdate(
                    "INSERT INTO ch_agent_item(id, seller_user_id, campus_id, title, description, price_cent, status, created_at, updated_at) " +
                            "VALUES(?,?,?,?,?,?,'ON_SALE',?,?)",
                    agentRows
            );
        } else {
            log.warn("[seed] ch_agent_item not found; skip agent items (please execute 007 script).");
        }

        // 5) 外卖订单（CREATED/PAID 混合）+ 明细
        List<Object[]> orderRows = new ArrayList<>();
        List<Object[]> orderItemRows = new ArrayList<>();
        for (int i = 0; i < takeoutOrders; i++) {
            long oid = nextId();
            long buyerUid = studentUids.get(i % studentUids.size());
            long storeId = storeIds.get(i % storeIds.size());
            long campusId = campusIds.get(i % campusIds.size());
            Long merchantUid = merchantUids.get(i % merchantUids.size());
            Long addrId = null;
            String orderNo = "CH" + System.currentTimeMillis() + RandomUtil.randomInt(1000, 9999);

            int lines = RandomUtil.randomInt(1, 4);
            int itemsAmount = 0;
            List<Long> picked = new ArrayList<>();
            for (int k = 0; k < lines; k++) picked.add(skuIds.get(RandomUtil.randomInt(0, skuIds.size())));
            int deliveryFee = 300;
            boolean paid = (i % 3) != 0;
            String status = paid ? "PAID" : "CREATED";
            String payStatus = paid ? "PAID" : "UNPAID";

            for (Long skuId : picked) {
                int unit = skuPrice.getOrDefault(skuId, 1000);
                int qty = RandomUtil.randomInt(1, 3);
                int amt = unit * qty;
                itemsAmount += amt;
                orderItemRows.add(new Object[]{nextId(), oid, skuId, "SKU#" + skuId, unit, qty, amt, ts(now)});
            }
            int total = itemsAmount + deliveryFee;
            orderRows.add(new Object[]{
                    oid, orderNo, "TAKEOUT", buyerUid, storeId, merchantUid, null, campusId, addrId,
                    status, payStatus, total, paid ? total : 0, deliveryFee, "seed",
                    ts(now.plusMinutes(15)), paid ? ts(now) : null, null, null, ts(now), ts(now)
            });

            if (orderRows.size() >= 500) {
                flushOrders(orderRows, orderItemRows);
            }
        }
        flushOrders(orderRows, orderItemRows);

        log.info("[seed] done. users={}, merchants={}, riders={}, stores={}, skus~{}, secondhand={}, orders={}",
                userTotal, merchantCount, riderCount, storeCount, skuIds.size(), secondhandCount, takeoutOrders);

        // 防止反复启动重复生成：生成一次后自动退出
        System.exit(0);
    }

    private void flushOrders(List<Object[]> orderRows, List<Object[]> orderItemRows) {
        if (!orderRows.isEmpty()) {
            jdbc.batchUpdate(
                    "INSERT INTO ch_order(id, order_no, order_type, user_id, store_id, merchant_user_id, rider_user_id, campus_id, address_id, status, pay_status, total_amount_cent, pay_amount_cent, delivery_fee_cent, remark, expire_at, paid_at, cancelled_at, completed_at, created_at, updated_at) " +
                            "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                    orderRows
            );
            orderRows.clear();
        }
        if (!orderItemRows.isEmpty()) {
            jdbc.batchUpdate(
                    "INSERT INTO ch_order_item(id, order_id, item_type, ref_id, title, unit_price_cent, quantity, amount_cent, snapshot_json, created_at) " +
                            "VALUES(?,?, 'SKU', ?, ?, ?, ?, ?, NULL, ?)",
                    orderItemRows
            );
            orderItemRows.clear();
        }
    }

    private void resetBusinessTables() {
        // 不清理：ch_campus / ch_role 等基础字典。仅清理业务数据，顺序尽量从子表到主表。
        String[] sql = new String[]{
                "DELETE FROM ch_message_recipient",
                "DELETE FROM ch_message",
                "DELETE FROM ch_outbox",
                "DELETE FROM ch_payment_notify",
                "DELETE FROM ch_payment",
                "DELETE FROM ch_activity_enroll",
                "DELETE FROM ch_ticket_type",
                "DELETE FROM ch_activity",
                "DELETE FROM ch_order_ticket_ext",
                "DELETE FROM ch_order_secondhand_ext",
                "DELETE FROM ch_order_errand_ext",
                "DELETE FROM ch_order_item",
                "DELETE FROM ch_order",
                "DELETE FROM ch_secondhand_image",
                "DELETE FROM ch_secondhand_item",
                "DELETE FROM ch_agent_item",
                "DELETE FROM ch_product_sku",
                "DELETE FROM ch_product",
                "DELETE FROM ch_store",
                "DELETE FROM ch_address",
                "DELETE FROM ch_user_role",
                "DELETE FROM ch_user"
        };
        for (String s : sql) {
            try {
                jdbc.execute(s);
            } catch (Exception e) {
                log.warn("[seed] skip reset sql failed: {}, err={}", s, e.getMessage());
            }
        }
    }

    private static long nextId() {
        return IdUtil.getSnowflake(1, 1).nextId();
    }

    private static Timestamp ts(LocalDateTime t) {
        return t == null ? null : Timestamp.valueOf(t);
    }

    private static String inSql(int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            if (i > 0) sb.append(",");
            sb.append("?");
        }
        return sb.toString();
    }

    private static String randomFoodName() {
        String[] a = {"牛肉面", "鸡腿饭", "炒饭", "酸辣粉", "麻辣烫", "煎饼果子", "砂锅", "黄焖鸡", "馄饨", "奶茶"};
        return a[RandomUtil.randomInt(0, a.length)];
    }

    private static String randomSecondhandTitle() {
        String[] a = {"考研英语教材", "iPad 保护壳", "二手显示器", "机械键盘", "自行车", "蓝牙耳机", "台灯", "羽毛球拍", "宿舍小冰箱", "电煮锅"};
        return a[RandomUtil.randomInt(0, a.length)];
    }

    private static String randomAgentTitle() {
        String[] a = {"超市代购", "药店代买", "快递代取", "食堂代买", "打印跑腿", "外卖取送"};
        return a[RandomUtil.randomInt(0, a.length)];
    }
}

