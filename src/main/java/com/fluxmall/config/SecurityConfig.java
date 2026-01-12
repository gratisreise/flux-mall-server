package com.fluxmall.config;

import com.fluxmall.filter.ExceptionHandlerFilter;
import com.fluxmall.filter.JwtAuthenticationFilter;
import com.fluxmall.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
//@EnableWebSecurity
public class SecurityConfig {

//    private final JwtUtil jwtUtil;

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
//            .authorizeHttpRequests(auth -> auth
//                .requestMatchers(PUBLIC_ENDPOINTS).permitAll() // 해당 url 허용
//                .requestMatchers("/api/admin/**").hasRole("ADMIN")
//                .requestMatchers("/api/seller/**").hasRole("SELLER")
//                .anyRequest().authenticated() // 나머지 접근 방지
//            )
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())

            //jwt 커스텀필터 넣기
//            .addFilterBefore(
//                new ExceptionHandlerFilter(),
//                UsernamePasswordAuthenticationFilter.class)
//            .addFilterBefore(
//                new JwtAuthenticationFilter(jwtUtil),
//                UsernamePasswordAuthenticationFilter.class)

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

    private static final String[] PUBLIC_ENDPOINTS = {
        // 회원가입 및 로그인 (JWT 토큰 발급)
        "/api/members/signup",      // 회원가입 (이메일/비밀번호/닉네임 중복 체크 포함)
        "/api/members/login",       // 로그인 (JWT 발급)

        // 상품 도메인 - 비로그인 사용자도 접근 가능
        "/api/products",            // 상품 목록 조회 (페이징, 필터링, 정렬)
        "/api/products/search",     // 키워드 검색
        "/api/products/{id}",       // 상품 상세 조회 (재고, 평점 포함)
        "/api/products/{id}/reviews",

        "/css/**",
        "/js/**",
        "/images/**",
        "/img/**",
        "/assets/**",
        "/favicon.ico",
        "/webjars/**",              // Swagger UI 등에서 사용

        // AWS S3 상품/리뷰 이미지 (외부 URL이지만 프록시 경로 존재 시)
        "/s3/images/**",

        // Swagger / OpenAPI 문서화 (springdoc-openapi-starter-webmvc-ui 2.5.0)
        "/v3/api-docs/**",
        "/swagger-ui.html",
        "/swagger-ui/**",

        // Actuator 모니터링 (보통 health/info는 공개, 나머지는 제한)
        "/actuator/**",
    };
}
