package com.campushelp.common.safety;

import com.campushelp.common.api.ApiResult;
import com.campushelp.common.api.ResultCode;
import com.campushelp.common.web.RequestIdFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.PostConstruct;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.FilterChain;
import javax.servlet.ReadListener;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

/**
 * 敏感开放接口：HMAC-SHA256 + 时间戳窗口，防简单重放。
 * <p>
 * 头：X-Timestamp（秒）、X-Signature（hex）。签名原文：{@code ts + "\n" + method + "\n" + uri + "\n" + body}。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 5)
@ConditionalOnProperty(name = "campus.api-signature.enabled", havingValue = "true")
public class ApiSignatureFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    @Value("${campus.api-signature.secret:}")
    private String secret;

    @Value("${campus.api-signature.max-skew-seconds:300}")
    private long maxSkewSeconds;

    @Value("${campus.api-signature.path-prefixes:/api/v3/payments/mock-callback}")
    private String pathPrefixesRaw;

    private List<String> pathPrefixes = List.of("/api/v3/payments/mock-callback");

    public ApiSignatureFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void initPathPrefixes() {
        if (pathPrefixesRaw != null && !pathPrefixesRaw.isBlank()) {
            List<String> p = new ArrayList<>();
            for (String s : pathPrefixesRaw.split(",")) {
                String t = s.trim();
                if (!t.isEmpty()) {
                    p.add(t);
                }
            }
            if (!p.isEmpty()) {
                pathPrefixes = p;
            }
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!needsVerify(request) || secret == null || secret.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        String tsHeader = request.getHeader("X-Timestamp");
        String sign = request.getHeader("X-Signature");
        if (tsHeader == null || sign == null) {
            deny(response, request, "缺少签名校验头 X-Timestamp / X-Signature");
            return;
        }
        long now = System.currentTimeMillis() / 1000;
        long ts;
        try {
            ts = Long.parseLong(tsHeader.trim());
        } catch (NumberFormatException e) {
            deny(response, request, "时间戳无效");
            return;
        }
        if (Math.abs(now - ts) > maxSkewSeconds) {
            deny(response, request, "请求已过期");
            return;
        }

        byte[] body = StreamUtils.copyToByteArray(request.getInputStream());
        String uri = request.getRequestURI();
        String payload = tsHeader.trim() + "\n" + request.getMethod() + "\n" + uri + "\n"
                + new String(body, StandardCharsets.UTF_8);
        String expectHex = hmacSha256Hex(secret, payload);
        if (!constantTimeEquals(expectHex, sign.trim().toLowerCase())) {
            deny(response, request, "签名无效");
            return;
        }

        filterChain.doFilter(new BodyReplayHttpServletRequest(request, body), response);
    }

    private boolean needsVerify(HttpServletRequest request) {
        String uri = request.getRequestURI();
        for (String p : pathPrefixes) {
            if (uri.startsWith(p)) {
                return true;
            }
        }
        return false;
    }

    private void deny(HttpServletResponse response, HttpServletRequest request, String msg) throws IOException {
        response.setStatus(401);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
        Object rid = request.getAttribute(RequestIdFilter.ATTR_REQUEST_ID);
        objectMapper.writeValue(response.getWriter(), ApiResult.fail(ResultCode.UNAUTHORIZED, msg,
                rid != null ? rid.toString() : null));
    }

    private static String hmacSha256Hex(String secretKey, String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] raw = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(raw.length * 2);
            for (byte b : raw) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static boolean constantTimeEquals(String a, String b) {
        byte[] x = a.getBytes(StandardCharsets.UTF_8);
        byte[] y = b.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(x, y);
    }

    private static final class BodyReplayHttpServletRequest extends HttpServletRequestWrapper {

        private final byte[] body;

        BodyReplayHttpServletRequest(HttpServletRequest request, byte[] body) {
            super(request);
            this.body = body != null ? body : new byte[0];
        }

        @Override
        public ServletInputStream getInputStream() {
            ByteArrayInputStream source = new ByteArrayInputStream(body);
            return new ServletInputStream() {
                @Override
                public int read() {
                    return source.read();
                }

                @Override
                public boolean isFinished() {
                    return source.available() == 0;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(ReadListener readListener) {
                }
            };
        }
    }
}
