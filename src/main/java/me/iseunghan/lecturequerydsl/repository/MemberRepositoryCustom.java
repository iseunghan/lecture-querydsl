package me.iseunghan.lecturequerydsl.repository;

import me.iseunghan.lecturequerydsl.dto.MemberSearchCond;
import me.iseunghan.lecturequerydsl.dto.MemberTeamDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MemberRepositoryCustom {
    List<MemberTeamDto> search(MemberSearchCond cond);

    Page<MemberTeamDto> searchComplexPage(MemberSearchCond cond, Pageable pageable);
}
