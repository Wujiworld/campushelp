package com.campushelp.common.safety;

import com.campushelp.common.api.ApiResult;
import com.campushelp.common.api.ResultCode;
import com.campushelp.common.web.RequestIdFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Redis + Lua 限流：支持<strong>固定窗口</strong>与<strong>滑动窗口</strong>（ZSET 按时间戳计次）。
 * <p>
 * 配置：{@code campus.ratelimit.window-mode=fixed|sliding}，默认 {@code sliding}。
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
@Component
@ConditionalOnProperty(name = "campus.ratelimit.enabled", havingValue = "true", matchIfMissing = true)
public class RateLimitFilter extends OncePerRequestFilter {

    public enum WindowMode {
        fixed,
        sliding
    }

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    private final DefaultRedisScript<Long> fixedWindowScript;
    private final DefaultRedisScript<Long> slidingWindowScript;

    /**
     * 非 Spring 单测场景下默认为 sliding；Spring 中会由 {@link Value} 覆盖。
     */
    @Value("${campus.ratelimit.window-mode:sliding}")
    private WindowMode windowMode = WindowMode.sliding;

    /** 单测用：切换窗口算法 */
    void setWindowModeForTest(WindowMode mode) {
        this.windowMode = mode;
    }

    public RateLimitFilter(StringRedisTemplate redis, ObjectMapper objectMapper) {
        this.redis = redis;
        this.objectMapper = objectMapper;
        this.fixedWindowScript = new DefaultRedisScript<>();
        this.fixedWindowScript.setResultType(Long.class);
        this.fixedWindowScript.setScriptText(
                "local c = redis.call('INCR', KEYS[1]); "
                        + "if c == 1 then redis.call('EXPIRE', KEYS[1], ARGV[1]); end; "
                        + "return c;"
        );
        this.slidingWindowScript = new DefaultRedisScript<>();
        this.slidingWindowScript.setResultType(Long.class);
        this.slidingWindowScript.setScriptText(
                "local key = KEYS[1]\n"
                        + "local nowMs = tonumber(ARGV[1])\n"
                        + "local windowMs = tonumber(ARGV[2])\n"
                        + "local limit = tonumber(ARGV[3])\n"
                        + "local member = ARGV[4]\n"
                        + "redis.call('ZREMRANGEBYSCORE', key, '-inf', nowMs - windowMs)\n"
                        + "local n = redis.call('ZCARD', key)\n"
                        + "if n >= limit then return -1 end\n"
                        + "redis.call('ZADD', key, nowMs, member)\n"
                        + "redis.call('EXPIRE', key, math.ceil(windowMs / 1000) + 1)\n"
                        + "return n + 1"
        );
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String method = request.getMethod();
        String path = request.getRequestURI();

        Rule rule = ruleOf(method, path);
        if (rule != null) {
            String ip = ipOf(request);
            String key = "rl:ip:" + windowMode + ":" + rule.name + ":" + ip;
            Long cur = applyLimit(key, rule);
            if (cur == null) {
                filterChain.doFilter(request, response);
                return;
            }
            boolean over = windowMode == WindowMode.sliding ? cur < 0 : cur > rule.limit;
            if (over) {
                tooManyRequests(response, request, "请求过于频繁，请稍后再试");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private Long applyLimit(String key, Rule rule) {
        if (windowMode == WindowMode.fixed) {
            return redis.execute(fixedWindowScript, List.of(key), String.valueOf(rule.windowSeconds));
        }
        long nowMs = System.currentTimeMillis();
        long windowMs = rule.windowSeconds * 1000L;
        String member = UUID.randomUUID().toString();
        return redis.execute(slidingWindowScript, List.of(key),
                String.valueOf(nowMs), String.valueOf(windowMs), String.valueOf(rule.limit), member);
    }

    private void tooManyRequests(HttpServletResponse response, HttpServletRequest request, String msg) throws IOException {
        response.setStatus(429);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
        Object rid = request.getAttribute(RequestIdFilter.ATTR_REQUEST_ID);
        ApiResult<Void> body = ApiResult.fail(ResultCode.BIZ_RULE, msg, rid != null ? rid.toString() : null);
        objectMapper.writeValue(response.getWriter(), body);
    }

    private static String ipOf(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        String xr = request.getHeader("X-Real-IP");
        if (xr != null && !xr.isBlank()) {
            return xr.trim();
        }
        return request.getRemoteAddr();
    }

    private static Rule ruleOf(String method, String path) {
        if ("POST".equalsIgnoreCase(method)) {
            if (path.startsWith("/api/v3/auth/login")) return new Rule("login", 60, 20);
            if (path.startsWith("/api/v3/auth/register")) return new Rule("register", 60, 10);
            if (path.startsWith("/api/v3/comments")) return new Rule("comment", 60, 30);
            if (path.startsWith("/api/v3/orders")) return new Rule("order", 60, 30);
            if (path.startsWith("/api/v3/seckill/")) return new Rule("seckill", 3, 10);
        }
        return null;
    }

    private static class Rule {
        final String name;
        final int windowSeconds;
        final int limit;

        Rule(String name, int windowSeconds, int limit) {
            this.name = name;
            this.windowSeconds = windowSeconds;
            this.limit = limit;
        }
    }
}
