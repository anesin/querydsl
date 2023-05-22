package study.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.entity.QMember.*;


@SpringBootTest
@Transactional
public class QuerydslBasicTest {

  @Autowired  // or @PersistenceContext
  EntityManager em;

  JPAQueryFactory factory;


  @BeforeEach
  public void before() {
    factory = new JPAQueryFactory(em);

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


  @Test
  void startJPQL() {
    String jpql = "select m from Member m where m.username = :username";
    Member findMember = em.createQuery(jpql, Member.class)
                          .setParameter("username", "member1")
                          .getSingleResult();
    assertThat(findMember.getUsername()).isEqualTo("member1");
  }


  @Test
  void startQuerydsl() {
    Member findMember = factory.select(member)
                               .from(member)
                               .where(member.username.eq("member1"))
                               .fetchOne();
    assertThat(findMember.getUsername()).isEqualTo("member1");
  }


  @Test
  void search() {
    Member findMember = factory.selectFrom(member)
                               .where(member.username.eq("member1")
                                            .and(member.age.eq(10)))
                               .fetchOne();
    assertThat(findMember.getUsername()).isEqualTo("member1");
  }


  @Test
  void searchAndParam() {
    Member findMember = factory.selectFrom(member)
                               .where(member.username.eq("member1"),
                                      member.age.eq(10))
                               .fetchOne();
    assertThat(findMember.getUsername()).isEqualTo("member1");
  }

}
