package com.campushelp.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * JWT 的创建与解析（对称密钥 HS512）。
 * <p>
 * 企业注意：生产环境必须通过 KMS/配置中心注入 {@code campus.jwt.secret}，且密钥足够长；
 * 多服务间必须使用同一密钥才能互相验签（网关/各微服务）。
 * <p>
 * 使用 JJWT 0.12+（JDK Base64，不依赖 JAXB）。对 {@code campus.jwt.secret} 的 UTF-8 字节做
 * SHA-512 派生为 64 字节密钥材料，满足 HS512 要求且各服务一致。
 */
@Component
public class JwtTokenProvider {

    @Value("${campus.jwt.secret}")
    private String secret;

    @Value("${campus.jwt.expiration-ms:86400000}")
    private long expirationMs;

    private SecretKey hmacKey() {
        byte[] raw = secret == null ? new byte[0] : secret.getBytes(StandardCharsets.UTF_8);
        try {
            raw = MessageDigest.getInstance("SHA-512").digest(raw);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
        return Keys.hmacShaKeyFor(raw);
    }

    public String createToken(Long userId, String... roles) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMs);
        String rolesJoined = roles == null || roles.length == 0
                ? ""
                : Stream.of(roles).collect(Collectors.joining(","));
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("roles", rolesJoined)
                .issuedAt(now)
                .expiration(exp)
                .signWith(hmacKey(), Jwts.SIG.HS512)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(hmacKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long getUserId(Claims claims) {
        return Long.parseLong(claims.getSubject());
    }

    public String[] getRoles(Claims claims) {
        Object r = claims.get("roles");
        if (r == null) {
            return new String[0];
        }
        String s = String.valueOf(r);
        if (s.isEmpty()) {
            return new String[0];
        }
        return s.split(",");
    }
}
