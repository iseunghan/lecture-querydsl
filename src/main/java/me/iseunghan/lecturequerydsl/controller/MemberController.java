package me.iseunghan.lecturequerydsl.controller;

import lombok.RequiredArgsConstructor;
import me.iseunghan.lecturequerydsl.dto.MemberSearchCond;
import me.iseunghan.lecturequerydsl.dto.MemberTeamDto;
import me.iseunghan.lecturequerydsl.repository.MemberJpaRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class MemberController {

    private final MemberJpaRepository memberJpaRepository;

    @GetMapping("/v1/members")
    public List<MemberTeamDto> findMembers(MemberSearchCond cond) {
        return memberJpaRepository.dynamicSearchMember_WhereParams(cond);
    }
}
