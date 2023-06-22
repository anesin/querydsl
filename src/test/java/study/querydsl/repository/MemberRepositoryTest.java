package study.querydsl.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.entity.QMember.member;


@SpringBootTest
@Transactional
class MemberRepositoryTest {

  @Autowired
  private EntityManager em;

  @Autowired
  private MemberRepository memberRepository;


  @Test
  void basicTest() {
    Member member = new Member("member1");
    memberRepository.save(member);

    Member findMember = memberRepository.findById(member.getId()).get();
    assertThat(findMember).isEqualTo(member);

    List<Member> result1 = memberRepository.findAll();
    assertThat(result1).containsExactly(member);

    List<Member> result2 = memberRepository.findByUsername(member.getUsername());
    assertThat(result2).containsExactly(member);
  }


  @Test
  void searchTest() {
    readyData();

    MemberSearchCondition condition = new MemberSearchCondition();
    condition.setAgeGoe(35);
    condition.setAgeLoe(40);
    condition.setTeamName("teamB");
    List<MemberTeamDto> result = memberRepository.search(condition);
    assertThat(result).extracting("username").containsExactly("member4");
  }


  @Test
  void searchPageTest() {
    readyData();

    MemberSearchCondition condition = new MemberSearchCondition();
    PageRequest pageRequest = PageRequest.of(0, 3);
    Page<MemberTeamDto> resultsSimple = memberRepository.searchPageSimple(condition, pageRequest);
    assertThat(resultsSimple.getContent().size()).isEqualTo(3);
    assertThat(resultsSimple)
        .extracting("username")
        .containsExactly("member1", "member2", "member3");

    Page<MemberTeamDto> resultsComplex = memberRepository.searchPageComplex(condition, pageRequest);
    assertThat(resultsComplex.getContent().size()).isEqualTo(3);
    assertThat(resultsComplex)
        .extracting("username")
        .containsExactly("member1", "member2", "member3");
  }


  @Test
  void querydslPredicateExecutorTest() {
    readyData();

    memberRepository.findAll(member.age.between(10, 40).and(member.username.eq("member1")))
                    .forEach(System.out::println);
  }


  private void readyData() {
    Team teamA = new Team("teamA");
    Team teamB = new Team("teamB");
    em.persist(teamA);
    em.persist(teamB);

    Member member1 = new Member("member1", 10, teamA);
    Member member2 = new Member("member2", 20, teamA);
    Member member3 = new Member("member3", 30, teamB);
    Member member4 = new Member("member4", 40, teamB);
    em.persist(member1);
    em.persist(member2);
    em.persist(member3);
    em.persist(member4);
  }

}