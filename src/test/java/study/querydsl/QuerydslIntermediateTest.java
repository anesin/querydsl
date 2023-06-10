package study.querydsl;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import study.querydsl.dto.MemberDto;
import study.querydsl.dto.QMemberDto;
import study.querydsl.dto.UserDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;

import static study.querydsl.entity.QMember.member;


@SpringBootTest
@Transactional
public class QuerydslIntermediateTest {

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
  void simpleProjection() {
    factory.select(member.username)
           .from(member)
           .fetch()
           .forEach(System.out::println);
  }


  @Test
  void tupleProjection() {
    List<Tuple> result = factory.select(member.username, member.age)
                                .from(member)
                                .fetch();
    for (var tuple : result) {
      String username = tuple.get(member.username);
      Integer age = tuple.get(member.age);
      System.out.println("username = " + username + ", age = " + age);
    }
  }


  @Test
  void findDtoByJPQL() {
    String jpql = "select new study.querydsl.dto.MemberDto(m.username, m.age) from Member m";
    em.createQuery(jpql, MemberDto.class)
      .getResultList()
      .forEach(System.out::println);
  }


  @Test
  void findDtoBySetter() {
    factory.select(Projections.bean(MemberDto.class, member.username, member.age))
           .from(member)
           .fetch()
           .forEach(System.out::println);
  }


  @Test
  void findDtoByField() {
    factory.select(Projections.fields(MemberDto.class, member.username, member.age))
           .from(member)
           .fetch()
           .forEach(System.out::println);
  }


  @Test
  void findUserDtoName() {
    factory.select(Projections.fields(UserDto.class, member.username.as("name"), member.age))
           .from(member)
           .fetch()
           .forEach(System.out::println);
  }


  @Test
  void findUserDtoAge() {
    QMember memberSub = new QMember("memberSub");
    var ex = JPAExpressions.select(memberSub.age.max()).from(memberSub);
    var fields = Projections.fields(UserDto.class,
                                    member.username.as("name"),
                                    ExpressionUtils.as(ex, "age"));
    factory.select(fields)
           .from(member)
           .fetch()
           .forEach(System.out::println);
  }


  @Test
  void findDtoByConstructor() {
    factory.select(Projections.constructor(MemberDto.class, member.username, member.age))
           .from(member)
           .fetch()
           .forEach(System.out::println);
  }


  @Test
  void findDtoByQueryProjection() {
    factory.select(new QMemberDto(member.username, member.age))
           .from(member)
           .fetch()
           .forEach(System.out::println);
  }

}
