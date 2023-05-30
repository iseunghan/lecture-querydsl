package me.iseunghan.lecturequerydsl.repository;

import jakarta.persistence.EntityManager;
import me.iseunghan.lecturequerydsl.entity.Member;
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

}