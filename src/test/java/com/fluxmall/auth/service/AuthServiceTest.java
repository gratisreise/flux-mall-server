package com.fluxmall.auth.service;

import com.fluxmall.auth.dto.request.LoginRequest;
import com.fluxmall.auth.dto.response.LoginResponse;
import com.fluxmall.auth.dto.response.LogoutResponse;
import com.fluxmall.auth.dto.response.TokenRefreshResponse;
import com.fluxmall.auth.exception.AuthError;
import com.fluxmall.global.exception.BusinessException;
import com.fluxmall.global.util.JwtUtil;
import com.fluxmall.member.domain.Member;
import com.fluxmall.member.repository.MemberMapper;
import com.fluxmall.support.TestFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 테스트")
class AuthServiceTest {

    @Mock
    private MemberMapper memberMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @InjectMocks
    private AuthService authService;

    @Nested
    @DisplayName("login: 로그인")
    class Login {

        @Test
        @DisplayName("성공: 정상 로그인")
        void 로그인_성공() {
            // given
            Member member = TestFixture.createMember(1L, "testuser");
            LoginRequest request = new LoginRequest("testuser", "password123!");

            given(memberMapper.findByUsername("testuser")).willReturn(member);
            given(passwordEncoder.matches("password123!", member.getPassword())).willReturn(true);
            given(jwtUtil.createAccessToken(member.getId(), member.getRole().name()))
                    .willReturn("access-token");
            given(jwtUtil.createRefreshToken(member.getId()))
                    .willReturn("refresh-token");

            // when
            LoginResponse response = authService.login(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.accessToken()).isEqualTo("access-token");
            assertThat(response.refreshToken()).isEqualTo("refresh-token");
        }

        @Test
        @DisplayName("실패: 사용자 없음")
        void 사용자_없음() {
            // given
            LoginRequest request = new LoginRequest("nonexistent", "password123!");

            given(memberMapper.findByUsername("nonexistent")).willReturn(null);

            // when & then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(AuthError.INVALID_CREDENTIALS);
        }

        @Test
        @DisplayName("실패: 비밀번호 불일치")
        void 비밀번호_불일치() {
            // given
            Member member = TestFixture.createMember(1L, "testuser");
            LoginRequest request = new LoginRequest("testuser", "wrongpassword");

            given(memberMapper.findByUsername("testuser")).willReturn(member);
            given(passwordEncoder.matches("wrongpassword", member.getPassword())).willReturn(false);

            // when & then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(AuthError.INVALID_CREDENTIALS);
        }
    }

    @Nested
    @DisplayName("logout: 로그아웃")
    class Logout {

        @Test
        @DisplayName("성공: 정상 로그아웃")
        void 로그아웃_성공() {
            // given
            String token = "valid-token";

            // when
            LogoutResponse response = authService.logout(token);

            // then
            assertThat(response).isNotNull();
            assertThat(response.message()).isEqualTo("로그아웃되었습니다.");
            verify(tokenBlacklistService).addToBlacklist(token);
        }
    }

    @Nested
    @DisplayName("refresh: 토큰 재발급")
    class Refresh {

        @Test
        @DisplayName("성공: Access Token 재발급")
        void 토큰_재발급_성공() {
            // given
            String refreshToken = "valid-refresh-token";
            Member member = TestFixture.createMember(1L, "testuser");

            given(jwtUtil.validateRefreshToken(refreshToken)).willReturn(true);
            given(jwtUtil.getRefreshMemberId(refreshToken)).willReturn(1L);
            given(memberMapper.findById(1L)).willReturn(member);
            given(jwtUtil.createAccessToken(member.getId(), member.getRole().name()))
                    .willReturn("new-access-token");

            // when
            TokenRefreshResponse response = authService.refresh(refreshToken);

            // then
            assertThat(response).isNotNull();
            assertThat(response.accessToken()).isEqualTo("new-access-token");
        }

        @Test
        @DisplayName("실패: 유효하지 않은 Refresh Token")
        void 유효하지_않은_토큰() {
            // given
            String invalidToken = "invalid-refresh-token";

            willThrow(new BusinessException(AuthError.INVALID_TOKEN))
                    .given(jwtUtil).validateRefreshToken(invalidToken);

            // when & then
            assertThatThrownBy(() -> authService.refresh(invalidToken))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(AuthError.INVALID_TOKEN);
        }

        @Test
        @DisplayName("실패: 회원 없음")
        void 회원_없음() {
            // given
            String refreshToken = "valid-refresh-token";

            given(jwtUtil.validateRefreshToken(refreshToken)).willReturn(true);
            given(jwtUtil.getRefreshMemberId(refreshToken)).willReturn(999L);
            given(memberMapper.findById(999L)).willReturn(null);

            // when & then
            assertThatThrownBy(() -> authService.refresh(refreshToken))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(AuthError.MEMBER_NOT_FOUND);
        }
    }
}
