package me.iseunghan.lecturequerydsl.repository;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import me.iseunghan.lecturequerydsl.dto.MemberSearchCond;
import me.iseunghan.lecturequerydsl.dto.MemberTeamDto;
import me.iseunghan.lecturequerydsl.dto.QMemberTeamDto;
import me.iseunghan.lecturequerydsl.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;
import java.util.Objects;

import static me.iseunghan.lecturequerydsl.entity.QMember.member;
import static me.iseunghan.lecturequerydsl.entity.QTeam.team;
import static org.springframework.util.StringUtils.hasText;

public class MemberRepositoryCustomImpl extends QuerydslRepositorySupport implements MemberRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public MemberRepositoryCustomImpl(EntityManager em) {
        super(Member.class);
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

        JPAQuery<Long> countQuery = queryFactory
                .select(member.count())
                .from(member)
                .join(member.team, team)
                .where(
                        usernameEq(cond.getUsername()),
                        teamNameEq(cond.getTeamName()),
                        memberAgeGoe(cond.getAgeGoe()),
                        memberAgeLoe(cond.getAgeLoe())
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<MemberTeamDto> searchComplexPage_Support(MemberSearchCond cond, Pageable pageable) {
        JPQLQuery<MemberTeamDto> jpqlQuery = from(member)
                .join(member.team, team)
                .where(
                        usernameEq(cond.getUsername()),
                        teamNameEq(cond.getTeamName()),
                        memberAgeGoe(cond.getAgeGoe()),
                        memberAgeLoe(cond.getAgeLoe())
                )
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")));

        JPQLQuery<MemberTeamDto> query = getQuerydsl().applyPagination(pageable, jpqlQuery);
        QueryResults<MemberTeamDto> result = query.fetchResults();

        return new PageImpl<>(result.getResults(), pageable, result.getTotal());
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
