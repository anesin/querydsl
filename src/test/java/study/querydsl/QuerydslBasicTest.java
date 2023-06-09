package study.querydsl;

import com.querydsl.core.NonUniqueResultException;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.transaction.Transactional;
import java.util.List;

import static com.querydsl.jpa.JPAExpressions.select;
import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;


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


  @Test
  void resultFetch() {
    List<Member> fetch = factory.selectFrom(member)
                                .fetch();

    try {
      Member findMember1 = factory.selectFrom(member)
                                  .fetchOne();
    }
    catch (NonUniqueResultException ignored) {}

    Member findMember2 = factory.selectFrom(member)
                                .fetchFirst();

    // Deprecated: recommend use fetch() instead
    QueryResults<Member> queryResults = factory.selectFrom(member)
                                               .fetchResults();
    long total = queryResults.getTotal();
    List<Member> results = queryResults.getResults();

    // Deprecated: recommend use the size of fetch() instead
    long count = factory.selectFrom(member)
                        .fetchCount();
  }


  @Test
  void sort() {
    em.persist(new Member(null, 100));
    em.persist(new Member("member5", 100));
    em.persist(new Member("member6", 100));

    List<Member> results = factory.selectFrom(member)
                                  .where(member.age.eq(100))
                                  .orderBy(member.age.desc(), member.username.asc().nullsLast())
                                  .fetch();
    Member member5 = results.get(0);
    Member member6 = results.get(1);
    Member memberNull = results.get(2);
    assertThat(member5.getUsername()).isEqualTo("member5");
    assertThat(member6.getUsername()).isEqualTo("member6");
    assertThat(memberNull.getUsername()).isNull();
  }


  @Test
  void paging1() {
    List<Member> result = factory.selectFrom(member)
                                 .orderBy(member.username.desc())
                                 .offset(1)
                                 .limit(2)
                                 .fetch();
    assertThat(result.size()).isEqualTo(2);
  }


  @Test
  void paging2() {
    QueryResults<Member> queryResults = factory.selectFrom(member)
                                               .orderBy(member.username.desc())
                                               .offset(1)
                                               .limit(2)
                                               .fetchResults();
    assertThat(queryResults.getTotal()).isEqualTo(4);
    assertThat(queryResults.getOffset()).isEqualTo(1);
    assertThat(queryResults.getLimit()).isEqualTo(2);
    assertThat(queryResults.getResults().size()).isEqualTo(2);
  }


  @Test
  void aggregation() {
    List<Tuple> result = factory.select(
                                    member.count(),
                                    member.age.sum(),
                                    member.age.avg(),
                                    member.age.max(),
                                    member.age.min())
                               .from(member)
                               .fetch();
    Tuple tuple = result.get(0);
    assertThat(tuple.get(member.count())).isEqualTo(4);
    assertThat(tuple.get(member.age.sum())).isEqualTo(100);
    assertThat(tuple.get(member.age.avg())).isEqualTo(25);
    assertThat(tuple.get(member.age.max())).isEqualTo(40);
    assertThat(tuple.get(member.age.min())).isEqualTo(10);
  }


  @Test
  void group() {
    List<Tuple> result = factory.select(team.name, member.age.avg())
                                .from(member)
                                .join(member.team, team)
                                .groupBy(team.name)
                                .fetch();

    Tuple teamA = result.get(0);
    Tuple teamB = result.get(1);

    assertThat(teamA.get(team.name)).isEqualTo("teamA");
    assertThat(teamA.get(member.age.avg())).isEqualTo(15);  // 10, 20

    assertThat(teamB.get(team.name)).isEqualTo("teamB");
    assertThat(teamB.get(member.age.avg())).isEqualTo(35);  // 30, 40
  }


  @Test
  void join() {
    List<Member> result = factory.selectFrom(member)
                                 .join(member.team, team)
                                 .where(team.name.eq("teamA"))
                                 .fetch();

    assertThat(result)
        .extracting("username")
        .containsExactly("member1", "member2");
  }


  @Test
  void theta_join() {
    em.persist(new Member("teamA"));
    em.persist(new Member("teamB"));
    em.persist(new Member("teamC"));

    List<Member> result = factory.select(member)
                                 .from(member, team)
                                 .where(member.username.eq(team.name))
                                 .fetch();

    assertThat(result)
        .extracting("username")
        .containsExactly("teamA", "teamB");
  }


  @Test
  void join_on_filtering() {
    List<Tuple> result = factory.select(member, team)
                                .from(member)
                                .leftJoin(member.team, team)
                                .on(team.name.eq("teamA"))
                                .fetch();

    for (var tuple : result)
      System.out.println("tuple = " + tuple);
  }


  @Test
  void join_on_no_relation() {
    em.persist(new Member("teamA"));
    em.persist(new Member("teamB"));
    em.persist(new Member("teamC"));

    List<Tuple> result = factory.select(member, team)
                                .from(member)
                                .leftJoin(team)
                                .on(member.username.eq(team.name))
                                .fetch();

    for (var tuple : result)
      System.out.println("tuple = " + tuple);
  }


  @PersistenceUnit
  EntityManagerFactory emf;


  @Test
  void fetchJoinNo() {
    em.flush();
    em.clear();

    Member findMember = factory.selectFrom(member)
                               .where(member.username.eq("member1"))
                               .fetchOne();

    boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
    assertThat(loaded).as("패치 조인 미적용").isFalse();
  }


  @Test
  void fetchJoinUse() {
    em.flush();
    em.clear();

    Member findMember = factory.selectFrom(member)
                               .join(member.team, team)
                               .fetchJoin()  // HERE !!!
                               .where(member.username.eq("member1"))
                               .fetchOne();

    boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
    assertThat(loaded).as("패치 조인 적용").isTrue();
  }


  @Test
  void subQueryEq() {
    QMember memberSub = new QMember("memberSub");

    List<Member> result = factory.selectFrom(member)
                                 .where(member.age.eq(
                                     select(memberSub.age.max()).from(memberSub)))
                                 .fetch();

    assertThat(result).extracting("age")
                      .containsExactly(40);
  }


  @Test
  void subQueryGoe() {
    QMember memberSub = new QMember("memberSub");

    List<Member> result = factory.selectFrom(member)
                                 .where(member.age.goe(
                                     select(memberSub.age.avg()).from(memberSub)))
                                 .fetch();

    assertThat(result).extracting("age")
                      .containsExactly(30, 40);
  }


  @Test
  void subQueryIn() {
    QMember memberSub = new QMember("memberSub");

    List<Member> result = factory.selectFrom(member)
                                 .where(member.age.in(
                                     select(memberSub.age).from(memberSub)
                                                          .where(memberSub.age.gt(10))))
                                 .fetch();

    assertThat(result).extracting("age")
                      .containsExactly(20, 30, 40);
  }


  @Test
  void subQuerySelect() {
    QMember memberSub = new QMember("memberSub");

    List<Tuple> fetch = factory.select(member, select(memberSub.age.avg()).from(memberSub))
                               .from(member)
                               .fetch();

    for (var tuple : fetch)
      System.out.println("tuple = " + tuple);
  }


  @Test
  void basicCase() {
    factory.select(member.age
                         .when(10).then("열살")
                         .when(20).then("스무살")
                         .otherwise("기타"))
            .from(member)
            .fetch()
            .forEach(s -> System.out.println("s = " + s));
  }


  @Test
  void complexCase() {
    factory.select(new CaseBuilder()
                    .when(member.age.between(0, 20)).then("0~20살")
                    .when(member.age.between(21, 30)).then("21~30살")
                    .otherwise("기타"))
            .from(member)
            .fetch()
            .forEach(s -> System.out.println("s = " + s));
  }


  @Test
  void constant() {
    factory.select(member.username, Expressions.constant("A"))
           .from(member)
           .fetch()
           .forEach(t -> System.out.println("t = " + t));
  }


  @Test
  void concat() {
    factory.select(member.username.concat("_").concat(member.age.stringValue()))
           .from(member)
           .where(member.username.eq("member1"))
           .fetch()
           .forEach(s -> System.out.println("s = " + s));
  }

}
