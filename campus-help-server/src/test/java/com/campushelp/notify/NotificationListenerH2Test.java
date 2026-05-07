package com.campushelp.notify;

import com.campushelp.CampusHelpApplication;
import com.campushelp.common.event.DomainEvent;
import com.campushelp.common.event.NotificationEventType;
import com.campushelp.common.security.JwtTokenProvider;
import com.campushelp.life.notify.mq.NotificationEventListener;
import com.campushelp.order.mapper.ChMessageMapper;
import com.campushelp.order.mapper.ChMessageRecipientMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        classes = CampusHelpApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "campus.jwt.secret=test-secret-please-use-longer-in-prod",
                "campus.ratelimit.enabled=false",
                "spring.autoconfigure.exclude=org.redisson.spring.starter.RedissonAutoConfiguration",
                "spring.rabbitmq.listener.simple.auto-startup=false",
                "spring.task.scheduling.enabled=false",
                "spring.datasource.url=jdbc:h2:mem:campus_help_test;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.sql.init.mode=never",
                // 避免测试环境尝试连接本地 Redis/Rabbit
                "spring.redis.host=127.0.0.1",
                "spring.redis.port=0",
                "spring.rabbitmq.host=127.0.0.1",
                "spring.rabbitmq.port=0"
        }
)
public class NotificationListenerH2Test {

    @LocalServerPort
    int port;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    NotificationEventListener notificationEventListener;

    @Autowired
    ChMessageMapper messageMapper;

    @Autowired
    ChMessageRecipientMapper recipientMapper;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void initTables() {
        jdbcTemplate.execute("DROP TABLE IF EXISTS ch_message_recipient");
        jdbcTemplate.execute("DROP TABLE IF EXISTS ch_message");
        jdbcTemplate.execute("DROP TABLE IF EXISTS ch_order");
        jdbcTemplate.execute("CREATE TABLE ch_message (" +
                "id BIGINT PRIMARY KEY," +
                "event_id VARCHAR(64) NOT NULL," +
                "type VARCHAR(64) NOT NULL," +
                "biz_id VARCHAR(64) NULL," +
                "title VARCHAR(128) NOT NULL," +
                "content VARCHAR(512) NOT NULL," +
                "payload_json TEXT NULL," +
                "created_at DATETIME NOT NULL" +
                ")");
        jdbcTemplate.execute("CREATE TABLE ch_message_recipient (" +
                "id BIGINT PRIMARY KEY," +
                "message_id BIGINT NOT NULL," +
                "user_id BIGINT NOT NULL," +
                "read_at DATETIME NULL," +
                "deleted_at DATETIME NULL," +
                "created_at DATETIME NOT NULL" +
                ")");
        // 最小占位：避免定时任务扫描时报表不存在
        jdbcTemplate.execute("CREATE TABLE ch_order (" +
                "id BIGINT PRIMARY KEY," +
                "status VARCHAR(32) NULL," +
                "pay_status VARCHAR(32) NULL," +
                "expire_at DATETIME NULL" +
                ")");
    }

    @Test
    void messagePersistIsIdempotent() {
        String eventId = UUID.randomUUID().toString();
        DomainEvent ev = new DomainEvent(
                eventId,
                NotificationEventType.ORDER_PAID,
                "123",
                Instant.now(),
                new Long[]{1L, 2L},
                Map.of("orderId", 123, "orderNo", "NO123", "status", "PAID")
        );

        notificationEventListener.handle(ev);
        notificationEventListener.handle(ev);

        assertEquals(1L, messageMapper.selectCount(null).longValue());
        assertEquals(2L, recipientMapper.selectCount(null).longValue());
    }

    @Test
    void websocketCanReceiveUserTopic() throws Exception {
        String token = jwtTokenProvider.createToken(7L, "ROLE_STUDENT");

        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        CompletableFuture<Map> got = new CompletableFuture<>();

        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        String wsUrl = "ws://localhost:" + port + "/ws?token=" + token;
        StompSession session = stompClient.connect(wsUrl, headers, new StompSessionHandlerAdapter() {})
                .get(5, TimeUnit.SECONDS);

        session.subscribe("/topic/user.7", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return Map.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                got.complete((Map) payload);
            }
        });
        Thread.sleep(300);

        DomainEvent ev = new DomainEvent(
                UUID.randomUUID().toString(),
                NotificationEventType.COMMENT_CREATED,
                "1",
                Instant.now(),
                new Long[]{7L},
                Map.of("commentId", 1, "content", "hi")
        );
        notificationEventListener.handle(ev);

        Map recv = got.get(10, TimeUnit.SECONDS);
        assertEquals("COMMENT_CREATED", String.valueOf(recv.get("type")));
    }
}

