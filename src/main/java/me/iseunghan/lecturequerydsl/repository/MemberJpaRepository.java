package me.iseunghan.lecturequerydsl.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import me.iseunghan.lecturequerydsl.dto.MemberSearchCond;
import me.iseunghan.lecturequerydsl.dto.MemberTeamDto;
import me.iseunghan.lecturequerydsl.dto.QMemberTeamDto;
import me.iseunghan.lecturequerydsl.entity.Member;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static me.iseunghan.lecturequerydsl.entity.QMember.member;
import static me.iseunghan.lecturequerydsl.entity.QTeam.team;
import static org.springframework.util.StringUtils.hasText;

@Repository
public class MemberJpaRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    public MemberJpaRepository(EntityManager em) {
        this.em = em;
        this.queryFactory = new JPAQueryFactory(em);
    }

    public void save(Member member) {
        em.persist(member);
    }

    public Optional<Member> findById(Long id) {
        Member member = em.createQuery("select m from Member m where m.id = :id", Member.class)
                .setParameter("id", id)
                .getSingleResult();
        return Optional.ofNullable(member);
    }

    public Optional<Member> findById_dsl(Long id) {
        return Optional.ofNullable(
                queryFactory.selectFrom(member)
                        .where(member.id.eq(id))
                        .fetchOne()
        );
    }

    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
    }

    public List<Member> findAll_dsl() {
        return queryFactory.selectFrom(member)
                .fetch();
    }

    public List<Member> findByUsername(String username) {
        return em.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username", username)
                .getResultList();
    }

    public List<Member> findByUsername_dsl(String username) {
        return queryFactory.selectFrom(member)
                .where(member.username.eq(username))
                .fetch();
    }

    public List<MemberTeamDto> dynamicSearchMember_Builder(MemberSearchCond cond) {
        BooleanBuilder builder = new BooleanBuilder();  // 주의! 기본 조건(동적쿼리로 인해 결과 개수가 천차만별이므로)으로 페이징을 꼭 넣어줘야 한다.

        if (hasText(cond.getUsername())) {
            builder.and(member.username.eq(cond.getUsername()));
        }
        if (hasText(cond.getTeamName())) {
            builder.and(team.name.eq(cond.getTeamName()));
        }
        if (Objects.nonNull(cond.getAgeGoe())) {
            builder.and(member.age.goe(cond.getAgeGoe()));
        }
        if (Objects.nonNull(cond.getAgeLoe())) {
            builder.and(member.age.loe(cond.getAgeLoe()));
        }

        return queryFactory.select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                ))
                .from(member)
                .join(member.team, team)
                .where(builder)
                .fetch();
    }

    public List<MemberTeamDto> dynamicSearchMember_WhereParams(MemberSearchCond cond) {
        return queryFactory.select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                ))
                .from(member)
                .join(member.team, team)
                .where(
                        usernameEq(cond.getUsername()),
                        teamNameEq(cond.getTeamName()),
                        memberAgeGoe(cond.getAgeGoe()),
                        memberAgeLoe(cond.getAgeLoe())
                )
                .fetch();
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
