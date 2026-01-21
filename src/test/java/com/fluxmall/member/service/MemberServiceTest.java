package com.fluxmall.member.service;

import com.fluxmall.auth.exception.AuthError;
import com.fluxmall.global.exception.BusinessException;
import com.fluxmall.member.domain.Member;
import com.fluxmall.member.dto.request.RegisterRequest;
import com.fluxmall.member.dto.response.MemberResponse;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberService 테스트")
class MemberServiceTest {

    @Mock
    private MemberMapper memberMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private MemberService memberService;

    @Nested
    @DisplayName("register: 회원가입")
    class Register {

        @Test
        @DisplayName("성공: 정상 회원가입")
        void 회원가입_성공() {
            // given
            RegisterRequest request = new RegisterRequest(
                    "test@example.com",
                    "Password123!",
                    "테스트유저"
            );

            given(memberMapper.existsByUsername("test@example.com")).willReturn(false);
            given(memberMapper.existsByNickname("테스트유저")).willReturn(false);
            given(passwordEncoder.encode("Password123!")).willReturn("encoded_password");

            // when
            memberService.register(request);

            // then
            verify(memberMapper).insertMember(any(Member.class));
        }

        @Test
        @DisplayName("실패: 이메일 중복")
        void 이메일_중복() {
            // given
            RegisterRequest request = new RegisterRequest(
                    "duplicate@example.com",
                    "Password123!",
                    "테스트유저"
            );

            given(memberMapper.existsByUsername("duplicate@example.com")).willReturn(true);

            // when & then
            assertThatThrownBy(() -> memberService.register(request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(AuthError.DUPLICATE_USERNAME);

            verify(memberMapper, never()).insertMember(any(Member.class));
        }

        @Test
        @DisplayName("실패: 닉네임 중복")
        void 닉네임_중복() {
            // given
            RegisterRequest request = new RegisterRequest(
                    "test@example.com",
                    "Password123!",
                    "중복닉네임"
            );

            given(memberMapper.existsByUsername("test@example.com")).willReturn(false);
            given(memberMapper.existsByNickname("중복닉네임")).willReturn(true);

            // when & then
            assertThatThrownBy(() -> memberService.register(request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(AuthError.DUPLICATE_NICKNAME);

            verify(memberMapper, never()).insertMember(any(Member.class));
        }
    }

    @Nested
    @DisplayName("getMyInfo: 내 정보 조회")
    class GetMyInfo {

        @Test
        @DisplayName("성공: 내 정보 조회")
        void 내_정보_조회_성공() {
            // given
            Long memberId = 1L;
            Member member = TestFixture.createMember(memberId, "testuser");

            given(memberMapper.findById(memberId)).willReturn(member);

            // when
            MemberResponse response = memberService.getMyInfo(memberId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(memberId);
        }

        @Test
        @DisplayName("실패: 회원 없음")
        void 회원_없음() {
            // given
            Long memberId = 999L;

            given(memberMapper.findById(memberId)).willReturn(null);

            // when & then
            assertThatThrownBy(() -> memberService.getMyInfo(memberId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(AuthError.MEMBER_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("updateProfile: 프로필 수정")
    class UpdateProfile {

        @Test
        @DisplayName("성공: 닉네임 변경")
        void 닉네임_변경_성공() {
            // given
            Long memberId = 1L;
            Member member = TestFixture.createMember(memberId, "testuser");
            String newNickname = "새닉네임";

            given(memberMapper.findById(memberId)).willReturn(member);
            given(memberMapper.existsByNickname(newNickname)).willReturn(false);

            // when
            MemberResponse response = memberService.updateProfile(memberId, newNickname);

            // then
            assertThat(response).isNotNull();
            verify(memberMapper).updateMember(any(Member.class));
        }

        @Test
        @DisplayName("성공: 닉네임 동일하면 변경 안함")
        void 닉네임_동일_변경_없음() {
            // given
            Long memberId = 1L;
            Member member = TestFixture.createMember(memberId, "testuser");
            String sameNickname = member.getNickname();

            given(memberMapper.findById(memberId)).willReturn(member);

            // when
            MemberResponse response = memberService.updateProfile(memberId, sameNickname);

            // then
            assertThat(response).isNotNull();
            verify(memberMapper, never()).updateMember(any(Member.class));
        }

        @Test
        @DisplayName("실패: 닉네임 중복")
        void 닉네임_중복() {
            // given
            Long memberId = 1L;
            Member member = TestFixture.createMember(memberId, "testuser");
            String duplicateNickname = "중복닉네임";

            given(memberMapper.findById(memberId)).willReturn(member);
            given(memberMapper.existsByNickname(duplicateNickname)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> memberService.updateProfile(memberId, duplicateNickname))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(AuthError.DUPLICATE_NICKNAME);

            verify(memberMapper, never()).updateMember(any(Member.class));
        }
    }

    @Nested
    @DisplayName("changePassword: 비밀번호 변경")
    class ChangePassword {

        @Test
        @DisplayName("성공: 비밀번호 변경")
        void 비밀번호_변경_성공() {
            // given
            Long memberId = 1L;
            String newPassword = "NewPassword123!";

            given(passwordEncoder.encode(newPassword)).willReturn("encoded_new_password");

            // when
            memberService.changePassword(memberId, newPassword);

            // then
            verify(memberMapper).updatePassword(memberId, "encoded_new_password");
        }
    }

    @Nested
    @DisplayName("findById: 회원 조회")
    class FindById {

        @Test
        @DisplayName("성공: 회원 조회")
        void 회원_조회_성공() {
            // given
            Long memberId = 1L;
            Member member = TestFixture.createMember(memberId, "testuser");

            given(memberMapper.findById(memberId)).willReturn(member);

            // when
            Member result = memberService.findById(memberId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(memberId);
        }

        @Test
        @DisplayName("실패: 회원 없음")
        void 회원_없음() {
            // given
            Long memberId = 999L;

            given(memberMapper.findById(memberId)).willReturn(null);

            // when & then
            assertThatThrownBy(() -> memberService.findById(memberId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(AuthError.MEMBER_NOT_FOUND);
        }
    }
}
