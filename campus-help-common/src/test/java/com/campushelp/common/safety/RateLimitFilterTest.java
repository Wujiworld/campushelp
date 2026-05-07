package com.campushelp.common.safety;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class RateLimitFilterTest {

    @Test
    void blocksWhenOverLimitSliding() throws Exception {
        StringRedisTemplate redis = Mockito.mock(StringRedisTemplate.class);
        when(redis.execute(any(DefaultRedisScript.class), anyList(),
                nullable(String.class), nullable(String.class), nullable(String.class), nullable(String.class)))
                .thenReturn(-1L);

        RateLimitFilter filter = new RateLimitFilter(redis, new ObjectMapper());

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setMethod("POST");
        req.setRequestURI("/api/v3/auth/login");
        req.setRemoteAddr("127.0.0.1");

        MockHttpServletResponse resp = new MockHttpServletResponse();
        filter.doFilter(req, resp, new MockFilterChain());

        assertEquals(429, resp.getStatus());
        assertTrue(resp.getContentAsString().contains("请求过于频繁"));
    }

    @Test
    void blocksWhenOverLimitFixed() throws Exception {
        StringRedisTemplate redis = Mockito.mock(StringRedisTemplate.class);
        when(redis.execute(any(DefaultRedisScript.class), anyList(), anyString()))
                .thenReturn(999L);

        RateLimitFilter filter = new RateLimitFilter(redis, new ObjectMapper());
        filter.setWindowModeForTest(RateLimitFilter.WindowMode.fixed);

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setMethod("POST");
        req.setRequestURI("/api/v3/auth/login");
        req.setRemoteAddr("127.0.0.1");

        MockHttpServletResponse resp = new MockHttpServletResponse();
        filter.doFilter(req, resp, new MockFilterChain());

        assertEquals(429, resp.getStatus());
    }

    @Test
    void passesWhenUnderLimitSliding() throws Exception {
        StringRedisTemplate redis = Mockito.mock(StringRedisTemplate.class);
        when(redis.execute(any(DefaultRedisScript.class), anyList(),
                nullable(String.class), nullable(String.class), nullable(String.class), nullable(String.class)))
                .thenReturn(1L);

        RateLimitFilter filter = new RateLimitFilter(redis, new ObjectMapper());

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setMethod("POST");
        req.setRequestURI("/api/v3/auth/login");
        req.setRemoteAddr("127.0.0.1");

        MockHttpServletResponse resp = new MockHttpServletResponse();
        filter.doFilter(req, resp, new MockFilterChain());

        assertEquals(200, resp.getStatus());
    }
}
