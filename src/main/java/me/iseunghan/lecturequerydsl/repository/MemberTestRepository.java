package me.iseunghan.lecturequerydsl.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import me.iseunghan.lecturequerydsl.dto.MemberSearchCond;
import me.iseunghan.lecturequerydsl.dto.MemberTeamDto;
import me.iseunghan.lecturequerydsl.dto.QMemberTeamDto;
import me.iseunghan.lecturequerydsl.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

import static com.querydsl.core.types.ExpressionUtils.count;
import static me.iseunghan.lecturequerydsl.entity.QMember.member;
import static me.iseunghan.lecturequerydsl.entity.QTeam.team;
import static org.springframework.util.StringUtils.hasText;

@Repository
public class MemberTestRepository extends Querydsl4RepositorySupport {

    public MemberTestRepository() {
        super(Member.class);
    }

    public List<Member> basicSelect() {
        return select(member)
                .from(member)
                .fetch();
    }

    public List<Member> basicSelectFrom() {
        return selectFrom(member)
                .fetch();
    }

    public Page<MemberTeamDto> pageSelect(MemberSearchCond cond, Pageable pageable) {
        return applyPagination(pageable, query -> query.select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")))
                .from(member)
                .join(member.team, team)
                .where(
                        usernameEq(cond.getUsername()),
                        teamNameEq(cond.getTeamName()),
                        memberAgeGoe(cond.getAgeGoe()),
                        memberAgeLoe(cond.getAgeLoe())
                )
        );
    }

    public Page<MemberTeamDto> pageSelect_Count(MemberSearchCond cond, Pageable pageable) {
        return applyPagination(pageable,
                contentQuery -> contentQuery.select(new QMemberTeamDto(
                                member.id.as("memberId"),
                                member.username,
                                member.age,
                                team.id.as("teamId"),
                                team.name.as("teamName")))
                        .from(member)
                        .join(member.team, team)
                        .where(
                                usernameEq(cond.getUsername()),
                                teamNameEq(cond.getTeamName()),
                                memberAgeGoe(cond.getAgeGoe()),
                                memberAgeLoe(cond.getAgeLoe())),
                countQuery -> countQuery.select(count(member.id))
                        .from(member)
                        .join(member.team, team)
                        .where(
                                usernameEq(cond.getUsername()),
                                teamNameEq(cond.getTeamName()),
                                memberAgeGoe(cond.getAgeGoe()),
                                memberAgeLoe(cond.getAgeLoe())
                        )
        );
    }

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
