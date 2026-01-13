package com.fluxmall.service;

import com.fluxmall.domain.dto.LoginRequest;
import com.fluxmall.domain.dto.LoginResponse;
import com.fluxmall.domain.dto.RegisterRequest;
import com.fluxmall.domain.entity.Member;
import com.fluxmall.exception.BusinessException;
import com.fluxmall.exception.errors.AuthError;
import com.fluxmall.mapper.MemberMapper;
import com.fluxmall.utils.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * 회원가입
     */
    @Transactional
    public Member register(@Valid RegisterRequest request) {
        // 이메일 중복 체크
        if (memberMapper.existsByUsername(request.username())) {
            throw new BusinessException(AuthError.DUPLICATE_USERNAME);
        }

        // 닉네임 중복 체크
        if (memberMapper.existsByNickname(request.nickname())) {
            throw new BusinessException(AuthError.DUPLICATE_NICKNAME);
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.password());

        // 회원 생성 (기본 역할 = USER)
        Member member = Member.builder()
                .username(request.username())
                .password(encodedPassword)
                .nickname(request.nickname())
                .role(Member.MemberRole.USER)
                .build();

        // DB 저장
        memberMapper.insertMember(member);

        return member;
    }

    /**
     * 로그인 (인증)
     */
    @Transactional(readOnly = true)
    public LoginResponse authenticate(@Valid LoginRequest request) {
        // 사용자 조회
        Member member = memberMapper.findByUsername(request.username());
        if (member == null) {
            throw new BusinessException(AuthError.INVALID_CREDENTIALS);
        }

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new BusinessException(AuthError.INVALID_CREDENTIALS);
        }

        // JWT 토큰 생성 (role 포함)
        String accessToken = jwtUtil.createAccessToken(member.getId(), member.getRole().name());
        String refreshToken = jwtUtil.createRefreshToken(member.getId());

        return new LoginResponse(accessToken, refreshToken);
    }

    /**
     * ID로 회원 조회
     */
    @Transactional(readOnly = true)
    public Member findById(Long id) {
        Member member = memberMapper.findById(id);
        if (member == null) {
            throw new BusinessException(AuthError.MEMBER_NOT_FOUND);
        }
        return member;
    }

    /**
     * Username으로 회원 조회
     */
    @Transactional(readOnly = true)
    public Member findByUsername(String username) {
        return memberMapper.findByUsername(username);
    }

    /**
     * 닉네임 존재 여부 확인
     */
    @Transactional(readOnly = true)
    public boolean existsByNickname(String nickname) {
        return memberMapper.existsByNickname(nickname);
    }

    /**
     * 프로필 수정 (닉네임)
     */
    @Transactional
    public void updateProfile(Long memberId, String newNickname) {
        Member member = findById(memberId);

        // 닉네임이 변경된 경우 중복 체크
        if (!member.getNickname().equals(newNickname)) {
            if (memberMapper.existsByNickname(newNickname)) {
                throw new BusinessException(AuthError.DUPLICATE_NICKNAME);
            }
            member.setNickname(newNickname);
            memberMapper.updateMember(member);
        }
    }

    /**
     * 비밀번호 변경
     */
    @Transactional
    public void changePassword(Long memberId, String newPassword) {
        String encodedPassword = passwordEncoder.encode(newPassword);
        memberMapper.updatePassword(memberId, encodedPassword);
    }
}
