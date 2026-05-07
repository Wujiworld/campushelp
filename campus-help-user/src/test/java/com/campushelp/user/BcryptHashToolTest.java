package com.campushelp.user;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 校验种子 SQL 中 BCrypt 与 Spring {@link BCryptPasswordEncoder} 互认。
 */
public class BcryptHashToolTest {

    @Test
    void hutoolHashMatchesSpringEncoder() {
        String h = "$2a$10$fjuBodlRHJOue6DiOAoC1uir2nqtm8kgCZ976eiIZjKP1nu28B5PW";
        Assertions.assertTrue(new BCryptPasswordEncoder().matches("123456", h));
    }
}
