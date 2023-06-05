package me.iseunghan.lecturequerydsl.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import me.iseunghan.lecturequerydsl.dto.MemberSearchCond;
import me.iseunghan.lecturequerydsl.dto.MemberTeamDto;
import me.iseunghan.lecturequerydsl.dto.QMemberTeamDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Objects;

import static me.iseunghan.lecturequerydsl.entity.QMember.member;
import static me.iseunghan.lecturequerydsl.entity.QTeam.team;
import static org.springframework.util.StringUtils.hasText;

public class MemberRepositoryCustomImpl implements MemberRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public MemberRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<MemberTeamDto> search(MemberSearchCond cond) {
        return selectFromJoinMemberTeam()
                .where(
                        usernameEq(cond.getUsername()),
                        teamNameEq(cond.getTeamName()),
                        memberAgeGoe(cond.getAgeGoe()),
                        memberAgeLoe(cond.getAgeLoe())
                )
                .fetch();
    }

    private JPAQuery<MemberTeamDto> selectFromJoinMemberTeam() {
        return queryFactory.select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                ))
                .from(member)
                .join(member.team, team);
    }

    @Override
    public Page<MemberTeamDto> searchComplexPage(MemberSearchCond cond, Pageable pageable) {
        List<MemberTeamDto> content = selectFromJoinMemberTeam()
                .where(
                        usernameEq(cond.getUsername()),
                        teamNameEq(cond.getTeamName()),
                        memberAgeGoe(cond.getAgeGoe()),
                        memberAgeLoe(cond.getAgeLoe())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(member.count())
                .from(member)
                .join(member.team, team)
                .where(
                        usernameEq(cond.getUsername()),
                        teamNameEq(cond.getTeamName()),
                        memberAgeGoe(cond.getAgeGoe()),
                        memberAgeLoe(cond.getAgeLoe())
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }

    /**
     * Predicate말고 BooleanExpression 타입으로 반환하는 이유는, and(), or() 메소드를 제공해줘서 composition하기 좋음!
     */
    private BooleanExpression usernameEq(String username) {
        return hasText(username) ?
                member.username.eq(username) : null;
    }

    private BooleanExpression teamNameEq(String teamName) {
        return hasText(teamName) ?
                team.name.eq(teamName) : null;
    }

    private BooleanExpression memberAgeGoe(Integer memberAge) {
        return Objects.nonNull(memberAge) ?
                member.age.goe(memberAge) : null;
    }

    private BooleanExpression memberAgeLoe(Integer memberAge) {
        return Objects.nonNull(memberAge) ?
                member.age.loe(memberAge) : null;
    }
}