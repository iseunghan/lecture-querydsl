package me.iseunghan.lecturequerydsl.repository;

import jakarta.persistence.EntityManager;
import me.iseunghan.lecturequerydsl.dto.MemberSearchCond;
import me.iseunghan.lecturequerydsl.dto.MemberTeamDto;
import me.iseunghan.lecturequerydsl.entity.Member;
import me.iseunghan.lecturequerydsl.entity.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
class MemberTestRepositoryTest {

    @Autowired
    EntityManager em;
    @Autowired MemberTestRepository memberTestRepository;

    @BeforeEach
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
    void basicSelect() {
        // when
        List<Member> members = memberTestRepository.basicSelect();

        // then
        assertThat(members)
                .extracting("username")
                .containsExactlyInAnyOrder("member1",
                        "member2",
                        "member3",
                        "member4",
                        "member5",
                        "member6",
                        "member7",
                        "member8");
    }

    @Test
    void basicSelectFrom() {
        // when
        List<Member> members = memberTestRepository.basicSelectFrom();

        // then
        assertThat(members)
                .extracting("username")
                .containsExactlyInAnyOrder("member1",
                        "member2",
                        "member3",
                        "member4",
                        "member5",
                        "member6",
                        "member7",
                        "member8");
    }

    @Test
    void pageSelect() {
        // given
        PageRequest pageRequest = PageRequest.of(0, 10);
        MemberSearchCond cond = new MemberSearchCond();
        cond.setUsername("member5");
        cond.setAgeGoe(40);
        cond.setAgeLoe(80);
        cond.setTeamName("teamB");

        // when
        Page<MemberTeamDto> result = memberTestRepository.pageSelect(cond, pageRequest);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getContent().get(0).getUsername()).isEqualTo("member5");
    }

    @Test
    void pageSelect_Count() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        MemberSearchCond cond = new MemberSearchCond();
        cond.setUsername("member5");
        cond.setAgeGoe(40);
        cond.setAgeLoe(80);
        cond.setTeamName("teamB");

        // when
        Page<MemberTeamDto> result = memberTestRepository.pageSelect_Count(cond, pageRequest);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getContent().get(0).getUsername()).isEqualTo("member5");
    }
}