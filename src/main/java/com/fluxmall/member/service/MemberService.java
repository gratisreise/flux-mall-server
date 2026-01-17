package com.fluxmall.member.service;

import com.fluxmall.member.dto.request.RegisterRequest;
import com.fluxmall.member.domain.Member;
import com.fluxmall.global.exception.BusinessException;
import com.fluxmall.auth.exception.AuthError;
import com.fluxmall.member.dto.response.MemberResponse;
import com.fluxmall.member.repository.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원가입
     */
    @Transactional
    public void register(RegisterRequest request) {
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
        Member member = request.toEntity(encodedPassword);

        // DB 저장
        memberMapper.insertMember(member);
    }

    /**
     * 내 정보 조회
     */
    @Transactional(readOnly = true)
    public MemberResponse getMyInfo(Long memberId) {
        Member member = findById(memberId);
        return MemberResponse.from(member);
    }

    /**
     * 프로필 수정 (닉네임)
     */
    @Transactional
    public MemberResponse updateProfile(Long memberId, String newNickname) {
        Member member = findById(memberId);

        // 닉네임이 변경된 경우 중복 체크
        if (!member.getNickname().equals(newNickname)) {
            if (memberMapper.existsByNickname(newNickname)) {
                throw new BusinessException(AuthError.DUPLICATE_NICKNAME);
            }
            member.setNickname(newNickname);
            memberMapper.updateMember(member);
        }
        return MemberResponse.from(member);
    }

    /**
     * 비밀번호 변경
     */
    @Transactional
    public void changePassword(Long memberId, String newPassword) {
        String encodedPassword = passwordEncoder.encode(newPassword);
        memberMapper.updatePassword(memberId, encodedPassword);
    }

    /**
     * ID로 회원 조회 (내부용)
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
     * Username으로 회원 조회 (내부용)
     */
    @Transactional(readOnly = true)
    public Member findByUsername(String username) {
        return memberMapper.findByUsername(username);
    }
}
