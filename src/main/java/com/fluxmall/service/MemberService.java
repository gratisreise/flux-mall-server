package com.fluxmall.service;


import com.fluxmall.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberMapper memberMapper;

    public Member findById(Long id) {
        return null;
    }

    public Member findByUsername(String username) {
        return null;
    }

    public boolean existsByNickname(String nickname) {
        return false;
    }

    public void register(Member member) {
    }

    public void updateProfile(Member member) {
    }

    public void changePassword(Member member) {
    }
}