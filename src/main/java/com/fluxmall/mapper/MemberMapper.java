package com.fluxmall.mapper;

import com.fluxmall.domain.member.entity.Member;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MemberMapper {

    Member findById(Long id);

    Member findByUsername(String username);

    boolean findByNickname(String nickname);

    void save(Member member);

    void update(Member member);

    void updatePassword(Member member);
}