package me.iseunghan.lecturequerydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.*;
import me.iseunghan.lecturequerydsl.entity.Member;
import me.iseunghan.lecturequerydsl.entity.QMember;
import me.iseunghan.lecturequerydsl.entity.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static me.iseunghan.lecturequerydsl.entity.QMember.*;
import static me.iseunghan.lecturequerydsl.entity.QMember.member;
import static me.iseunghan.lecturequerydsl.entity.QTeam.team;
import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
public class QuerydslTest {

    @Autowired
    EntityManager em;

    /**
     * 동시성 이슈?
     * em이 멀티스레드 환경에서 사용할 수 있도록 설계되었기 때문에 트랜잭션 별로 영속성 컨텍스트가 분리되기 때문에
     * 따로 동시성 이슈를 걱정하지 않아도 된다.
     */
    JPAQueryFactory queryFactory;

    @BeforeEach
    void setup() {
        queryFactory = new JPAQueryFactory(em);

        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 10, teamA);
        Member member3 = new Member("member3", 10, teamA);
        Member member4 = new Member("member4", 10, teamB);
        Member member5 = new Member("member5", 10, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
        em.persist(member5);

        em.flush();
        em.clear();
    }

    @Test
    void startJPQL() {
        // 단점: 문자열이기 때문에 컴파일 시점에 못잡는다. 런타임시 에러를 통해 알 수 있다.
        Member findMember = em.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username", "member1")
                .getSingleResult();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    void startQuerydsl() {
        // 장점: 빌더 패턴으로 쿼리를 짤 수 있다.
        // 자바 코드로 쿼리를 생성하기 때문에 타입 에러가 전혀 없다. 컴파일 시점에서 잡을 수 있다.
        Member findMember = queryFactory
                .select(member)
                .from(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    void searchOrCondition() {
        List<Member> findMembers = queryFactory
                .select(member)
                .from(member)
                .where(member.username.eq("member1")
                        .or(member.age.between(10, 20)))
                .fetch();    // Result가 여러 개 일때, fetchOne은 NonUniqueResult 예외 발생!

        assertThat(findMembers).isNotEmpty();
    }

    @Test
    void searchAndCondition1() {
        List<Member> findMembers = queryFactory
                .select(member)
                .from(member)
                .where(
                        member.username.eq("member1")
                        .and(member.age.between(10, 20))
                )
                .fetch();

        assertThat(findMembers).isNotEmpty();
    }

    @Test
    void searchAndCondition2() {
        List<Member> findMembers = queryFactory
                .select(member)
                .from(member)
                .where(
                        member.username.eq("member1"),
                        member.age.between(10, 20)
                )   // 모든 where 조건이 and 인 경우에는 쉼표(,)로 작성할 수 있다.
                .fetch();

        assertThat(findMembers).isNotEmpty();
    }

    @Test
    void fetchOneQuery() {
        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();
    }

    @Test
    void fetchQuery() {
        List<Member> findMembers = queryFactory
                .selectFrom(member)
                .fetch();
    }

    @Test
    void fetchFirstQuery() {
        Member findMember = queryFactory
                .selectFrom(member)
                .fetchFirst();
        // .limit(1).fetchOne() 과 동일
    }

    @Test
    void fetchCount() {
        Long totalCount = queryFactory
                .select(member.count())
                .from(member)
                .fetchOne();
    }

    @Test
    void orderUsernameDesc() {
        em.persist(new Member(null, 100));
        em.persist(new Member("member80", 80));
        em.persist(new Member("member70", 70));

        List<Member> findMembers = queryFactory
                .selectFrom(member)
                .where(member.age.goe(70))
                .orderBy(
                        member.age.asc(),
                        member.username.desc().nullsLast()
                )
                .fetch();

        assertThat(findMembers.get(0).getUsername()).isEqualTo("member70");
        assertThat(findMembers.get(1).getUsername()).isEqualTo("member80");
        assertThat(findMembers.get(2).getUsername()).isNull();
    }

    @Test
    void pagingQuery() {
        QueryResults<Member> queryResults = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .where(member.age.gt(20))
                .offset(0)
                .limit(5)
                .fetchResults();

        assertThat(queryResults.getLimit()).isEqualTo(5);
        assertThat(queryResults.getOffset()).isEqualTo(0);
        assertThat(queryResults.getTotal()).isEqualTo(3);
    }

    @Test
    void aggregation() {
        List<Tuple> result = queryFactory
                .select(
                        member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.min(),
                        member.age.max()
                )
                .from(member)
                .fetch();

        Tuple tuple = result.get(0);
        assertThat(tuple.get(member.count())).isEqualTo(5);
        assertThat(tuple.get(member.age.sum())).isEqualTo(50);
        assertThat(tuple.get(member.age.avg())).isEqualTo(10);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);
        assertThat(tuple.get(member.age.max())).isEqualTo(10);
    }

    @Test
    void group() {
        // 팀 이름과 각 팀의 평균 연령
        List<Tuple> result = queryFactory
                .select(
                        team.name,
                        member.age.avg()
                )
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();

        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);
        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(10);
        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(10);
    }

    @Test
    void group_having() {
        // 팀 이름과 각 팀의 평균 연령이 10 이상인 그룹만
        List<Tuple> result = queryFactory
                .select(
                        team.name,
                        member.age.avg()
                )
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .having(member.id.count().goe(2))   // member가 두명이상인 팀
                .fetch();

        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);
        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(10);
        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(10);
    }

