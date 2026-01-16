package com.fluxmall.auth.classes;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
public class CustomUserDetails implements UserDetails {

    private final Long memberId;      // 우리가 리졸버에서 꺼내 쓸 PK
    private final String email;       // Spring Security의 username 역할
    private final String password;    // 인코딩된 패스워드
    private final Collection<? extends GrantedAuthority> authorities;

    // 생성자: 필터에서 토큰 정보를 바탕으로 이 객체를 생성할 때 사용
    @Builder
    public CustomUserDetails(Long memberId, String email, String password, List<String> roles) {
        this.memberId = memberId;
        this.email = email;
        this.password = password;
        this.authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    /**
     * UserDetails 인터페이스 구현 메서드들
     */
    
    @Override
    public String getUsername() {
        return email; // 우리 시스템에서는 이메일을 아이디로 사용
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    // 계정 만료, 잠금, 만료, 활성화 여부 (보통 true로 설정하거나 비즈니스 로직에 맞게 구현)
    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}