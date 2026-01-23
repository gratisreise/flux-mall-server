package com.fluxmall.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * 통합 테스트 Base 클래스
 * - Testcontainers로 실제 MySQL 컨테이너 사용
 * - MockMvc로 HTTP 요청 시뮬레이션
 * - @Transactional로 테스트 후 자동 롤백
 */
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public abstract class IntegrationTestBase {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withCommand("--character-set-server=utf8mb4", "--collation-server=utf8mb4_unicode_ci");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);

        // Redis 비활성화 (통합 테스트에서는 Redis 없이 진행)
        registry.add("spring.data.redis.host", () -> "localhost");
        registry.add("spring.data.redis.port", () -> "6379");
        registry.add("spring.cache.type", () -> "none");

        // JWT 설정 (테스트용)
        registry.add("jwt.access-secret", () -> "test-access-secret-key-for-integration-test-minimum-256-bits");
        registry.add("jwt.refresh-secret", () -> "test-refresh-secret-key-for-integration-test-minimum-256-bits");
        registry.add("jwt.access-expiration", () -> "1800000");
        registry.add("jwt.refresh-expiration", () -> "604800000");
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;
}
