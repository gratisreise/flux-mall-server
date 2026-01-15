package com.fluxmall.config;

import com.fluxmall.filter.ExceptionHandlerFilter;
import com.fluxmall.filter.JwtAuthenticationFilter;
import com.fluxmall.service.auth.TokenBlacklistService;
import com.fluxmall.utils.JwtUtil;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 설정
 * JWT 기반 인증 및 역할 기반 접근 제어(RBAC)를 구성합니다.
 *
 * 주요 기능:
 * - JWT 토큰 기반 Stateless 인증
 * - 역할별 엔드포인트 접근 제어 (USER, SELLER, ADMIN)
 * - 공개 엔드포인트 설정 (회원가입, 로그인, 상품 조회 등)
 * - 커스텀 필터 체인 (ExceptionHandlerFilter, JwtAuthenticationFilter)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)  // @PreAuthorize 활성화
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)  // CSRF 비활성화
            .formLogin(AbstractHttpConfigurer::disable)  // 폼 로그인 비활성화
            .httpBasic(AbstractHttpConfigurer::disable)  // HTTP Basic 인증 비활성화
            //세션 비활성화 stateless
            .sessionManagement(
                session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            // 요청 정책
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(PUBLIC_ENDPOINTS).permitAll() // 공개 엔드포인트 허용
                .requestMatchers("/api/admin/**").hasRole("ADMIN")  // ADMIN 역할 필요
                .requestMatchers("/api/seller/**").hasRole("SELLER")  // SELLER 역할 필요
                .anyRequest().authenticated() // 나머지는 인증 필요
            )
            //jwt 커스텀필터 넣기
            .addFilterBefore(
                new ExceptionHandlerFilter(),
                UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(
                new JwtAuthenticationFilter(jwtUtil, tokenBlacklistService),
                UsernamePasswordAuthenticationFilter.class)

            .build();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception{
        return authenticationConfiguration.getAuthenticationManager();
    }

    // ============================
    // Public Endpoints Configuration
    // ============================

    // 인증 관련 공개 엔드포인트
    private static final String[] AUTH_ENDPOINTS = {
        "/api/members/register",    // 회원가입
        "/api/auth/login",           // 로그인 (JWT 발급)
        "/api/auth/refresh"          // 토큰 재발급
    };

    // 상품 관련 공개 엔드포인트 (비로그인 사용자도 접근 가능)
    private static final String[] PRODUCT_ENDPOINTS = {
        "/api/products",             // 상품 목록 조회
        "/api/products/search",      // 키워드 검색
        "/api/products/{id}",        // 상품 상세 조회
        "/api/products/{id}/reviews" // 리뷰 목록 조회
    };

    // 정적 리소스 엔드포인트
    private static final String[] STATIC_RESOURCES = {
        "/css/**",
        "/js/**",
        "/images/**",
        "/img/**",
        "/assets/**",
        "/favicon.ico",
        "/webjars/**",               // Swagger UI 등에서 사용
        "/s3/images/**"              // AWS S3 상품/리뷰 이미지
    };

    // API 문서화 엔드포인트
    private static final String[] DOCUMENTATION_ENDPOINTS = {
        "/v3/api-docs/**",           // OpenAPI 스펙
        "/swagger-ui.html",          // Swagger UI 홈
        "/swagger-ui/**"             // Swagger UI 리소스
    };

    // 모니터링 엔드포인트 (공개)
    private static final String[] MONITORING_ENDPOINTS = {
        "/actuator/health",          // 헬스 체크
        "/actuator/info"             // 애플리케이션 정보
    };

    /**
     * 인증 없이 접근 가능한 엔드포인트 목록
     * 카테고리별로 그룹화하여 가독성과 유지보수성을 향상
     */
    private static final String[] PUBLIC_ENDPOINTS = combineArrays(
        AUTH_ENDPOINTS,
        PRODUCT_ENDPOINTS,
        STATIC_RESOURCES,
        DOCUMENTATION_ENDPOINTS,
        MONITORING_ENDPOINTS
    );

    /**
     * 여러 String 배열을 하나로 결합하는 유틸리티 메서드
     */
    private static String[] combineArrays(String[]... arrays) {
        return Arrays.stream(arrays)
            .flatMap(Arrays::stream)
            .toArray(String[]::new);
    }
}
