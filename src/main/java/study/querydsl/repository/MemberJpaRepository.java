package study.querydsl.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.NonNull;
import org.hibernate.criterion.SimpleExpression;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.QTeam;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.springframework.util.StringUtils.hasText;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;


@Repository
public class MemberJpaRepository {

  private EntityManager em;
  private JPAQueryFactory queryFactory;


  public MemberJpaRepository(EntityManager em) {
    this.em = em;
    this.queryFactory = new JPAQueryFactory(em);
  }


  public void save(Member member) {
    em.persist(member);
  }


  public Optional<Member> findById(long id) {
    return Optional.ofNullable(em.find(Member.class, id));
  }


  public List<Member> findAll() {
    return em.createQuery("select m from Member m", Member.class)
             .getResultList();
  }


  public List<Member> findAll_Querydsl() {
    return queryFactory.selectFrom(member).fetch();
  }


  public List<Member> findByUsername(String username) {
    return em.createQuery("select m from Member m where m.username = :username", Member.class)
             .setParameter("username", username)
             .getResultList();
  }


  public List<Member> findByUsername_Querydsl(String username) {
    return queryFactory.selectFrom(member)
                       .where(member.username.eq(username))
                       .fetch();
  }


  public List<MemberTeamDto> searchByBuilder(MemberSearchCondition condition) {
    BooleanBuilder builder = new BooleanBuilder();
    builderAnd(builder, StringUtils::hasText, condition.getUsername(), member.username::eq);
    builderAnd(builder, StringUtils::hasText, condition.getTeamName(), team.name::eq);
    builderAnd(builder, Objects::nonNull, condition.getAgeGoe(), member.age::goe);
    builderAnd(builder, Objects::nonNull, condition.getAgeLoe(), member.age::loe);

    QMemberTeamDto qMemberTeamDto = new QMemberTeamDto(member.id, member.username, member.age, team.id, team.name);
    return queryFactory.select(qMemberTeamDto)
                       .from(member)
                       .leftJoin(member.team, team)
                       .where(builder)
                       .fetch();
  }


  private <T> void builderAnd(BooleanBuilder builder, Predicate<T> predicate, T t,
                              Function<T, BooleanExpression> expression) {
    if (predicate.test(t))
      builder.and(expression.apply(t));
  }


  public List<MemberTeamDto> search(MemberSearchCondition condition) {
    QMemberTeamDto qMemberTeamDto = new QMemberTeamDto(member.id, member.username, member.age, team.id, team.name);
    return queryFactory.select(qMemberTeamDto)
                       .from(member)
                       .leftJoin(member.team, team)
                       .where(usernameEq(condition.getUsername()),
                              teamNameEq(condition.getTeamName()),
                              ageGoe(condition.getAgeGoe()),
                              ageLoe(condition.getAgeLoe()))
                       .fetch();
  }


  public List<Member> searchMember(MemberSearchCondition condition) {
    return queryFactory.selectFrom(member)
                       .leftJoin(member.team, team)
                       .where(usernameEq(condition.getUsername()),
                              teamNameEq(condition.getTeamName()),
                              ageGoe(condition.getAgeGoe()),
                              ageLoe(condition.getAgeLoe()))
                       .fetch();
  }


  private BooleanExpression usernameEq(String username) {
    return hasText(username)? member.username.eq(username): null;
  }


  private BooleanExpression teamNameEq(String teamName) {
    return hasText(teamName)? team.name.eq(teamName): null;
  }


  private BooleanExpression ageGoe(Integer ageGoe) {
    return ageGoe == null? null: member.age.goe(ageGoe);
  }


  private BooleanExpression ageLoe(Integer ageLoe) {
    return ageLoe == null? null:  member.age.loe(ageLoe);
  }

}