    /**
     * TeamA에 소속된 모든 회원
     */
    @Test
    void join() {
        List<Member> teamAMembers = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                .on(team.name.eq("teamA"))
                .fetch();

        assertThat(teamAMembers).hasSize(3);
        assertThat(teamAMembers)
                .extracting(Member::getTeam)
                    .isNotNull()
                .extracting(Team::getName)
                    .isNotNull()
                .contains("teamA")
        ;
    }

    /**
     * 연관관계가 없는 컬럼을 조인
     * - Member의 이름이 Team 이름과 동일한 컬럼만 가져와라
     */
    @Test
    void theta_join() {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));

        List<Member> members = queryFactory
                .select(member)
                .from(member, team) // join 없이 그냥 모든 테이블을 다 가져와서 카테시안곱을 이용한다.
                .where(member.username.eq(team.name))
                .fetch();

        assertThat(members)
                .extracting("username")
                .containsExactly("teamA", "teamB");
    }

    @Test
    void inner_join_filtering() {
        List<Tuple> teamA = queryFactory
                .select(member, team)
                .from(member)
                // 방법 1.
                .join(member.team, team).on(team.name.eq("teamA"))

                // 방법 2.
//                .innerJoin(member.team, team).on(team.name.eq("teamA"))

                // 방법 3. (결과적으로 동일하나, on절을 사용하면 join할 때 좀 더 적은 튜플을 가져와서 최적화할 수 있다.
//                .join(member.team, team)
//                .where(team.name.eq("teamA"))

                .fetch();

        teamA.forEach(System.out::println);
    }

    @Test
    void left_join_filtering() {
        List<Tuple> teamA = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team).on(team.name.eq("teamA"))
                .fetch();

        teamA.forEach(System.out::println);
    }

    /**
     * 연관관계가 없는 테이블을 막 조인한다고 가정
     * - 멤버의 이름과 팀 이름이 같은 튜플을 left join 한다고 했을 때
     */
    @Test
    void join_on_no_relation() {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));

        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(team).on(member.username.eq(team.name))   // join on절에는 id가 아닌, username과 name 비교조건이 들어간다.
                .fetch();

        result.forEach(System.out::println);
    }

    // EntityManager를 만드는 Factoty를 가져옴
    @PersistenceUnit
    EntityManagerFactory emf;

    @Test
    void fetchJoin_NotUse() {
        em.flush();
        em.clear();

        Member member1 = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(member1.getTeam());
        // em.getEntityManagerFactory().getPersistenceUnitUtil() 를 통해서도 얻을 수 있다.

        assertThat(loaded).isFalse();
    }

    @Test
    void fetchJoin_use() {
        em.flush();
        em.clear();

        Member member1 = queryFactory
                .selectFrom(member)
                .join(member.team, team).fetchJoin()
                .where(member.username.eq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(member1.getTeam());

        assertThat(loaded).isTrue();
    }

    /**
     * 나이가 가장 많은 회원 조회
     */
    @Test
    void subQuery_Max_Age() {
        QMember m = new QMember("m");   // QMember를 또 생성한 이유? 서브쿼리에 들어가는 Memeber의 alias가 동일하면 안되기 때문에.

        Member member1 = queryFactory
                .select(member)
                .from(member)
                .where(member.age.eq(
                        JPAExpressions
                                .select(m.age.max())
                                .from(m)
                ))
                .fetchOne();

        assertThat(member1.getAge()).isEqualTo(80);
    }

    /**
     * 나이가 평균보다 큰 회원 조회
     */
    @Test
    void subQuery_Age_Goe() {
        QMember m = new QMember("m");

        List<Member> members = queryFactory
                .select(member)
                .from(member)
                .where(member.age.goe(
                        JPAExpressions
                                .select(m.age.avg())
                                .from(m)
                ))
                .fetch();

        assertThat(members).extracting("age")
                .containsExactly(60, 70, 80);
    }

    /**
     * 나이가 해당되는 회원 조회
     */
    @Test
    void subQuery_Age_In() {
        QMember m = new QMember("m");

        List<Member> members = queryFactory
                .select(member)
                .from(member)
                .where(member.age.in(
                        JPAExpressions
                                .select(m.age)
                                .from(m)
                                .where(m.age.goe(30))
                ))
                .fetch();

        assertThat(members).extracting("age")
                .containsExactly(60, 70, 80);
    }

    /**
     * 회원이름, 평균나이(Select 서브쿼리 이용) 조회
     */
    @Test
    void selectSubQuery() {
        QMember m = new QMember("m");

        List<Tuple> tuples = queryFactory
                .select(
                        member.username,
                        JPAExpressions.select(m.age.avg())
                                .from(m)
                )
                .from(member)
                .fetch();

        for (Tuple tuple : tuples) {
            System.out.println("tuple = " + tuple);
        }
    }


}
