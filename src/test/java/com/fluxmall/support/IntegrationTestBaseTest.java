package com.fluxmall.support;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * IntegrationTestBase 검증 테스트
 */
@DisplayName("통합 테스트 기반 검증")
class IntegrationTestBaseTest extends IntegrationTestBase {

    @Test
    @DisplayName("Testcontainers MySQL 컨테이너 정상 동작")
    void MySQL_컨테이너_정상_동작() {
        assertThat(mysql.isRunning()).isTrue();
        assertThat(mysql.getDatabaseName()).isEqualTo("testdb");
    }

    @Test
    @DisplayName("MockMvc 정상 주입")
    void MockMvc_정상_주입() {
        assertThat(mockMvc).isNotNull();
    }

    @Test
    @DisplayName("ObjectMapper 정상 주입")
    void ObjectMapper_정상_주입() {
        assertThat(objectMapper).isNotNull();
    }
}
