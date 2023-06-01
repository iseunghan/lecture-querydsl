package me.iseunghan.lecturequerydsl.repository;

import jakarta.persistence.EntityManager;
import me.iseunghan.lecturequerydsl.dto.MemberSearchCond;
import me.iseunghan.lecturequerydsl.dto.MemberTeamDto;
import me.iseunghan.lecturequerydsl.entity.Member;
import me.iseunghan.lecturequerydsl.entity.Team;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
class MemberJpaRepositoryTest {

    @Autowired EntityManager em;
    @Autowired MemberJpaRepository memberJpaRepository;

    @Test
    void jpaBasicTest() {
        // save
        Member member = new Member("member", 10);
        memberJpaRepository.save(member);

        // findById
        Optional<Member> byId = memberJpaRepository.findById(member.getId());
        assertThat(byId).isPresent();
        Member findMember1 = byId.get();
        assertThat(findMember1.getId()).isEqualTo(member.getId());

        // findByUsername
        List<Member> member1 = memberJpaRepository.findByUsername("member");
        assertThat(member1).containsExactly(member);

        // findAll
        List<Member> member2 = memberJpaRepository.findAll();
        assertThat(member1).containsExactly(member);
    }

    @Test
    void queryDslBasicTest() {
        // save
        Member member = new Member("member", 10);
        memberJpaRepository.save(member);

        // findById
        Optional<Member> byId = memberJpaRepository.findById_dsl(member.getId());
        assertThat(byId).isPresent();
        Member findMember1 = byId.get();
        assertThat(findMember1.getId()).isEqualTo(member.getId());

        // findByUsername
        List<Member> member1 = memberJpaRepository.findByUsername_dsl("member");
        assertThat(member1).containsExactly(member);

        // findAll
        List<Member> member2 = memberJpaRepository.findAll_dsl();
        assertThat(member1).containsExactly(member);
    }

    void setup() {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 10, teamA);
        Member member3 = new Member("member3", 10, teamA);
        Member member4 = new Member("member4", 10, teamB);
        Member member5 = new Member("member5", 50, teamB);
        Member member6 = new Member("member6", 60, teamB);
        Member member7 = new Member("member7", 70, teamB);
        Member member8 = new Member("member8", 80, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
        em.persist(member5);
        em.persist(member6);
        em.persist(member7);
        em.persist(member8);

        em.flush();
        em.clear();
    }

    @Test
    void dynamicQuerydsl_builder_Test() {
        // given
        setup();

        MemberSearchCond cond = new MemberSearchCond();
        cond.setUsername("member5");
        cond.setAgeGoe(40);
        cond.setAgeLoe(80);
        cond.setTeamName("teamB");

        // when
        List<MemberTeamDto> result = memberJpaRepository.dynamicSearchMember_Builder(cond);

        // then
        assertThat(result).extracting("username").containsExactly("member5");
    }

    @Test
    void dynamicQuerydsl_WhereParams_Test() {
        // given
        setup();

        MemberSearchCond cond = new MemberSearchCond();
        cond.setUsername("member5");
        cond.setAgeGoe(40);
        cond.setAgeLoe(80);
        cond.setTeamName("teamB");

        // when
        List<MemberTeamDto> result = memberJpaRepository.dynamicSearchMember_WhereParams(cond);

        // then
        assertThat(result).extracting("username").containsExactly("member5");
    }

}