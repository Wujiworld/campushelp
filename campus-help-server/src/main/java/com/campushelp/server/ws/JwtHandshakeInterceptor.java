package com.campushelp.server.ws;

import com.campushelp.common.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    public static final String ATTR_UID = "ws.uid";

    private final JwtTokenProvider jwtTokenProvider;

    public JwtHandshakeInterceptor(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String token = null;
        if (request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
            String auth = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (auth != null && auth.startsWith("Bearer ")) {
                token = auth.substring("Bearer ".length()).trim();
            }
        }
        if (token == null && request instanceof ServletServerHttpRequest) {
            HttpServletRequest r = ((ServletServerHttpRequest) request).getServletRequest();
            String q = r.getParameter("token");
            if (q != null && !q.isBlank()) {
                token = q.trim();
            }
        }

        try {
            if (token == null || token.isBlank()) {
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return false;
            }
            Claims claims = jwtTokenProvider.parseClaims(token);
            Long uid = jwtTokenProvider.getUserId(claims);
            attributes.put(ATTR_UID, uid);
            return true;
        } catch (Exception e) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }
}

