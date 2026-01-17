package com.fluxmall.member.repository;

import com.fluxmall.member.domain.Member;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MemberMapper {

    Member findById(Long id);

    Member findByUsername(String username);

    Member findByNickname(String nickname);

    boolean existsByUsername(String username);

    boolean existsByNickname(String nickname);

    void insertMember(Member member);

    void updateMember(Member member);

    void updatePassword(Long memberId, String encodedPassword);
}
